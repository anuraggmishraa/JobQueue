# Async Job Queue (Spring Boot)

A job queue where clients submit work, get an instant ticket number back, and
background workers process jobs concurrently with automatic retries.

## Run it

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`. H2 console (to peek at the `jobs`
table) is at `http://localhost:8080/h2-console` — JDBC URL
`jdbc:h2:mem:jobqueue`, user `sa`, empty password.

## Try it

**Submit a job:**
```bash
curl -X POST http://localhost:8080/jobs \
  -H "Content-Type: application/json" \
  -d '{"type": "send-email", "payload": "{\"to\":\"user@example.com\"}"}'
```

Copy the `id` from the response, then:

**Check status:**
```bash
curl http://localhost:8080/jobs/<id>
```

Poll it a few times — you'll see it go `PENDING` -> `RUNNING` -> `COMPLETED`
(or `FAILED` after 3 retries, since `JobProcessor` fails ~30% of the time on
purpose so you can watch retries happen).

**List/filter jobs:**
```bash
curl "http://localhost:8080/jobs?status=FAILED&page=0&size=10"
```

**Fire a bunch at once to see concurrency:**
```bash
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/jobs \
    -H "Content-Type: application/json" \
    -d "{\"type\": \"job-$i\"}" &
done
wait
```
With `corePoolSize=3` in `AsyncConfig`, you'll see 3 jobs processing at a
time in the logs (`job-worker-1`, `job-worker-2`, `job-worker-3`).

## Architecture

1. `POST /jobs` saves a row with status `PENDING` and returns immediately.
2. `JobService.submit()` fires `JobExecutionService.executeJob()`, which is
   `@Async` — runs on a background thread pool (`AsyncConfig`), not the
   request thread.
3. The worker marks the job `RUNNING`, calls `JobProcessor`, and on success
   marks it `COMPLETED`.
4. On failure, `@Retryable` re-runs the method with exponential backoff
   (2s, 4s, 8s). After 3 failed attempts, `@Recover` marks it `FAILED`
   permanently (our "dead letter" state).
5. `GET /jobs/{id}` lets the client poll for status/result at any time.

## Known limitation (worth mentioning in interviews)

Jobs live in an in-memory thread pool queue. If the app crashes mid-processing,
any `RUNNING` or queued job is lost from the executor (though the DB row still
shows its last known state). A production version would either use a durable
broker (RabbitMQ/Kafka) or re-scan for `PENDING`/`RUNNING` jobs on startup and
re-submit them.

## Next steps to extend

- Swap H2 for Postgres in `application.yml` for persistence across restarts
- Add a startup recovery scan (`ApplicationRunner` that re-queues stuck jobs)
- Add job cancellation, priority queues, or idempotency keys
- Expose custom Actuator metrics for queue depth / active workers
