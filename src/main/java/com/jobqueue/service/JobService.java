package com.jobqueue.service;

import com.jobqueue.dto.CreateJobRequest;
import com.jobqueue.exception.JobNotFoundException;
import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;

    public JobService(JobRepository jobRepository, JobExecutionService jobExecutionService) {
        this.jobRepository = jobRepository;
        this.jobExecutionService = jobExecutionService;
    }

    /**
     * The "waiter" step: take the order, hand back a ticket number immediately,
     * then quietly signal the kitchen to start cooking in the background.
     */
    public Job submit(CreateJobRequest request) {
        Job job = new Job(request.getType(), request.getPayload());
        job = jobRepository.save(job);

        // Fire-and-forget: this call returns immediately because executeJob
        // is @Async - the actual processing happens on a worker thread.
        jobExecutionService.executeJob(job.getId());

        return job;
    }

    public Job getById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    public Page<Job> list(JobStatus status, String type, Pageable pageable) {
        if (status != null && type != null) {
            return jobRepository.findByStatusAndType(status, type, pageable);
        }
        if (status != null) {
            return jobRepository.findByStatus(status, pageable);
        }
        return jobRepository.findAll(pageable);
    }
}
