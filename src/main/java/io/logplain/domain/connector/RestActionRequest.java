package io.logplain.domain.connector;

import java.util.Map;
import java.util.Objects;

public record RestActionRequest(HttpMethod method, Map<String, String> headers, byte[] body) {
    public RestActionRequest {
        Objects.requireNonNull(method, "method");
        headers = Map.copyOf(Objects.requireNonNull(headers, "headers"));
        body = body == null ? new byte[0] : body.clone();
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
