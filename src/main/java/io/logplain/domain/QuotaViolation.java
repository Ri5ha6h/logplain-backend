package io.logplain.domain;

public record QuotaViolation(QuotaName quota, long limit, long observed) {
    public QuotaViolation {
        if (limit < 0 || observed < 0) {
            throw new IllegalArgumentException("Quota values cannot be negative");
        }
    }
}
