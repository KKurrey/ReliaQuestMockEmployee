package com.reliaquest.api.config;

import com.reliaquest.api.constants.ClientConstants;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Resilience4jRetryLoggerConfig {

    private final RetryRegistry retryRegistry;

    @PostConstruct
    public void setupRetryLogging() {

        Retry retry = retryRegistry.retry(ClientConstants.EMPLOYEE_API_RETRY);

        retry.getEventPublisher().onRetry(event -> {
            log.info(
                    "Retry attempt #{} for '{}' due to: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getName(),
                    event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown");
        });

        retry.getEventPublisher().onError(event -> {
            log.error(
                    "Retry failed after {} attempts for '{}'. Cause: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getName(),
                    event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown");
        });

        log.info("Retry logging set up for '{}'", ClientConstants.EMPLOYEE_API_RETRY);
    }
}
