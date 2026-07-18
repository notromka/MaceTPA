package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.model.ActiveTeleport;
import net.macestudios.macetpa.model.RequestType;
import net.macestudios.macetpa.model.TeleportKind;
import net.macestudios.macetpa.model.TeleportRequest;
import net.macestudios.macetpa.scheduler.ScheduledTaskHandle;
import net.macestudios.macetpa.scheduler.SchedulerAdapter;
import net.macestudios.macetpa.util.SafeLocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportManager {
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final SoundManager soundManager;
    private final SchedulerAdapter scheduler;
    private final BackManager backManager;
    private final EconomyManager economyManager;
    private final Map<UUID, Countdown> countdowns = new ConcurrentHashMap<>();

    public TeleportManager(ConfigManager configManager, MessageManager messageManager, SoundManager soundManager, SchedulerAdapter scheduler, BackManager backManager, EconomyManager economyManager) {
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.soundManager = soundManager;
        this.scheduler = scheduler;
        this.backManager = backManager;
        this.economyManager = economyManager;
    }

    public void startTeleport(TeleportRequest request, Player sender, Player receiver) {
        Player movingPlayer = request.type() == RequestType.TPA ? sender : receiver;
        Player destinationPlayer = request.type() == RequestType.TPA ? receiver : sender;
        scheduler.runEntityLater(destinationPlayer, () -> {
            Location destination = destinationPlayer.getLocation().clone();
            scheduler.runEntityLater(movingPlayer, () -> {
                int delay = movingPlayer.hasPermission("macetpa.bypass.delay") ? 0 : configManager.teleportDelaySeconds(movingPlayer.getWorld().getName());
                boolean cancelOnMove = configManager.cancelOnMove(movingPlayer.getWorld().getName());
                Map<String, String> placeholders = Map.of("player", sender.getName(), "target", receiver.getName(), "request_type", request.type().name());
                String economyCommand = request.type() == RequestType.TPA ? "tpa" : "tpahere";
                startTeleportToLocation(movingPlayer, sender, destination, TeleportKind.TPA, "teleport-success", economyCommand, delay, cancelOnMove, true, placeholders);
            }, 1L);
        }, 1L);
    }

    public void startBackTeleport(Player player, Location destination) {
        int delay = configManager.backUseTeleportDelay() && !player.hasPermission("macetpa.bypass.delay") ? configManager.teleportDelaySeconds(player.getWorld().getName()) : 0;
        startTeleportToLocation(player, player, destination, TeleportKind.BACK, "back-success", "back", delay, configManager.backCancelOnMove(), configManager.backSaveLocationBeforeBack(), Map.of("player", player.getName(), "target", player.getName(), "request_type", "BACK"));
    }

    public boolean isTeleporting(UUID playerId) {
        return countdowns.containsKey(playerId);
    }

    public ActiveTeleport activeTeleport(UUID playerId) {
        Countdown countdown = countdowns.get(playerId);
        return countdown == null ? null : countdown.teleport;
    }

    public int activeCount() {
        return countdowns.size();
    }

    public void cancel(UUID playerId) {
        Countdown countdown = countdowns.remove(playerId);
        if (countdown != null) {
            countdown.handle.cancel();
        }
    }

    public void cancelWithMessage(Player player, String messageKey) {
        Countdown countdown = countdowns.remove(player.getUniqueId());
        if (countdown == null) {
            return;
        }
        countdown.handle.cancel();
        Player payer = player.getServer().getPlayer(countdown.teleport.payerId());
        if (payer != null) {
            economyManager.refundOnCancel(payer, countdown.teleport.economyCommand());
        }
        messageManager.send(player, messageKey);
        soundManager.play(player, "teleport-cancelled");
    }

    public void cancelAll() {
        countdowns.values().forEach(countdown -> countdown.handle.cancel());
        countdowns.clear();
    }

    private void startTeleportToLocation(Player player, Player payer, Location destination, TeleportKind kind, String successMessageKey, String economyCommand, int delay, boolean cancelOnMove, boolean saveOriginAsBack, Map<String, String> placeholders) {
        if (destination == null || destination.getWorld() == null) {
            messageManager.send(player, "teleport-unsafe");
            soundManager.play(player, "error");
            return;
        }
        Location origin = player.getLocation().clone();
        if (delay <= 0) {
            finishTeleport(player, new ActiveTeleport(player.getUniqueId(), payer.getUniqueId(), origin, destination.clone(), kind, successMessageKey, economyCommand, cancelOnMove, saveOriginAsBack, 0), placeholders);
            return;
        }
        cancel(player.getUniqueId());
        ActiveTeleport teleport = new ActiveTeleport(player.getUniqueId(), payer.getUniqueId(), origin, destination.clone(), kind, successMessageKey, economyCommand, cancelOnMove, saveOriginAsBack, delay);
        Countdown countdown = new Countdown(teleport, delay, placeholders);
        countdowns.put(player.getUniqueId(), countdown);
        ScheduledTaskHandle handle = scheduler.runEntityRepeating(player, () -> tick(countdown, player), 0L, 20L);
        countdown.handle = handle;
    }

    private void tick(Countdown countdown, Player player) {
        if (!player.isOnline()) {
            cancel(player.getUniqueId());
            return;
        }
        if (countdown.teleport.remainingSeconds() <= 0) {
            countdown.handle.cancel();
            countdowns.remove(player.getUniqueId());
            finishTeleport(player, countdown.teleport, countdown.placeholders);
            return;
        }
        Map<String, String> placeholders = new java.util.HashMap<>(countdown.placeholders);
        placeholders.put("time", String.valueOf(countdown.teleport.remainingSeconds()));
        if (configManager.actionbarCountdown()) {
            messageManager.actionbar(player, "teleport-actionbar", placeholders);
        }
        soundManager.play(player, "teleport-countdown-tick");
        soundManager.play(player, "teleport-countdown-tick-2");
        countdown.teleport.decrementRemainingSeconds();
    }

    private void finishTeleport(Player player, ActiveTeleport teleport, Map<String, String> placeholders) {
        Location destination = teleport.destination().clone();
        scheduler.runAtLocation(destination, () -> {
            Location safeDestination = destination;
            if (configManager.safeTeleportEnabled()) {
                safeDestination = SafeLocationUtil.findSafe(destination, configManager.searchNearbySafeLocation(), configManager.safeTeleportRadius());
                if (safeDestination == null) {
                    scheduler.runEntityLater(player, () -> {
                        messageManager.send(player, "teleport-unsafe");
                        soundManager.play(player, "error");
                    }, 1L);
                    return;
                }
            }
            Location finalDestination = safeDestination.clone();
            scheduler.runEntityLater(player, () -> completeTeleport(player, teleport, finalDestination), 1L);
        });
    }

    private void completeTeleport(Player player, ActiveTeleport teleport, Location destination) {
        if (!player.isOnline()) {
            return;
        }
        if (teleport.saveOriginAsBack()) {
            backManager.saveBackLocation(player.getUniqueId(), teleport.origin(), configManager.backExpireAfterSeconds());
        }
        scheduler.teleport(player, destination);
        Player payer = player.getServer().getPlayer(teleport.payerId());
        if (payer != null) {
            economyManager.chargeOnSuccess(payer, teleport.economyCommand());
        }
        messageManager.send(player, teleport.successMessageKey());
    }

    private static final class Countdown {
        private final ActiveTeleport teleport;
        private final int totalSeconds;
        private final Map<String, String> placeholders;
        private ScheduledTaskHandle handle = () -> {
        };

        private Countdown(ActiveTeleport teleport, int totalSeconds, Map<String, String> placeholders) {
            this.teleport = teleport;
            this.totalSeconds = totalSeconds;
            this.placeholders = placeholders;
        }
    }
}
