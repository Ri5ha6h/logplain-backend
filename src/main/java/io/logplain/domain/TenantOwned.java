package io.logplain.domain;

public interface TenantOwned {
    TenantId tenantId();

    default void requireAccess(TenantContext context) {
        context.requireAccess(tenantId());
    }
}
