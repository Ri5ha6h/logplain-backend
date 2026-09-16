package io.logplain.domain;

public enum StepState {
    PENDING,
    RUNNING,
    WAITING,
    SUCCEEDED,
    FAILED,
    DEAD_LETTERED,
    CANCELLED,
    TIMED_OUT
}
