package io.logplain.domain.connector;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record RestRequest(
        HttpMethod method,
        String path,
        Map<String, List<String>> headers,
        byte[] body,
        Optional<String> tenantHint,
        Optional<String> clientKey) {
    public RestRequest {
        Objects.requireNonNull(method, "method");
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        headers = copyHeaders(headers);
        body = body == null ? new byte[0] : body.clone();
        tenantHint = Objects.requireNonNull(tenantHint, "tenantHint");
        clientKey = Objects.requireNonNull(clientKey, "clientKey");
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    private static Map<String, List<String>> copyHeaders(Map<String, List<String>> source) {
        Objects.requireNonNull(source, "headers");
        var copy = new HashMap<String, List<String>>();
        source.forEach((name, values) -> {
            Objects.requireNonNull(name, "header name");
            copy.put(name, List.copyOf(Objects.requireNonNull(values, "header values")));
        });
        return Map.copyOf(copy);
    }
}
