package io.logplain.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

public record QuotaPolicy(
        int maxRequestsPerWindow,
        Duration requestWindow,
        int maxConcurrentTransactions,
        int maxQueueDepth,
        long maxPayloadBytes,
        int maxOutboundCallsPerTransaction,
        Duration maxRetention,
        Duration maxExtensionRuntime) {

    public QuotaPolicy {
        if (maxRequestsPerWindow <= 0 || maxConcurrentTransactions <= 0 || maxQueueDepth < 0
                || maxPayloadBytes <= 0 || maxOutboundCallsPerTransaction < 0) {
            throw new IllegalArgumentException("Quota limits are invalid");
        }
        requirePositive(requestWindow, "requestWindow");
        requirePositive(maxRetention, "maxRetention");
        requirePositive(maxExtensionRuntime, "maxExtensionRuntime");
    }

    public QuotaDecision check(QuotaUsage usage) {
        Objects.requireNonNull(usage, "usage");
        var violations = new ArrayList<QuotaViolation>();
        addIfExceeded(violations, QuotaName.REQUEST_RATE, maxRequestsPerWindow, usage.requestsInWindow());
        addIfExceeded(violations, QuotaName.CONCURRENT_TRANSACTIONS,
                maxConcurrentTransactions, usage.concurrentTransactions());
        addIfExceeded(violations, QuotaName.QUEUE_DEPTH, maxQueueDepth, usage.queueDepth());
        addIfExceeded(violations, QuotaName.PAYLOAD_SIZE, maxPayloadBytes, usage.payloadBytes());
        addIfExceeded(violations, QuotaName.OUTBOUND_CALLS,
                maxOutboundCallsPerTransaction, usage.outboundCalls());
        if (usage.retention().compareTo(maxRetention) > 0) {
            addIfExceeded(violations, QuotaName.RETENTION,
                    maxRetention.toSeconds(), usage.retention().toSeconds());
        }
        if (usage.extensionRuntime().compareTo(maxExtensionRuntime) > 0) {
            addIfExceeded(violations, QuotaName.EXTENSION_RUNTIME,
                    maxExtensionRuntime.toMillis(), usage.extensionRuntime().toMillis());
        }
        return violations.isEmpty() ? QuotaDecision.allowedDecision() : QuotaDecision.rejected(violations);
    }

    private static void addIfExceeded(
            ArrayList<QuotaViolation> violations, QuotaName quota, long limit, long observed) {
        if (observed > limit) {
            violations.add(new QuotaViolation(quota, limit, observed));
        }
    }

    private static void requirePositive(Duration value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }
}
