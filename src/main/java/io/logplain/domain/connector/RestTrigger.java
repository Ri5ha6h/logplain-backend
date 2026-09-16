package io.logplain.domain.connector;

import io.logplain.domain.Endpoint;
import io.logplain.domain.TenantContext;
import io.logplain.domain.TransactionId;

import java.time.Instant;

/** Framework-neutral contract for authenticated Tenant REST intake. */
public interface RestTrigger {
    String name();

    AuthenticatedRestRequest authenticate(RestRequest request);

    TriggerAcceptance accept(
            AuthenticatedRestRequest request,
            Endpoint endpoint,
            TransactionId transactionId,
            Instant acceptedAt);

    record TriggerAcceptance(
            TenantContext tenantContext,
            TransactionId transactionId,
            io.logplain.domain.IdempotencyScope idempotencyScope,
            io.logplain.domain.RestResponseMode responseMode) {
        public TriggerAcceptance {
            java.util.Objects.requireNonNull(tenantContext, "tenantContext");
            java.util.Objects.requireNonNull(transactionId, "transactionId");
            java.util.Objects.requireNonNull(idempotencyScope, "idempotencyScope");
            java.util.Objects.requireNonNull(responseMode, "responseMode");
            if (!tenantContext.tenantId().equals(idempotencyScope.tenantId())) {
                throw io.logplain.domain.DomainException.unauthorized(
                        "TENANT_SCOPE_MISMATCH", "The idempotency scope does not match the authenticated Tenant");
            }
        }
    }
}
