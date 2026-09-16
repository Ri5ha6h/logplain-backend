package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record EndpointId(UUID value) implements Identifier {
    public EndpointId {
        Objects.requireNonNull(value, "value");
    }

    public static EndpointId random() {
        return new EndpointId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
