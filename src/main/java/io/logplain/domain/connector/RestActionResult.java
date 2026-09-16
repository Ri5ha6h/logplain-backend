package io.logplain.domain.connector;

import io.logplain.domain.DomainError;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record RestActionResult(
        Outcome outcome,
        int statusCode,
        Map<String, String> headers,
        byte[] body,
        Optional<DomainError> error) {
    public RestActionResult {
        Objects.requireNonNull(outcome, "outcome");
        if (statusCode < 0 || statusCode > 999) {
            throw new IllegalArgumentException("statusCode is invalid");
        }
        headers = Map.copyOf(Objects.requireNonNull(headers, "headers"));
        body = body == null ? new byte[0] : body.clone();
        error = Objects.requireNonNull(error, "error");
        if (outcome == Outcome.SUCCESS && error.isPresent()) {
            throw new IllegalArgumentException("A successful result cannot contain an error");
        }
        if (outcome != Outcome.SUCCESS && error.isEmpty()) {
            throw new IllegalArgumentException("A non-successful result needs an error");
        }
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    public enum Outcome {
        SUCCESS,
        FAILED,
        UNCERTAIN
    }
}
