package io.logplain.domain.extension;

import io.logplain.domain.TenantId;

import java.util.Objects;
import java.util.Set;

public sealed interface ExtensionVisibility
        permits ExtensionVisibility.PlatformShared, ExtensionVisibility.TenantSpecific {

    boolean visibleTo(TenantId tenantId);

    record PlatformShared(Set<TenantId> grantedTenantIds) implements ExtensionVisibility {
        public PlatformShared {
            grantedTenantIds = Set.copyOf(Objects.requireNonNull(grantedTenantIds, "grantedTenantIds"));
        }

        @Override
        public boolean visibleTo(TenantId tenantId) {
            return grantedTenantIds.contains(tenantId);
        }

        public PlatformShared grantTo(TenantId tenantId) {
            Objects.requireNonNull(tenantId, "tenantId");
            var grants = new java.util.HashSet<>(grantedTenantIds);
            grants.add(tenantId);
            return new PlatformShared(grants);
        }
    }

    record TenantSpecific(TenantId tenantId) implements ExtensionVisibility {
        public TenantSpecific {
            Objects.requireNonNull(tenantId, "tenantId");
        }

        @Override
        public boolean visibleTo(TenantId tenantId) {
            return this.tenantId.equals(tenantId);
        }
    }
}
