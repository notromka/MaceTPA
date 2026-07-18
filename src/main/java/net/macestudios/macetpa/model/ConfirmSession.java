package net.macestudios.macetpa.model;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record ConfirmSession(
        UUID sender,
        UUID target,
        RequestType type,
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired(Clock clock) {
        return !Instant.now(clock).isBefore(expiresAt);
    }
}
