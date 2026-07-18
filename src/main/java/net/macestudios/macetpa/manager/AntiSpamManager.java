package net.macestudios.macetpa.manager;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiSpamManager {
    private final Clock clock;
    private final Map<UUID, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> blockedUntil = new ConcurrentHashMap<>();

    public AntiSpamManager(Clock clock) {
        this.clock = clock;
    }

    public Result recordRequest(UUID playerId, boolean enabled, int maxPerMinute, int temporaryBlockSeconds) {
        if (!enabled) {
            return Result.permitted();
        }
        Instant now = Instant.now(clock);
        Instant blocked = blockedUntil.get(playerId);
        if (blocked != null && now.isBefore(blocked)) {
            return Result.blocked(Math.max(1, blocked.getEpochSecond() - now.getEpochSecond()), false);
        }
        blockedUntil.remove(playerId);
        Deque<Instant> playerAttempts = attempts.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        playerAttempts.removeIf(instant -> instant.plusSeconds(60).isBefore(now));
        if (playerAttempts.size() >= Math.max(1, maxPerMinute)) {
            Instant until = now.plusSeconds(Math.max(1, temporaryBlockSeconds));
            blockedUntil.put(playerId, until);
            playerAttempts.clear();
            return Result.blocked(temporaryBlockSeconds, true);
        }
        playerAttempts.addLast(now);
        return Result.permitted();
    }

    public void clear(UUID playerId) {
        attempts.remove(playerId);
        blockedUntil.remove(playerId);
    }

    public record Result(boolean allowed, long remainingSeconds, boolean newlyBlocked) {
        static Result permitted() {
            return new Result(true, 0, false);
        }

        static Result blocked(long remainingSeconds, boolean newlyBlocked) {
            return new Result(false, remainingSeconds, newlyBlocked);
        }
    }
}
