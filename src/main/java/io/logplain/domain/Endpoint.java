package io.logplain.domain;

import java.util.Objects;

public record Endpoint(
        TenantId tenantId,
        EndpointId id,
        String name,
        RestResponseMode responseMode,
        FlowVersion flowVersion,
        boolean published) implements TenantOwned {

    public Endpoint {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        Objects.requireNonNull(responseMode, "responseMode");
        Objects.requireNonNull(flowVersion, "flowVersion");
    }
}
