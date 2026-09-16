package io.logplain.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Transaction implements TenantOwned {
    private final TransactionId id;
    private final TenantId tenantId;
    private final EndpointId endpointId;
    private final FlowVersion flowVersion;
    private final Instant createdAt;
    private TransactionState state;
    private Instant updatedAt;

    private Transaction(
            TransactionId id,
            TenantId tenantId,
            EndpointId endpointId,
            FlowVersion flowVersion,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.endpointId = Objects.requireNonNull(endpointId, "endpointId");
        this.flowVersion = Objects.requireNonNull(flowVersion, "flowVersion");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = createdAt;
        this.state = TransactionState.ACCEPTED;
    }

    public static Transaction accepted(
            TenantContext context, Endpoint endpoint, TransactionId id, Instant acceptedAt) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(acceptedAt, "acceptedAt");
        context.requirePermission(Permission.ACCEPT_REQUEST);
        endpoint.requireAccess(context);
        if (!endpoint.published()) {
            throw new DomainException(new DomainError(
                    ErrorCategory.CONFLICT, "ENDPOINT_NOT_PUBLISHED", "The Endpoint is not published"));
        }
        return new Transaction(id, endpoint.tenantId(), endpoint.id(), endpoint.flowVersion(), acceptedAt);
    }

    public TransactionId id() {
        return id;
    }

    @Override
    public TenantId tenantId() {
        return tenantId;
    }

    public EndpointId endpointId() {
        return endpointId;
    }

    public FlowVersion flowVersion() {
        return flowVersion;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public TransactionState state() {
        return state;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public boolean isTerminal() {
        return switch (state) {
            case SUCCEEDED, REJECTED, FAILED, DEAD_LETTERED, CANCELLED, TIMED_OUT -> true;
            case ACCEPTED, RUNNING, WAITING -> false;
        };
    }

    public void transitionTo(TransactionState target, Instant changedAt) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(changedAt, "changedAt");
        if (!allowedTargets(state).contains(target)) {
            throw new DomainException(new DomainError(
                    ErrorCategory.CONFLICT,
                    "INVALID_TRANSACTION_STATE_CHANGE",
                    "The Transaction state change is not allowed"));
        }
        if (changedAt.isBefore(updatedAt)) {
            throw DomainException.invalid("TRANSACTION_TIME_MOVED_BACK", "Transaction time cannot move backwards");
        }
        state = target;
        updatedAt = changedAt;
    }

    private static Set<TransactionState> allowedTargets(TransactionState current) {
        return switch (current) {
            case ACCEPTED -> Set.of(TransactionState.RUNNING, TransactionState.REJECTED,
                    TransactionState.FAILED, TransactionState.CANCELLED, TransactionState.TIMED_OUT);
            case RUNNING -> Set.of(TransactionState.WAITING, TransactionState.SUCCEEDED,
                    TransactionState.FAILED, TransactionState.DEAD_LETTERED,
                    TransactionState.CANCELLED, TransactionState.TIMED_OUT);
            case WAITING -> Set.of(TransactionState.RUNNING, TransactionState.SUCCEEDED,
                    TransactionState.FAILED, TransactionState.DEAD_LETTERED,
                    TransactionState.CANCELLED, TransactionState.TIMED_OUT);
            case SUCCEEDED, REJECTED, FAILED, DEAD_LETTERED, CANCELLED, TIMED_OUT -> Set.of();
        };
    }
}
