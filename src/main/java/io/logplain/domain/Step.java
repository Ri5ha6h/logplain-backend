package io.logplain.domain;

public interface Step<I, O> {
    StepId id();

    O execute(I input, StepExecutionContext context);
}
