package io.logplain.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionAndAttemptTest {
    @Test
    void transactionUsesEndpointFlowVersionAndEnforcesTerminalState() {
        var tenant = DomainFixtures.tenant();
        var endpoint = DomainFixtures.endpoint(tenant);
        var time = Instant.parse("2026-01-01T00:00:00Z");
        var transaction = Transaction.accepted(
                DomainFixtures.context(tenant), endpoint, TransactionId.random(), time);

        assertEquals(endpoint.flowVersion(), transaction.flowVersion());
        transaction.transitionTo(TransactionState.RUNNING, time.plusSeconds(1));
        transaction.transitionTo(TransactionState.SUCCEEDED, time.plusSeconds(2));
        assertThrows(DomainException.class,
                () -> transaction.transitionTo(TransactionState.RUNNING, time.plusSeconds(3)));
    }

    @Test
    void retryCreatesAnotherAttemptUnderTheSameStep() {
        var tenant = DomainFixtures.tenant();
        var transaction = Transaction.accepted(
                DomainFixtures.context(tenant), DomainFixtures.endpoint(tenant),
                TransactionId.random(), Instant.now());
        var execution = new StepExecution(tenant.tenantId(), transaction.id(), StepId.random());
        execution.transitionTo(StepState.RUNNING);

        var first = execution.startAttempt(AttemptId.random(), Instant.now());
        execution.recordAttemptOutcome(first.id(), AttemptState.RETRY_SCHEDULED, Instant.now(),
                new DomainError(ErrorCategory.TRANSIENT, "REMOTE_BUSY", "The remote service is busy"));
        var second = execution.startAttempt(AttemptId.random(), Instant.now());
        execution.recordAttemptOutcome(second.id(), AttemptState.SUCCEEDED, Instant.now(), null);

        assertEquals(2, execution.attempts().size());
        assertEquals(1, execution.attempts().getFirst().number());
        assertEquals(2, execution.attempts().getLast().number());
        assertEquals(StepState.SUCCEEDED, execution.state());
    }

    @Test
    void uncertainAttemptMovesStepToWaiting() {
        var tenant = DomainFixtures.tenant();
        var transaction = Transaction.accepted(
                DomainFixtures.context(tenant), DomainFixtures.endpoint(tenant),
                TransactionId.random(), Instant.now());
        var execution = new StepExecution(tenant.tenantId(), transaction.id(), StepId.random());
        execution.transitionTo(StepState.RUNNING);
        var attempt = execution.startAttempt(AttemptId.random(), Instant.now());

        execution.recordAttemptOutcome(attempt.id(), AttemptState.UNCERTAIN, Instant.now(),
                new DomainError(ErrorCategory.UNCERTAIN_DELIVERY, "DELIVERY_UNKNOWN", "External delivery is unknown"));

        assertEquals(StepState.WAITING, execution.state());
    }
}
