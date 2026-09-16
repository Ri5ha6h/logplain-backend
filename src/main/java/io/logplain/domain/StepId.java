package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record StepId(UUID value) implements Identifier {
    public StepId {
        Objects.requireNonNull(value, "value");
    }

    public static StepId random() {
        return new StepId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
