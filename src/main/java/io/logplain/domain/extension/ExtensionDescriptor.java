package io.logplain.domain.extension;

import io.logplain.domain.ExtensionId;
import io.logplain.domain.TenantId;
import io.logplain.domain.Version;

import java.util.Objects;

public record ExtensionDescriptor(
        ExtensionId id,
        String name,
        Version version,
        ExtensionKind kind,
        ExtensionVisibility visibility) {
    public ExtensionDescriptor {
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(visibility, "visibility");
    }

    public boolean visibleTo(TenantId tenantId) {
        return visibility.visibleTo(tenantId);
    }
}
