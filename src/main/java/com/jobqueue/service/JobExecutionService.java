package com.jobqueue.service;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * This is the "kitchen" - runs on a background worker thread (see AsyncConfig),
 * separate from the request thread that accepted the job.
 */
@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobRepository jobRepository;
    private final JobProcessor jobProcessor;

    public JobExecutionService(JobRepository jobRepository, JobProcessor jobProcessor) {
        this.jobRepository = jobRepository;
        this.jobProcessor = jobProcessor;
    }

    /**
     * Entry point called right after a job is submitted. Runs on the
     * "jobExecutor" thread pool so the HTTP request thread never waits on it.
     *
     * @Retryable will re-invoke this whole method (including marking RUNNING
     * again) up to 3 times, waiting longer between each attempt, if it throws.
     * Once retries are exhausted, @Recover below takes over instead of the
     * exception propagating further (there's no one left to catch it - this
     * runs on a background thread with no caller waiting).
     */
    @Async("jobExecutor")
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2) // 2s, then 4s, then 8s
    )
    @Transactional
    public void executeJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job disappeared: " + jobId));

        job.setStatus(JobStatus.RUNNING);
        job.setAttempts(job.getAttempts() + 1);
        jobRepository.save(job);

        try {
            String result = jobProcessor.process(job);
            job.setStatus(JobStatus.COMPLETED);
            job.setResult(result);
            job.setErrorMessage(null);
            jobRepository.save(job);
            log.info("Job {} completed on attempt {}", jobId, job.getAttempts());
        } catch (Exception e) {
            // Record what happened, then rethrow so @Retryable knows to try again
            job.setErrorMessage(e.getMessage());
            jobRepository.save(job);
            log.warn("Job {} failed on attempt {}: {}", jobId, job.getAttempts(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Called automatically once @Retryable gives up (3 failed attempts).
     * This is where the job becomes permanently FAILED - our "dead letter" state.
     */
    @Recover
    @Transactional
    public void recover(RuntimeException e, UUID jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Giving up after " + job.getAttempts() + " attempts: " + e.getMessage());
            jobRepository.save(job);
            log.error("Job {} permanently failed after {} attempts", jobId, job.getAttempts());
        });
    }
}
