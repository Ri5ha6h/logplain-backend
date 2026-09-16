package io.logplain.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/** Immutable semantic version for a published Flow or Extension. */
public record Version(int major, int minor, int patch) implements Comparable<Version> {
    private static final Pattern VERSION = Pattern.compile("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)");

    public Version {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers cannot be negative");
        }
    }

    public static Version parse(String value) {
        Objects.requireNonNull(value, "value");
        var matcher = VERSION.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Version must use MAJOR.MINOR.PATCH format");
        }
        return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }

    @Override
    public int compareTo(Version other) {
        var majorOrder = Integer.compare(major, other.major);
        if (majorOrder != 0) {
            return majorOrder;
        }
        var minorOrder = Integer.compare(minor, other.minor);
        return minorOrder != 0 ? minorOrder : Integer.compare(patch, other.patch);
    }

    @Override
    public String toString() {
        return "%d.%d.%d".formatted(major, minor, patch);
    }
}
