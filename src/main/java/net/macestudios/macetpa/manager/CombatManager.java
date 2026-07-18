package net.macestudios.macetpa.manager;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatManager {
    private final Clock clock;
    private final Map<UUID, Instant> combatUntil = new ConcurrentHashMap<>();

    public CombatManager(Clock clock) {
        this.clock = clock;
    }

    public void markInCombat(UUID playerId, int seconds) {
        combatUntil.put(playerId, Instant.now(clock).plusSeconds(Math.max(1, seconds)));
    }

    public boolean isInCombat(UUID playerId) {
        Instant until = combatUntil.get(playerId);
        if (until == null) {
            return false;
        }
        if (!Instant.now(clock).isBefore(until)) {
            combatUntil.remove(playerId);
            return false;
        }
        return true;
    }

    public long remainingSeconds(UUID playerId) {
        if (!isInCombat(playerId)) {
            return 0;
        }
        return Math.max(1, combatUntil.get(playerId).getEpochSecond() - Instant.now(clock).getEpochSecond());
    }

    public void clear(UUID playerId) {
        combatUntil.remove(playerId);
    }
}
