package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpasettingsCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpasettingsCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.settings")) return true;
        Player player = support.requirePlayer(sender);
        if (player != null) {
            plugin.menuManager().open(player);
        }
        return true;
    }
}
