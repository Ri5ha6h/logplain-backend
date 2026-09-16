package io.logplain.domain.connector;

import io.logplain.domain.RestDestinationId;
import io.logplain.domain.RetryPolicy;
import io.logplain.domain.TenantId;
import io.logplain.domain.TenantOwned;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public record RestDestination(
        TenantId tenantId,
        RestDestinationId id,
        URI uri,
        Set<HttpMethod> allowedMethods,
        String secretReference,
        Duration timeout,
        RetryPolicy retryPolicy) implements TenantOwned {
    public RestDestination {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(uri, "uri");
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("uri must be absolute");
        }
        allowedMethods = Set.copyOf(Objects.requireNonNull(allowedMethods, "allowedMethods"));
        if (allowedMethods.isEmpty()) {
            throw new IllegalArgumentException("allowedMethods cannot be empty");
        }
        if (secretReference == null || secretReference.isBlank()) {
            throw new IllegalArgumentException("secretReference cannot be blank");
        }
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        Objects.requireNonNull(retryPolicy, "retryPolicy");
    }
}
