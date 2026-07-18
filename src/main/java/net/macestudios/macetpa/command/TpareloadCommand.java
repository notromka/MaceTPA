package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class TpareloadCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpareloadCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.reload")) return true;
        plugin.reloadMaceTpa();
        plugin.messageManager().send(sender, "reload-success");
        return true;
    }
}
