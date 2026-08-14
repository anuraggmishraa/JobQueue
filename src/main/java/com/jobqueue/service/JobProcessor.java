package com.jobqueue.service;

import com.jobqueue.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for "real" work - sending an email, resizing an image, generating
 * a report, etc. Swap the body of process() for whatever your job actually
 * needs to do. Deliberately fails ~30% of the time so you can watch retries
 * kick in.
 */
@Component
public class JobProcessor {

    private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);

    public String process(Job job) throws Exception {
        log.info("Processing job {} of type {}", job.getId(), job.getType());

        // Simulate work taking real time
        Thread.sleep(1500);

        // Simulate flaky downstream dependency (e.g. a third-party API)
        if (ThreadLocalRandom.current().nextInt(100) < 30) {
            throw new RuntimeException("Simulated transient failure processing job " + job.getId());
        }

        return "Processed '" + job.getType() + "' successfully at " + java.time.Instant.now();
    }
}
