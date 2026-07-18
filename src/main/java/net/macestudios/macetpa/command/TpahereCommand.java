package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.RequestType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpahereCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpahereCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.tpahere")) return true;
        Player player = support.requirePlayer(sender);
        if (player != null && args.length < 1) {
            plugin.soundManager().play(player, "error");
            return true;
        }
        return player == null || support.sendRequest(player, args, RequestType.TPAHERE);
    }
}
