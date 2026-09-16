package io.logplain.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantAndQuotaTest {
    @Test
    void tenantLifecycleAllowsOnlyDocumentedTransitions() {
        var tenant = DomainFixtures.tenant();

        assertEquals(TenantStatus.ACTIVE, tenant.status());
        assertTrue(tenant.acceptsIntake());
        assertEquals(TenantStatus.SUSPENDED, tenant.transitionTo(TenantStatus.SUSPENDED).status());
        assertThrows(DomainException.class, () -> tenant.transitionTo(TenantStatus.DELETED));
    }

    @Test
    void quotaDecisionReportsAllExceededLimits() {
        var policy = new QuotaPolicy(10, Duration.ofMinutes(1), 2, 3, 1000, 4,
                Duration.ofDays(7), Duration.ofSeconds(30));
        var decision = policy.check(new QuotaUsage(11, 3, 4, 1001, 5,
                Duration.ofDays(8), Duration.ofSeconds(31)));

        assertFalse(decision.allowed());
        assertEquals(Set.of(
                QuotaName.REQUEST_RATE,
                QuotaName.CONCURRENT_TRANSACTIONS,
                QuotaName.QUEUE_DEPTH,
                QuotaName.PAYLOAD_SIZE,
                QuotaName.OUTBOUND_CALLS,
                QuotaName.RETENTION,
                QuotaName.EXTENSION_RUNTIME),
                decision.violations().stream().map(QuotaViolation::quota).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void tenantContextBlocksAnotherTenantButPlatformScopeCanAccess() {
        var first = DomainFixtures.tenant();
        var second = DomainFixtures.tenant();
        var firstContext = DomainFixtures.context(first);

        assertFalse(firstContext.canAccess(second.tenantId()));
        assertThrows(DomainException.class, () -> firstContext.requireAccess(second.tenantId()));
        assertTrue(TenantContext.platform(first.tenantId(), "platform-admin", Set.of())
                .canAccess(second.tenantId()));
    }
}
