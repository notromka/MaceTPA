package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.manager.AntiSpamManager;
import net.macestudios.macetpa.model.ConfirmSession;
import net.macestudios.macetpa.model.RequestType;
import net.macestudios.macetpa.model.TeleportRequest;
import net.macestudios.macetpa.util.WorldRuleUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandSupport {
    private final MaceTPA plugin;
    private static final Map<UUID, Instant> COOLDOWNS = new ConcurrentHashMap<>();

    public CommandSupport(MaceTPA plugin) {
        this.plugin = plugin;
    }

    boolean ensurePermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission) || sender.hasPermission("macetpa.admin")) {
            return true;
        }
        plugin.messageManager().send(sender, "no-permission");
        return false;
    }

    Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        plugin.messageManager().send(sender, "player-only");
        return null;
    }

    boolean sendRequest(Player sender, String[] args, RequestType type) {
        if (args.length < 1) {
            plugin.messageManager().send(sender, "usage-tpahere");
            return true;
        }
        Player target = validateRequestTarget(sender, args[0], type);
        if (target == null) {
            return true;
        }
        if (plugin.configManager().confirmGuiEnabled() && plugin.playerDataManager().settings(sender.getUniqueId()).isConfirmRequest()) {
            plugin.confirmSessionManager().create(sender.getUniqueId(), target.getUniqueId(), type);
            plugin.menuManager().openConfirm(sender, target, type, plugin.configManager().confirmGuiExpireSeconds());
            return true;
        }
        return dispatchRequest(sender, target, type);
    }

    public boolean sendConfirmedRequest(Player sender, ConfirmSession session) {
        Player target = plugin.getServer().getPlayer(session.target());
        if (target == null) {
            plugin.messageManager().send(sender, "player-not-found");
            plugin.messageManager().actionbar(sender, "player-not-found", Map.of());
            plugin.soundManager().playTwice(sender, "error");
            return true;
        }
        Player validatedTarget = validateRequestTarget(sender, target.getName(), session.type());
        if (validatedTarget == null) {
            return true;
        }
        return dispatchRequest(sender, validatedTarget, session.type());
    }

    private Player validateRequestTarget(Player sender, String targetName, RequestType type) {
        if (!WorldRuleUtil.canUseFrom(sender, plugin.configManager(), plugin.messageManager())) {
            plugin.soundManager().play(sender, "error");
            return null;
        }
        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target == null) {
            plugin.messageManager().send(sender, "player-not-found");
            plugin.messageManager().actionbar(sender, "player-not-found", Map.of());
            plugin.soundManager().playTwice(sender, "error");
            return null;
        }
        if (!WorldRuleUtil.canTarget(sender, target, plugin.configManager(), plugin.messageManager())) {
            plugin.soundManager().play(sender, "error");
            return null;
        }
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            plugin.soundManager().play(sender, "error");
            return null;
        }
        var targetSettings = plugin.playerDataManager().settings(target.getUniqueId());
        boolean tpaBlocked = type == RequestType.TPA ? !targetSettings.isTpaEnabled() : !targetSettings.isTpahereEnabled();
        if (tpaBlocked) {
            plugin.messageManager().send(sender, "target-tpa-disabled");
            plugin.messageManager().actionbar(sender, "target-tpa-disabled", Map.of());
            plugin.soundManager().play(sender, "error");
            return null;
        }
        if (!sender.hasPermission("macetpa.bypass.cooldown")) {
            Instant until = COOLDOWNS.get(sender.getUniqueId());
            if (until != null && Instant.now().isBefore(until)) {
                long remaining = Math.max(1, until.getEpochSecond() - Instant.now().getEpochSecond());
                plugin.messageManager().send(sender, "cooldown", Map.of("time", String.valueOf(remaining)));
                plugin.messageManager().actionbar(sender, "cooldown", Map.of("time", String.valueOf(remaining)));
                plugin.soundManager().playTwice(sender, "error");
                return null;
            }
        }
        String economyCommand = type == RequestType.TPA ? "tpa" : "tpahere";
        if (!plugin.economyManager().ensureCanPay(sender, economyCommand)) {
            plugin.soundManager().play(sender, "error");
            return null;
        }
        return target;
    }

    private boolean dispatchRequest(Player sender, Player target, RequestType type) {
        if (!sender.hasPermission("macetpa.bypass.antispam")) {
            AntiSpamManager.Result spam = plugin.antiSpamManager().recordRequest(sender.getUniqueId(), plugin.configManager().antiSpamEnabled(), plugin.configManager().antiSpamMaxRequestsPerMinute(), plugin.configManager().antiSpamTemporaryBlockSeconds());
            if (!spam.allowed()) {
                plugin.messageManager().send(sender, "too-many-requests", Map.of("time", String.valueOf(spam.remainingSeconds())));
                if (spam.newlyBlocked() && plugin.configManager().antiSpamNotifyAdmins()) {
                    plugin.getServer().getOnlinePlayers().stream()
                            .filter(player -> player.hasPermission("macetpa.admin"))
                            .forEach(player -> plugin.messageManager().send(player, "anti-spam-admin-alert", Map.of("player", sender.getName())));
                }
                return true;
            }
        }
        Optional<TeleportRequest> created = plugin.requestManager().createRequest(sender.getUniqueId(), target.getUniqueId(), type);
        if (created.isEmpty()) {
            plugin.messageManager().send(sender, "already-requested");
            plugin.messageManager().actionbar(sender, "already-requested", Map.of());
            plugin.soundManager().play(sender, "error");
            plugin.soundManager().play(sender, "request-denied");
            return true;
        }
        COOLDOWNS.put(sender.getUniqueId(), Instant.now().plusSeconds(plugin.configManager().cooldownSeconds()));
        Map<String, String> placeholders = placeholders(sender, target, type);
        String sentKey = type == RequestType.TPA ? "request-sent" : "request-here-sent";
        plugin.messageManager().send(sender, sentKey, placeholders);
        plugin.messageManager().actionbar(sender, sentKey + "-actionbar", placeholders);
        plugin.soundManager().play(sender, "request-sent");
        plugin.soundManager().play(sender, "request-sent-2");
        String receivedKey = type == RequestType.TPA ? "request-received" : "request-here-received";
        plugin.messageManager().send(target, receivedKey, placeholders);
        if (plugin.configManager().clickableButtons()) {
            Component acceptLine = plugin.messageManager().button("accept-click-button", "accept-click-hover", placeholders, "/tpaccept " + sender.getName())
                    .append(plugin.messageManager().component("accept-or-type", placeholders))
                    .append(plugin.messageManager().button("accept-type-button", "accept-click-hover", placeholders, "/tpaccept " + sender.getName()));
            target.sendMessage(acceptLine);
        }
        plugin.messageManager().send(target, "request-trap-warning", placeholders);
        plugin.messageManager().actionbar(target, receivedKey + "-actionbar", placeholders);
        plugin.soundManager().play(target, "request-received");
        plugin.soundManager().play(target, "request-received-2");
        // Auto-accept if the target has tpaauto enabled
        if (plugin.playerDataManager().settings(target.getUniqueId()).isTpaautoEnabled() && created.isPresent()) {
            plugin.teleportManager().startTeleport(created.get(), sender, target);
        }
        return true;
    }

    Map<String, String> placeholders(Player player, Player target, RequestType type) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("target", target.getName());
        placeholders.put("request_type", type.name());
        return placeholders;
    }
}
