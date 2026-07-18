package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TparequestsCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TparequestsCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.requests")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        plugin.menuManager().openRequests(player);
        plugin.messageManager().send(player, "requests-menu-opened");
        return true;
    }
}
