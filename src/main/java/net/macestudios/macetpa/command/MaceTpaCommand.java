package net.macestudios.macetpa.command;

import net.macestudios.macetpa.MaceTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class MaceTpaCommand implements CommandExecutor {
    private final MaceTPA plugin;
    private final CommandSupport support;

    public MaceTpaCommand(MaceTPA plugin) {
        this.plugin = plugin;
        this.support = new CommandSupport(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (!support.ensurePermission(sender, "macetpa.reload")) return true;
                plugin.reloadMaceTpa();
                plugin.messageManager().send(sender, "reload-success");
            }
            case "version" -> plugin.messageManager().send(sender, "version", Map.of("version", plugin.getDescription().getVersion()));
            case "debug" -> {
                if (!support.ensurePermission(sender, "macetpa.admin")) return true;
                if (args.length > 1 && args[1].equalsIgnoreCase("paste")) {
                    try {
                        File file = plugin.debugManager().createPasteFile();
                        plugin.messageManager().send(sender, "debug-created", Map.of("file", file.getAbsolutePath()));
                    } catch (IOException exception) {
                        sender.sendMessage("Could not create debug file: " + exception.getMessage());
                    }
                    return true;
                }
                plugin.debugManager().lines().forEach(sender::sendMessage);
            }
            default -> plugin.messageManager().send(sender, "help");
        }
        return true;
    }
}
