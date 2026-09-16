package io.logplain.domain;

import java.util.UUID;

/** Common contract for globally unique domain identifiers. */
public interface Identifier {
    UUID value();
}
