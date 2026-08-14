package com.jobqueue.repository;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByStatusAndType(JobStatus status, String type, Pageable pageable);

    // Used on startup to recover jobs that were left mid-flight if the app crashed
    List<Job> findByStatusIn(List<JobStatus> statuses);

    long countByStatus(JobStatus status);
}
