package io.logplain.domain;

import java.util.Objects;

/** The uniqueness scope is exactly Tenant + Endpoint + client key. */
public record IdempotencyScope(TenantId tenantId, EndpointId endpointId, String clientKey) implements TenantOwned {
    public IdempotencyScope {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(endpointId, "endpointId");
        if (clientKey == null || clientKey.isBlank() || clientKey.length() > 256) {
            throw new IllegalArgumentException("clientKey must contain 1 to 256 characters");
        }
    }

    public static IdempotencyScope from(TenantContext context, EndpointId endpointId, String clientKey) {
        Objects.requireNonNull(context, "context");
        return new IdempotencyScope(context.tenantId(), endpointId, clientKey);
    }
}
