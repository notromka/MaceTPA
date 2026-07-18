package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.PlayerSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpaautoCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpaautoCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.auto")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        PlayerSettings settings = plugin.playerDataManager().settings(player.getUniqueId());
        settings.setTpaautoEnabled(!settings.isTpaautoEnabled());
        plugin.playerDataManager().save(player.getUniqueId());
        if (settings.isTpaautoEnabled()) {
            plugin.messageManager().send(player, "tpaauto-enabled");
            plugin.messageManager().actionbar(player, "tpaauto-enabled", java.util.Map.of());
            plugin.soundManager().play(player, "tpaauto-on");
        } else {
            plugin.messageManager().send(player, "tpaauto-disabled");
            plugin.messageManager().actionbar(player, "tpaauto-disabled", java.util.Map.of());
            plugin.soundManager().play(player, "tpaauto-off");
        }
        return true;
    }
}
