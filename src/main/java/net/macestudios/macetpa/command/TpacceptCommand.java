package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.TeleportRequest;
import net.macestudios.macetpa.util.WorldRuleUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public final class TpacceptCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpacceptCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.accept")) return true;
        Player receiver = support.requirePlayer(sender);
        if (receiver == null) return true;
        if (!WorldRuleUtil.canUseFrom(receiver, plugin.configManager(), plugin.messageManager())) return true;
        Player requestedSender = args.length > 0 ? plugin.getServer().getPlayerExact(args[0]) : null;
        if (args.length > 0 && requestedSender == null) {
            plugin.messageManager().send(receiver, "player-not-found");
            plugin.messageManager().actionbar(receiver, "player-not-found", Map.of());
            plugin.soundManager().playTwice(receiver, "error");
            return true;
        }
        if (args.length == 0 && plugin.requestManager().incomingCount(receiver.getUniqueId()) > 1) {
            plugin.messageManager().send(receiver, "multiple-pending-requests");
            return true;
        }
        Optional<TeleportRequest> request = plugin.requestManager().resolveIncoming(receiver.getUniqueId(), requestedSender == null ? null : requestedSender.getUniqueId());
        if (request.isEmpty()) {
            plugin.messageManager().send(receiver, "no-pending-requests");
            plugin.messageManager().actionbar(receiver, "no-pending-requests", Map.of());
            plugin.soundManager().play(receiver, "error");
            return true;
        }
        Player requestSender = plugin.getServer().getPlayer(request.get().sender());
        if (requestSender == null) {
            plugin.messageManager().send(receiver, "player-not-found");
            plugin.messageManager().actionbar(receiver, "player-not-found", Map.of());
            plugin.soundManager().playTwice(receiver, "error");
            return true;
        }
        if (!WorldRuleUtil.canTarget(receiver, requestSender, plugin.configManager(), plugin.messageManager())) return true;
        Map<String, String> placeholders = support.placeholders(requestSender, receiver, request.get().type());
        plugin.messageManager().send(requestSender, "request-accepted-sender", placeholders);
        plugin.messageManager().send(receiver, "request-accepted-target", placeholders);
        plugin.messageManager().actionbar(receiver, "request-accepted-target", placeholders);
        plugin.soundManager().play(requestSender, "request-accepted");
        plugin.soundManager().play(receiver, "request-accepted");
        plugin.teleportManager().startTeleport(request.get(), requestSender, receiver);
        return true;
    }
}
