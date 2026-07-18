package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpacancelCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpacancelCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.cancel")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        if (plugin.requestManager().cancelOutgoing(player.getUniqueId()).isPresent()) {
            plugin.messageManager().send(player, "request-cancelled");
            plugin.messageManager().actionbar(player, "request-cancelled", java.util.Map.of());
        } else {
            plugin.messageManager().send(player, "no-outgoing-request");
            plugin.messageManager().actionbar(player, "no-outgoing-request", java.util.Map.of());
            plugin.soundManager().play(player, "error");
        }
        return true;
    }
}
