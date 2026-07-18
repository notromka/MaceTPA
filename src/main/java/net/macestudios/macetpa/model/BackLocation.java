package net.macestudios.macetpa.model;

import org.bukkit.Location;

import java.time.Clock;
import java.time.Instant;

public record BackLocation(Location location, Instant savedAt, Instant expiresAt) {
    public boolean isExpired(Clock clock) {
        return !Instant.now(clock).isBefore(expiresAt);
    }
}
