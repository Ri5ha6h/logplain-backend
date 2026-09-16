package io.logplain.domain;

public enum AttemptState {
    RUNNING,
    SUCCEEDED,
    RETRY_SCHEDULED,
    FAILED,
    TIMED_OUT,
    UNCERTAIN,
    CANCELLED
}
