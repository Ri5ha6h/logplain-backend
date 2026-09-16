package io.logplain.domain;

public enum TransactionState {
    ACCEPTED,
    RUNNING,
    WAITING,
    SUCCEEDED,
    REJECTED,
    FAILED,
    DEAD_LETTERED,
    CANCELLED,
    TIMED_OUT
}
