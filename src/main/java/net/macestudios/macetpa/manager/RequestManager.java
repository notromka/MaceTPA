package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.model.RequestType;
import net.macestudios.macetpa.model.TeleportRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RequestManager {
    private final Clock clock;
    private final Map<UUID, TeleportRequest> outgoingRequests = new ConcurrentHashMap<>();
    private int expireSeconds;
    private boolean oneRequestAtATime;

    public RequestManager(Clock clock, int expireSeconds, boolean oneRequestAtATime) {
        this.clock = clock;
        this.expireSeconds = expireSeconds;
        this.oneRequestAtATime = oneRequestAtATime;
    }

    public Optional<TeleportRequest> createRequest(UUID sender, UUID receiver, RequestType type) {
        removeExpiredRequests();
        if (oneRequestAtATime && outgoingRequests.containsKey(sender)) {
            return Optional.empty();
        }
        Instant now = Instant.now(clock);
        TeleportRequest request = new TeleportRequest(sender, receiver, type, now, now.plusSeconds(expireSeconds));
        outgoingRequests.put(sender, request);
        return Optional.of(request);
    }

    public Optional<TeleportRequest> getOutgoingRequest(UUID sender) {
        removeExpiredRequests();
        return Optional.ofNullable(outgoingRequests.get(sender));
    }

    public List<TeleportRequest> getIncomingRequests(UUID receiver) {
        removeExpiredRequests();
        return outgoingRequests.values().stream()
                .filter(request -> request.receiver().equals(receiver))
                .toList();
    }

    public Optional<TeleportRequest> resolveIncoming(UUID receiver, UUID sender) {
        removeExpiredRequests();
        List<TeleportRequest> incoming = getIncomingRequests(receiver);
        if (sender == null && incoming.size() != 1) {
            return Optional.empty();
        }
        UUID resolvedSender = sender == null ? incoming.get(0).sender() : sender;
        TeleportRequest request = outgoingRequests.get(resolvedSender);
        if (request == null || !request.receiver().equals(receiver)) {
            return Optional.empty();
        }
        outgoingRequests.remove(resolvedSender);
        return Optional.of(request);
    }

    public Optional<TeleportRequest> cancelOutgoing(UUID sender) {
        return Optional.ofNullable(outgoingRequests.remove(sender));
    }

    public List<TeleportRequest> removePlayer(UUID playerId) {
        List<TeleportRequest> removed = new ArrayList<>();
        Iterator<Map.Entry<UUID, TeleportRequest>> iterator = outgoingRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            TeleportRequest request = iterator.next().getValue();
            if (request.sender().equals(playerId) || request.receiver().equals(playerId)) {
                removed.add(request);
                iterator.remove();
            }
        }
        return removed;
    }

    public List<TeleportRequest> removeExpiredRequests() {
        List<TeleportRequest> expired = new ArrayList<>();
        Iterator<Map.Entry<UUID, TeleportRequest>> iterator = outgoingRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            TeleportRequest request = iterator.next().getValue();
            if (request.isExpired(clock)) {
                expired.add(request);
                iterator.remove();
            }
        }
        return expired;
    }

    public int incomingCount(UUID receiver) {
        return getIncomingRequests(receiver).size();
    }

    public void updateSettings(int expireSeconds, boolean oneRequestAtATime) {
        this.expireSeconds = expireSeconds;
        this.oneRequestAtATime = oneRequestAtATime;
    }

    public void clear() {
        outgoingRequests.clear();
    }

    public int activeCount() {
        removeExpiredRequests();
        return outgoingRequests.size();
    }
}
