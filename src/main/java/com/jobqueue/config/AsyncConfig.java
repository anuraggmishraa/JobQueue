package com.jobqueue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    // This is our "worker pool" - the cooks in the kitchen.
    @Bean(name = "jobExecutor")
    public Executor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(3);      // always-on workers
        executor.setMaxPoolSize(6);       // burst capacity under load
        executor.setQueueCapacity(50);    // jobs waiting in line before we push back
        executor.setThreadNamePrefix("job-worker-");

        // If the queue is full AND we're at max workers, run it on the caller's
        // thread instead of silently dropping the job. This applies backpressure
        // rather than losing work.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
