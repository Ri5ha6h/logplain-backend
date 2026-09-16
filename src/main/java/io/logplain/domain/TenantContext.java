package io.logplain.domain;

import java.util.Objects;
import java.util.Set;

/** Trusted scope created by authentication or a server-managed platform binding. */
public record TenantContext(
        TenantId tenantId,
        String principal,
        Set<Permission> permissions,
        boolean platformScope) {

    public TenantContext {
        Objects.requireNonNull(tenantId, "tenantId");
        if (principal == null || principal.isBlank()) {
            throw new IllegalArgumentException("principal cannot be blank");
        }
        permissions = Set.copyOf(Objects.requireNonNull(permissions, "permissions"));
    }

    public static TenantContext tenant(TenantId tenantId, String principal, Set<Permission> permissions) {
        return new TenantContext(tenantId, principal, permissions, false);
    }

    public static TenantContext platform(TenantId targetTenant, String principal, Set<Permission> permissions) {
        return new TenantContext(targetTenant, principal, permissions, true);
    }

    public boolean canAccess(TenantId resourceTenant) {
        return platformScope || tenantId.equals(resourceTenant);
    }

    public void requireAccess(TenantId resourceTenant) {
        if (!canAccess(resourceTenant)) {
            throw DomainException.unauthorized("TENANT_SCOPE_MISMATCH", "The operation is outside the Tenant scope");
        }
    }

    public void requirePermission(Permission permission) {
        Objects.requireNonNull(permission, "permission");
        if (!permissions.contains(permission)) {
            throw DomainException.unauthorized("PERMISSION_REQUIRED", "The required permission is not present");
        }
    }
}
