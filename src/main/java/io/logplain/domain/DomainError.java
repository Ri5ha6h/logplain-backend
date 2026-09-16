package io.logplain.domain;

import java.io.Serializable;
import java.util.Objects;

public record DomainError(ErrorCategory category, String code, String safeMessage) implements Serializable {
    public DomainError {
        Objects.requireNonNull(category, "category");
        requireText(code, "code");
        requireText(safeMessage, "safeMessage");
    }

    private static void requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }
}
