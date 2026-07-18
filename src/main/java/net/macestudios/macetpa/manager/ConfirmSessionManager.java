package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.model.ConfirmSession;
import net.macestudios.macetpa.model.RequestType;

import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfirmSessionManager {
    private final Clock clock;
    private final Map<UUID, ConfirmSession> sessions = new ConcurrentHashMap<>();
    private int expireSeconds;

    public ConfirmSessionManager(Clock clock, int expireSeconds) {
        this.clock = clock;
        this.expireSeconds = expireSeconds;
    }

    public ConfirmSession create(UUID sender, UUID target, RequestType type) {
        removeExpiredSessions();
        Instant now = Instant.now(clock);
        ConfirmSession session = new ConfirmSession(sender, target, type, now, now.plusSeconds(expireSeconds));
        sessions.put(sender, session);
        return session;
    }

    public Optional<ConfirmSession> get(UUID sender) {
        removeExpiredSessions();
        return Optional.ofNullable(sessions.get(sender));
    }

    public Optional<ConfirmSession> consume(UUID sender) {
        removeExpiredSessions();
        return Optional.ofNullable(sessions.remove(sender));
    }

    public void removePlayer(UUID playerId) {
        sessions.values().removeIf(session -> session.sender().equals(playerId) || session.target().equals(playerId));
    }

    public int remainingSeconds(UUID sender) {
        return get(sender)
                .map(session -> Math.max(1, session.expiresAt().getEpochSecond() - Instant.now(clock).getEpochSecond()))
                .orElse(0L)
                .intValue();
    }

    public void updateSettings(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public void clear() {
        sessions.clear();
    }

    private void removeExpiredSessions() {
        Iterator<Map.Entry<UUID, ConfirmSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired(clock)) {
                iterator.remove();
            }
        }
    }
}
