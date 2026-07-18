package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.BackLocation;
import net.macestudios.macetpa.util.WorldRuleUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public final class BackCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public BackCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.back")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.configManager().backEnabled()) {
            plugin.messageManager().send(player, "back-disabled");
            return true;
        }
        if (!WorldRuleUtil.canUseFrom(player, plugin.configManager(), plugin.messageManager())) return true;
        if (plugin.configManager().combatEnabled() && plugin.configManager().combatBlockBack() && plugin.hookManager().combat().isInCombat(player.getUniqueId())) {
            plugin.messageManager().send(player, "in-combat", Map.of("time", String.valueOf(plugin.hookManager().combat().remainingSeconds(player.getUniqueId()))));
            return true;
        }
        if (!player.hasPermission("macetpa.bypass.back-cooldown") && !plugin.backManager().canUseBack(player.getUniqueId())) {
            plugin.messageManager().send(player, "back-cooldown", Map.of("time", String.valueOf(plugin.backManager().cooldownRemaining(player.getUniqueId()))));
            return true;
        }
        if (plugin.backManager().isExpired(player.getUniqueId())) {
            plugin.messageManager().send(player, "back-expired");
            return true;
        }
        Optional<BackLocation> backLocation = plugin.backManager().getBackLocation(player.getUniqueId());
        if (backLocation.isEmpty() || backLocation.get().location() == null) {
            plugin.messageManager().send(player, "back-no-location");
            return true;
        }
        Location destination = backLocation.get().location();
        if (plugin.configManager().blockedWorldsCheckTargetWorld() && plugin.configManager().isBlockedWorld(destination.getWorld().getName())) {
            plugin.messageManager().send(player, "target-world-blocked");
            return true;
        }
        if (!plugin.economyManager().ensureCanPay(player, "back")) {
            return true;
        }
        plugin.backManager().markUsed(player.getUniqueId(), plugin.configManager().backCooldownSeconds());
        plugin.teleportManager().startBackTeleport(player, destination);
        return true;
    }
}
