package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.util.MiniMessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public final class MessageManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private FileConfiguration messages;

    public MessageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public int version() {
        return messages.getInt("config-version", 1);
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(component(key, placeholders));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void actionbar(Player player, String key, Map<String, String> placeholders) {
        player.sendActionBar(component(key, placeholders));
    }

    public Component component(String key, Map<String, String> placeholders) {
        return MiniMessageUtil.parse(raw(key, placeholders));
    }

    public Component button(String key, String hoverKey, Map<String, String> placeholders, String command) {
        return component(key, placeholders)
                .hoverEvent(HoverEvent.showText(component(hoverKey, placeholders)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public String raw(String key, Map<String, String> placeholders) {
        String text = messages.getString("messages." + key, fallback(key));
        text = text.replace("{prefix}", configManager.prefix());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }

    private String fallback(String key) {
        return switch (key) {
            case "no-permission" -> "{prefix} <red>You do not have permission to use this command.";
            case "player-only" -> "{prefix} <red>Only players can use this command.";
            case "player-not-found" -> "{prefix} <red>That player is not online.";
            case "no-pending-requests" -> "{prefix} <red>You do not have any pending teleport requests.";
            case "reload-success" -> "{prefix} <green>Configuration reloaded successfully.";
            default -> "{prefix} <red>Missing message: " + key;
        };
    }
}
