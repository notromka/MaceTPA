package net.macestudios.macetpa.util;

import java.util.Optional;
import java.util.function.Predicate;

public final class SoundKeyResolver {
    private SoundKeyResolver() {
    }

    public static Optional<String> resolve(String configured, Predicate<String> exists) {
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }
        for (String candidate : configured.split(",")) {
            String normalized = normalize(candidate);
            if (!normalized.isBlank() && exists.test(normalized)) {
                return Optional.of(normalized);
            }
        }
        return Optional.empty();
    }

    private static String normalize(String candidate) {
        return candidate == null ? "" : candidate.trim().replace('.', '_').replace('-', '_').toUpperCase();
    }
}
