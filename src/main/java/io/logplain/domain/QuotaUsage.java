package io.logplain.domain;

import java.time.Duration;

public record QuotaUsage(
        int requestsInWindow,
        int concurrentTransactions,
        int queueDepth,
        long payloadBytes,
        int outboundCalls,
        Duration retention,
        Duration extensionRuntime) {

    public QuotaUsage {
        if (requestsInWindow < 0 || concurrentTransactions < 0 || queueDepth < 0
                || payloadBytes < 0 || outboundCalls < 0) {
            throw new IllegalArgumentException("Quota usage counters cannot be negative");
        }
        if (retention == null || retention.isNegative() || extensionRuntime == null || extensionRuntime.isNegative()) {
            throw new IllegalArgumentException("Quota durations cannot be negative");
        }
    }
}
