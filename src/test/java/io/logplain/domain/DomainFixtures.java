package io.logplain.domain;

import java.time.Duration;
import java.util.Set;

final class DomainFixtures {
    private DomainFixtures() {
    }

    static QuotaPolicy quotaPolicy() {
        return new QuotaPolicy(
                100,
                Duration.ofMinutes(1),
                10,
                20,
                1_000_000,
                25,
                Duration.ofDays(30),
                Duration.ofMinutes(5));
    }

    static Tenant tenant() {
        return Tenant.provision(TenantId.random(), "Acme", quotaPolicy()).transitionTo(TenantStatus.ACTIVE);
    }

    static TenantContext context(Tenant tenant) {
        return TenantContext.tenant(tenant.tenantId(), "client-1", Set.of(
                Permission.ACCEPT_REQUEST,
                Permission.EXECUTE_FLOW,
                Permission.CALL_DESTINATION,
                Permission.READ_TRANSACTION));
    }

    static Endpoint endpoint(Tenant tenant) {
        return new Endpoint(
                tenant.tenantId(),
                EndpointId.random(),
                "orders",
                RestResponseMode.ASYNC,
                new FlowVersion(FlowId.random(), Version.parse("1.0.0")),
                true);
    }
}
