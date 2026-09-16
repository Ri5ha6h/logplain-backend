package io.logplain.domain;

public enum ErrorCategory {
    AUTHENTICATION,
    AUTHORIZATION,
    VALIDATION,
    CONFLICT,
    NOT_FOUND,
    QUOTA_EXCEEDED,
    RATE_LIMITED,
    TRANSIENT,
    TIMEOUT,
    UNCERTAIN_DELIVERY,
    FATAL
}
