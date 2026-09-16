package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record RestDestinationId(UUID value) implements Identifier {
    public RestDestinationId {
        Objects.requireNonNull(value, "value");
    }

    public static RestDestinationId random() {
        return new RestDestinationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
