package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.model.BackLocation;
import org.bukkit.Location;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BackManager {
    private final Clock clock;
    private final Map<UUID, BackLocation> locations = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    public BackManager(Clock clock) {
        this.clock = clock;
    }

    public void saveBackLocation(UUID playerId, Location location, int expireSeconds) {
        Instant now = Instant.now(clock);
        locations.put(playerId, new BackLocation(location == null ? null : location.clone(), now, now.plusSeconds(Math.max(1, expireSeconds))));
    }

    public Optional<BackLocation> getBackLocation(UUID playerId) {
        BackLocation backLocation = locations.get(playerId);
        if (backLocation == null) {
            return Optional.empty();
        }
        if (backLocation.isExpired(clock)) {
            locations.remove(playerId);
            return Optional.empty();
        }
        return Optional.of(backLocation);
    }

    public boolean hasUsableBackLocation(UUID playerId) {
        return getBackLocation(playerId).isPresent();
    }

    public boolean hasStoredBackLocation(UUID playerId) {
        return locations.containsKey(playerId);
    }

    public boolean isExpired(UUID playerId) {
        BackLocation backLocation = locations.get(playerId);
        return backLocation != null && backLocation.isExpired(clock);
    }

    public boolean canUseBack(UUID playerId) {
        Instant until = cooldowns.get(playerId);
        if (until == null) {
            return true;
        }
        if (!Instant.now(clock).isBefore(until)) {
            cooldowns.remove(playerId);
            return true;
        }
        return false;
    }

    public long cooldownRemaining(UUID playerId) {
        if (canUseBack(playerId)) {
            return 0;
        }
        return Math.max(1, cooldowns.get(playerId).getEpochSecond() - Instant.now(clock).getEpochSecond());
    }

    public void markUsed(UUID playerId, int cooldownSeconds) {
        cooldowns.put(playerId, Instant.now(clock).plusSeconds(Math.max(1, cooldownSeconds)));
    }

    public void clear(UUID playerId) {
        locations.remove(playerId);
        cooldowns.remove(playerId);
    }
}
