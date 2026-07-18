package net.macestudios.macetpa.model;

import org.bukkit.Location;

import java.util.UUID;

public final class ActiveTeleport {
    private final UUID playerId;
    private final UUID payerId;
    private final Location origin;
    private final Location destination;
    private final TeleportKind kind;
    private final String successMessageKey;
    private final String economyCommand;
    private final boolean cancelOnMove;
    private final boolean saveOriginAsBack;
    private int remainingSeconds;

    public ActiveTeleport(UUID playerId, UUID payerId, Location origin, Location destination, TeleportKind kind, String successMessageKey, String economyCommand, boolean cancelOnMove, boolean saveOriginAsBack, int remainingSeconds) {
        this.playerId = playerId;
        this.payerId = payerId;
        this.origin = origin;
        this.destination = destination;
        this.kind = kind;
        this.successMessageKey = successMessageKey;
        this.economyCommand = economyCommand;
        this.cancelOnMove = cancelOnMove;
        this.saveOriginAsBack = saveOriginAsBack;
        this.remainingSeconds = remainingSeconds;
    }

    public UUID playerId() {
        return playerId;
    }

    public UUID payerId() {
        return payerId;
    }

    public Location origin() {
        return origin;
    }

    public Location destination() {
        return destination;
    }

    public TeleportKind kind() {
        return kind;
    }

    public String successMessageKey() {
        return successMessageKey;
    }

    public String economyCommand() {
        return economyCommand;
    }

    public boolean cancelOnMove() {
        return cancelOnMove;
    }

    public boolean saveOriginAsBack() {
        return saveOriginAsBack;
    }

    public int remainingSeconds() {
        return remainingSeconds;
    }

    public void decrementRemainingSeconds() {
        remainingSeconds--;
    }
}
