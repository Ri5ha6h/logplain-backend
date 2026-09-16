package io.logplain.domain;

import java.util.Objects;
import java.util.UUID;

public record ExtensionId(UUID value) implements Identifier {
    public ExtensionId {
        Objects.requireNonNull(value, "value");
    }

    public static ExtensionId random() {
        return new ExtensionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
