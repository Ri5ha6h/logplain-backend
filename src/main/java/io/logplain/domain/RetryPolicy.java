package io.logplain.domain;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public record RetryPolicy(
        int maxAttempts,
        Duration initialDelay,
        double backoffMultiplier,
        Duration maxDelay,
        Set<ErrorCategory> retryableCategories) {

    public RetryPolicy {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        requirePositive(initialDelay, "initialDelay");
        requirePositive(maxDelay, "maxDelay");
        if (backoffMultiplier < 1.0 || Double.isNaN(backoffMultiplier) || Double.isInfinite(backoffMultiplier)) {
            throw new IllegalArgumentException("backoffMultiplier must be finite and at least 1");
        }
        retryableCategories = Set.copyOf(Objects.requireNonNull(retryableCategories, "retryableCategories"));
    }

    public boolean shouldRetry(DomainError error, int completedAttempts) {
        Objects.requireNonNull(error, "error");
        if (completedAttempts < 1 || completedAttempts >= maxAttempts) {
            return false;
        }
        // An uncertain external effect needs explicit resolution. It is never auto-retried.
        return error.category() != ErrorCategory.UNCERTAIN_DELIVERY
                && retryableCategories.contains(error.category());
    }

    public Duration delayForAttempt(int attemptNumber) {
        if (attemptNumber < 2) {
            throw new IllegalArgumentException("A retry attempt number must be at least 2");
        }
        var exponent = attemptNumber - 2;
        var multiplier = Math.pow(backoffMultiplier, exponent);
        var delayMillis = Math.min(maxDelay.toMillis(), Math.round(initialDelay.toMillis() * multiplier));
        return Duration.ofMillis(Math.max(1, delayMillis));
    }

    private static void requirePositive(Duration value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }
}
