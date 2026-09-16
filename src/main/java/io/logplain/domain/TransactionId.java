package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) implements Identifier {
    public TransactionId {
        Objects.requireNonNull(value, "value");
    }

    public static TransactionId random() {
        return new TransactionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
