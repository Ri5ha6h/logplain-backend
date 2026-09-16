package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) implements Identifier {
    public TenantId {
        Objects.requireNonNull(value, "value");
    }

    public static TenantId random() {
        return new TenantId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
