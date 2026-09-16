package io.logplain.domain;

import java.util.Objects;

public record StepExecutionContext(
        TenantContext tenantContext,
        TransactionId transactionId,
        StepId stepId,
        FlowVersion flowVersion) {
    public StepExecutionContext {
        Objects.requireNonNull(tenantContext, "tenantContext");
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(stepId, "stepId");
        Objects.requireNonNull(flowVersion, "flowVersion");
    }
}
