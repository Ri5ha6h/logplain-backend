package io.logplain.domain;

import java.util.List;

public record QuotaDecision(boolean allowed, List<QuotaViolation> violations) {
    public QuotaDecision {
        violations = List.copyOf(violations);
        if (allowed && !violations.isEmpty()) {
            throw new IllegalArgumentException("An allowed decision cannot contain violations");
        }
        if (!allowed && violations.isEmpty()) {
            throw new IllegalArgumentException("A rejected decision must contain a violation");
        }
    }

    public static QuotaDecision allowedDecision() {
        return new QuotaDecision(true, List.of());
    }

    public static QuotaDecision rejected(List<QuotaViolation> violations) {
        return new QuotaDecision(false, violations);
    }
}
