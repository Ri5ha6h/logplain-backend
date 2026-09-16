package io.logplain.domain;

import java.util.Objects;

public record FlowVersion(FlowId flowId, Version version) {
    public FlowVersion {
        Objects.requireNonNull(flowId, "flowId");
        Objects.requireNonNull(version, "version");
    }

    @Override
    public String toString() {
        return "%s@%s".formatted(flowId, version);
    }
}
