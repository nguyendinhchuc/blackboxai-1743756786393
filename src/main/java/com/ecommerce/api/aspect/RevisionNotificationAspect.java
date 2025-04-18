package com.ecommerce.api.aspect;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.exception.RevisionNotificationException;
import com.ecommerce.api.metrics.RevisionMetrics;
import com.ecommerce.api.notification.RevisionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RevisionNotificationAspect {

    private final RevisionMetrics revisionMetrics;
    private final RevisionNotificationConfig notificationConfig;

    /**
     * Around advice for notification methods
     */
    @Around("execution(* com.ecommerce.api.notification.RevisionNotificationService.*(..))")
    public Object aroundNotificationMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.debug("Starting notification method: {} with args: {}", methodName, 
            Arrays.toString(args));

        Instant start = Instant.now();
        try {
            // Check if notifications are enabled
            if (!notificationConfig.isEmailEnabled()) {
                log.warn("Notifications are disabled. Skipping method: {}", methodName);
                return null;
            }

            // Record metrics before execution
            recordPreExecutionMetrics(methodName, args);

            // Execute the method with retry capability
            Object result = executeWithRetry(joinPoint);

            // Record success metrics
            recordSuccessMetrics(methodName, Duration.between(start, Instant.now()));

            return result;
        } catch (Exception e) {
            // Record failure metrics
            recordFailureMetrics(methodName, e);
            throw handleException(e, methodName, args);
        } finally {
            log.debug("Completed notification method: {} in {} ms", methodName,
                Duration.between(start, Instant.now()).toMillis());
        }
    }

    /**
     * Execute method with retry capability
     */
    @Retryable(
        value = {MessagingException.class, TimeoutException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private Object executeWithRetry(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        RevisionNotificationConfig.RetryConfig retryConfig = notificationConfig.getRetryConfig();
        Exception lastException = null;

        for (int attempt = 0; attempt < retryConfig.maxRetries(); attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                lastException = e;
                if (!shouldRetry(e) || !retryConfig.shouldRetry(attempt)) {
                    break;
                }
                handleRetryAttempt(methodName, attempt, e);
                Thread.sleep(retryConfig.getNextDelay(attempt));
            }
        }

        throw handleMaxRetriesExceeded(methodName, lastException);
    }

    /**
     * Record pre-execution metrics
     */
    private void recordPreExecutionMetrics(String methodName, Object[] args) {
        revisionMetrics.recordApiCall(
            "notification",
            methodName,
            200  // Initial status code
        );
    }

    /**
     * Record success metrics
     */
    private void recordSuccessMetrics(String methodName, Duration duration) {
        revisionMetrics.recordQueryPerformance(
            "notification_" + methodName,
            duration.toMillis()
        );
    }

    /**
     * Record failure metrics
     */
    private void recordFailureMetrics(String methodName, Exception e) {
        revisionMetrics.recordRevisionError(
            "notification",
            e.getClass().getSimpleName()
        );
    }

    /**
     * Handle retry attempt
     */
    private void handleRetryAttempt(String methodName, int attempt, Exception e) {
        log.warn("Retry attempt {} for method {} failed: {}", 
            attempt + 1, methodName, e.getMessage());
        
        if (attempt > 0) {
            revisionMetrics.recordRevisionError(
                "notification_retry",
                "attempt_" + (attempt + 1)
            );
        }
    }

    /**
     * Check if exception is retryable
     */
    private boolean shouldRetry(Exception e) {
        return e instanceof MessagingException || 
               e instanceof TimeoutException ||
               e.getCause() instanceof MessagingException ||
               e.getCause() instanceof TimeoutException;
    }

    /**
     * Handle max retries exceeded
     */
    private RevisionNotificationException handleMaxRetriesExceeded(String methodName, 
            Exception lastException) {
        log.error("Max retries exceeded for method {}: {}", 
            methodName, lastException.getMessage());
        
        return RevisionNotificationException.maxRetriesExceeded(
            methodName,
            notificationConfig.getRetryConfig().maxRetries()
        );
    }

    /**
     * Handle exception
     */
    private RevisionNotificationException handleException(Exception e, String methodName, 
            Object[] args) {
        log.error("Error in notification method {}: {}", methodName, e.getMessage());

        if (e instanceof RevisionNotificationException) {
            return (RevisionNotificationException) e;
        }

        String recipient = extractRecipient(args);
        if (e instanceof MessagingException) {
            return RevisionNotificationException.emailSendingFailed(
                recipient, e.getMessage(), e);
        }

        return RevisionNotificationException.processingError(
            recipient, 0, e.getMessage(), e);
    }

    /**
     * Extract recipient from method arguments
     */
    private String extractRecipient(Object[] args) {
        return Arrays.stream(args)
            .filter(arg -> arg instanceof String)
            .map(String.class::cast)
            .findFirst()
            .orElse("unknown");
    }

    /**
     * Get method parameter names
     */
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getParameterNames();
    }
}
