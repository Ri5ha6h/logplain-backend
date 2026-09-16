package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record FlowId(UUID value) implements Identifier {
    public FlowId {
        Objects.requireNonNull(value, "value");
    }

    public static FlowId random() {
        return new FlowId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
