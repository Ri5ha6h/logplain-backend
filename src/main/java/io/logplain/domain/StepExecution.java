package io.logplain.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StepExecution implements TenantOwned {
    private final TenantId tenantId;
    private final TransactionId transactionId;
    private final StepId stepId;
    private final List<Attempt> attempts = new ArrayList<>();
    private StepState state = StepState.PENDING;

    public StepExecution(TenantId tenantId, TransactionId transactionId, StepId stepId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.stepId = Objects.requireNonNull(stepId, "stepId");
    }

    @Override
    public TenantId tenantId() {
        return tenantId;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public StepId stepId() {
        return stepId;
    }

    public StepState state() {
        return state;
    }

    public List<Attempt> attempts() {
        return List.copyOf(attempts);
    }

    public void transitionTo(StepState target) {
        Objects.requireNonNull(target, "target");
        var allowed = switch (state) {
            case PENDING -> List.of(StepState.RUNNING, StepState.CANCELLED);
            case RUNNING -> List.of(StepState.WAITING, StepState.SUCCEEDED, StepState.FAILED,
                    StepState.DEAD_LETTERED, StepState.CANCELLED, StepState.TIMED_OUT);
            case WAITING -> List.of(StepState.RUNNING, StepState.SUCCEEDED, StepState.FAILED,
                    StepState.DEAD_LETTERED, StepState.CANCELLED, StepState.TIMED_OUT);
            case SUCCEEDED, FAILED, DEAD_LETTERED, CANCELLED, TIMED_OUT -> List.of();
        };
        if (!allowed.contains(target)) {
            throw new DomainException(new DomainError(
                    ErrorCategory.CONFLICT, "INVALID_STEP_STATE_CHANGE", "The Step state change is not allowed"));
        }
        state = target;
    }

    public Attempt startAttempt(AttemptId attemptId, Instant startedAt) {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(startedAt, "startedAt");
        if (state != StepState.RUNNING) {
            throw DomainException.invalid("STEP_NOT_RUNNING", "A Step must be RUNNING before an Attempt starts");
        }
        if (!attempts.isEmpty() && attempts.getLast().state() == AttemptState.RUNNING) {
            throw DomainException.invalid("ATTEMPT_ALREADY_RUNNING", "A Step cannot have two running Attempts");
        }
        var attempt = Attempt.start(attemptId, tenantId, transactionId, stepId,
                attempts.size() + 1, startedAt);
        attempts.add(attempt);
        return attempt;
    }

    public void recordAttemptOutcome(
            AttemptId attemptId, AttemptState outcome, Instant finishedAt, DomainError error) {
        var index = findAttempt(attemptId);
        var finished = attempts.get(index).finish(outcome, finishedAt, error);
        attempts.set(index, finished);
        var nextState = switch (outcome) {
            case SUCCEEDED -> StepState.SUCCEEDED;
            case RETRY_SCHEDULED -> StepState.RUNNING;
            case UNCERTAIN -> StepState.WAITING;
            case FAILED -> StepState.FAILED;
            case TIMED_OUT -> StepState.TIMED_OUT;
            case CANCELLED -> StepState.CANCELLED;
            case RUNNING -> throw DomainException.invalid("ATTEMPT_OUTCOME_REQUIRED", "An Attempt outcome is required");
        };
        if (state != nextState) {
            transitionTo(nextState);
        }
    }

    private int findAttempt(AttemptId attemptId) {
        for (var index = 0; index < attempts.size(); index++) {
            if (attempts.get(index).id().equals(attemptId)) {
                return index;
            }
        }
        throw new DomainException(new DomainError(
                ErrorCategory.NOT_FOUND, "ATTEMPT_NOT_FOUND", "The Attempt does not belong to this Step"));
    }
}
