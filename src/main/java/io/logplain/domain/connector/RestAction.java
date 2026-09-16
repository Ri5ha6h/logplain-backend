package io.logplain.domain.connector;

import io.logplain.domain.TenantContext;

/** Framework-neutral contract for a Tenant-authorized outbound REST call. */
public interface RestAction {
    String name();

    RestActionResult execute(
            RestActionRequest request,
            RestDestination destination,
            TenantContext tenantContext);
}
