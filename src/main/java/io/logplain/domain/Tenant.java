package io.logplain.domain;

import java.util.Objects;
import java.util.Set;

public final class Tenant implements TenantOwned {
    private static final Set<TenantStatus> PROVISIONING_TARGETS = Set.of(TenantStatus.ACTIVE);
    private static final Set<TenantStatus> ACTIVE_TARGETS = Set.of(TenantStatus.SUSPENDED, TenantStatus.RETIRING);
    private static final Set<TenantStatus> SUSPENDED_TARGETS = Set.of(TenantStatus.ACTIVE, TenantStatus.RETIRING);
    private static final Set<TenantStatus> RETIRING_TARGETS = Set.of(TenantStatus.DELETED);

    private final TenantId id;
    private final String name;
    private final TenantStatus status;
    private final QuotaPolicy quotaPolicy;

    private Tenant(TenantId id, String name, TenantStatus status, QuotaPolicy quotaPolicy) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = requireName(name);
        this.status = Objects.requireNonNull(status, "status");
        this.quotaPolicy = Objects.requireNonNull(quotaPolicy, "quotaPolicy");
    }

    public static Tenant provision(TenantId id, String name, QuotaPolicy quotaPolicy) {
        return new Tenant(id, name, TenantStatus.PROVISIONING, quotaPolicy);
    }

    @Override
    public TenantId tenantId() {
        return id;
    }

    public String name() {
        return name;
    }

    public TenantStatus status() {
        return status;
    }

    public QuotaPolicy quotaPolicy() {
        return quotaPolicy;
    }

    public boolean acceptsIntake() {
        return status == TenantStatus.ACTIVE;
    }

    public Tenant transitionTo(TenantStatus target) {
        Objects.requireNonNull(target, "target");
        var allowed = switch (status) {
            case PROVISIONING -> PROVISIONING_TARGETS;
            case ACTIVE -> ACTIVE_TARGETS;
            case SUSPENDED -> SUSPENDED_TARGETS;
            case RETIRING -> RETIRING_TARGETS;
            case DELETED -> Set.<TenantStatus>of();
        };
        if (!allowed.contains(target)) {
            throw new DomainException(new DomainError(
                    ErrorCategory.CONFLICT,
                    "INVALID_TENANT_STATE_CHANGE",
                    "The Tenant lifecycle state change is not allowed"));
        }
        return new Tenant(id, name, target, quotaPolicy);
    }

    private static String requireName(String value) {
        Objects.requireNonNull(value, "name");
        if (value.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return value;
    }
}
