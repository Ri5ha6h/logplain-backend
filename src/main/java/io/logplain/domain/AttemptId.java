package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record AttemptId(UUID value) implements Identifier {
    public AttemptId {
        Objects.requireNonNull(value, "value");
    }

    public static AttemptId random() {
        return new AttemptId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
