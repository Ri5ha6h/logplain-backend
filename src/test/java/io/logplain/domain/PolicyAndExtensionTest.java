package io.logplain.domain;

import io.logplain.domain.extension.ExtensionDescriptor;
import io.logplain.domain.extension.ExtensionKind;
import io.logplain.domain.extension.ExtensionVisibility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyAndExtensionTest {
    @Test
    void retryPolicyDoesNotRetryUncertainDelivery() {
        var policy = new RetryPolicy(3, Duration.ofSeconds(1), 2, Duration.ofSeconds(10),
                Set.of(ErrorCategory.TRANSIENT, ErrorCategory.TIMEOUT));

        assertTrue(policy.shouldRetry(
                new DomainError(ErrorCategory.TRANSIENT, "TEMPORARY", "Temporary failure"), 1));
        assertFalse(policy.shouldRetry(
                new DomainError(ErrorCategory.UNCERTAIN_DELIVERY, "UNKNOWN", "Delivery is unknown"), 1));
        assertFalse(policy.shouldRetry(
                new DomainError(ErrorCategory.TRANSIENT, "TEMPORARY", "Temporary failure"), 3));
        assertEquals(Duration.ofSeconds(2), policy.delayForAttempt(3));
    }

    @Test
    void extensionVisibilityRequiresAnExplicitTenantGrant() {
        var first = DomainFixtures.tenant();
        var second = DomainFixtures.tenant();
        var shared = new ExtensionDescriptor(
                ExtensionId.random(), "shared-step", Version.parse("1.2.0"), ExtensionKind.STEP,
                new ExtensionVisibility.PlatformShared(Set.of(first.tenantId())));
        var specific = new ExtensionDescriptor(
                ExtensionId.random(), "tenant-step", Version.parse("1.0.0"), ExtensionKind.STEP,
                new ExtensionVisibility.TenantSpecific(first.tenantId()));

        assertTrue(shared.visibleTo(first.tenantId()));
        assertFalse(shared.visibleTo(second.tenantId()));
        assertTrue(specific.visibleTo(first.tenantId()));
        assertFalse(specific.visibleTo(second.tenantId()));
    }

    private static void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
