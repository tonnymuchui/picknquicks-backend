package com.picknquicks.scheduler;
import com.picknquicks.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredKeys() {
        log.info("Starting idempotency keys cleanup");
        idempotencyService.deleteExpired();
        log.info("Idempotency keys cleanup completed");
    }
}