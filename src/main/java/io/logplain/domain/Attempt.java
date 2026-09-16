package io.logplain.domain;

import java.time.Instant;
import java.util.Objects;

public record Attempt(
        AttemptId id,
        TenantId tenantId,
        TransactionId transactionId,
        StepId stepId,
        int number,
        AttemptState state,
        Instant startedAt,
        Instant finishedAt,
        DomainError error) implements TenantOwned {

    public Attempt {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(stepId, "stepId");
        if (number <= 0) {
            throw new IllegalArgumentException("Attempt number must be positive");
        }
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(startedAt, "startedAt");
        if (finishedAt != null && finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Attempt cannot finish before it starts");
        }
        if (state == AttemptState.RUNNING && finishedAt != null) {
            throw new IllegalArgumentException("A running Attempt cannot have a finish time");
        }
    }

    public static Attempt start(
            AttemptId id, TenantId tenantId, TransactionId transactionId, StepId stepId,
            int number, Instant startedAt) {
        return new Attempt(id, tenantId, transactionId, stepId, number,
                AttemptState.RUNNING, startedAt, null, null);
    }

    public Attempt finish(AttemptState outcome, Instant finishedAt, DomainError failure) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(finishedAt, "finishedAt");
        if (state != AttemptState.RUNNING) {
            throw DomainException.invalid("ATTEMPT_ALREADY_FINISHED", "An Attempt can finish only once");
        }
        if (outcome == AttemptState.RUNNING) {
            throw DomainException.invalid("ATTEMPT_OUTCOME_REQUIRED", "An Attempt outcome cannot be RUNNING");
        }
        if (outcome == AttemptState.SUCCEEDED && failure != null) {
            throw DomainException.invalid("SUCCESS_WITH_ERROR", "A successful Attempt cannot contain an error");
        }
        if (outcome != AttemptState.SUCCEEDED && failure == null) {
            throw DomainException.invalid("ERROR_REQUIRED", "A non-successful Attempt needs an error");
        }
        return new Attempt(id, tenantId, transactionId, stepId, number,
                outcome, startedAt, finishedAt, failure);
    }
}
