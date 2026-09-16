package io.logplain.domain;

public class DomainException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final DomainError error;

    public DomainException(DomainError error) {
        super(error.code() + ": " + error.safeMessage());
        this.error = error;
    }

    public DomainError error() {
        return error;
    }

    public static DomainException invalid(String code, String message) {
        return new DomainException(new DomainError(ErrorCategory.VALIDATION, code, message));
    }

    public static DomainException unauthorized(String code, String message) {
        return new DomainException(new DomainError(ErrorCategory.AUTHORIZATION, code, message));
    }
}
