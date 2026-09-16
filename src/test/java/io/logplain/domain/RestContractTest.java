package io.logplain.domain;

import io.logplain.domain.connector.AuthenticatedRestRequest;
import io.logplain.domain.connector.HttpMethod;
import io.logplain.domain.connector.RestRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestContractTest {
    @Test
    void requestTenantHintMustMatchAuthenticatedTenant() {
        var first = DomainFixtures.tenant();
        var second = DomainFixtures.tenant();
        var request = new RestRequest(
                HttpMethod.POST,
                "/orders",
                Map.of("X-Request-Id", List.of("request-1")),
                new byte[] {1},
                Optional.of(second.tenantId().toString()),
                Optional.empty());

        assertThrows(DomainException.class,
                () -> new AuthenticatedRestRequest(request, DomainFixtures.context(first)));
    }

    @Test
    void idempotencyScopeContainsTenantEndpointAndClientKey() {
        var tenant = DomainFixtures.tenant();
        var firstEndpoint = DomainFixtures.endpoint(tenant);
        var secondEndpoint = DomainFixtures.endpoint(tenant);
        var first = IdempotencyScope.from(DomainFixtures.context(tenant), firstEndpoint.id(), "same-key");
        var second = IdempotencyScope.from(DomainFixtures.context(tenant), secondEndpoint.id(), "same-key");

        assertEquals(tenant.tenantId(), first.tenantId());
        assertEquals(firstEndpoint.id(), first.endpointId());
        assertEquals("same-key", first.clientKey());
        org.junit.jupiter.api.Assertions.assertNotEquals(first, second);
    }
}
