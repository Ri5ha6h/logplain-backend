package io.logplain.domain.connector;

import io.logplain.domain.DomainException;
import io.logplain.domain.TenantContext;

import java.util.Objects;
import java.util.UUID;

public record AuthenticatedRestRequest(RestRequest request, TenantContext tenantContext) {
    public AuthenticatedRestRequest {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(tenantContext, "tenantContext");
        request.tenantHint().ifPresent(hint -> {
            UUID hintedTenant;
            try {
                hintedTenant = UUID.fromString(hint);
            } catch (IllegalArgumentException exception) {
                throw DomainException.invalid("INVALID_TENANT_HINT", "The request Tenant hint is invalid");
            }
            if (!tenantContext.tenantId().value().equals(hintedTenant)) {
                throw DomainException.unauthorized(
                        "TENANT_SCOPE_MISMATCH", "The request Tenant hint does not match the authenticated Tenant");
            }
        });
    }
}
