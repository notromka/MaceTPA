package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.model.RequestType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpaCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public TpaCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!support.ensurePermission(sender, "macetpa.tpa")) return true;
        Player player = support.requirePlayer(sender);
        if (player == null) return true;
        if (args.length < 1) {
            plugin.soundManager().play(player, "error");
            return true;
        }
        return support.sendRequest(player, args, RequestType.TPA);
    }
}
