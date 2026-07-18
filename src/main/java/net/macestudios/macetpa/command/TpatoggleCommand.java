package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.PlayerSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpatoggleCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpatoggleCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.toggle")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        PlayerSettings settings = plugin.playerDataManager().settings(player.getUniqueId());
        settings.setTpaEnabled(!settings.isTpaEnabled());
        plugin.playerDataManager().save(player.getUniqueId());
        plugin.messageManager().send(player, settings.isTpaEnabled() ? "tpa-toggle-enabled" : "tpa-toggle-disabled");
        return true;
    }
}
