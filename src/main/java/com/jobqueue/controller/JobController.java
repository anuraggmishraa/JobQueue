package com.jobqueue.controller;

import com.jobqueue.dto.CreateJobRequest;
import com.jobqueue.dto.JobResponse;
import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Submit a job. Returns instantly - the job hasn't been processed yet, just accepted.
    @PostMapping
    public ResponseEntity<JobResponse> submit(@Valid @RequestBody CreateJobRequest request) {
        Job job = jobService.submit(request);
        JobResponse response = JobResponse.from(job);
        return ResponseEntity
                .created(URI.create("/jobs/" + job.getId()))
                .body(response);
    }

    // Check on a specific job by its ticket number (ID).
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        Job job = jobService.getById(id);
        return ResponseEntity.ok(JobResponse.from(job));
    }

    // List/filter jobs, e.g. GET /jobs?status=FAILED&type=send-email&page=0&size=20
    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<JobResponse> page = jobService.list(status, type, pageable).map(JobResponse::from);
        return ResponseEntity.ok(page);
    }
}
