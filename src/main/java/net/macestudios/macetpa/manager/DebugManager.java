package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.hook.HookManager;
import net.macestudios.macetpa.scheduler.SchedulerAdapter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DebugManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final MenuManager menuManager;
    private final RequestManager requestManager;
    private final TeleportManager teleportManager;
    private final PlayerDataManager playerDataManager;
    private final EconomyManager economyManager;
    private final HookManager hookManager;
    private final SchedulerAdapter scheduler;

    public DebugManager(JavaPlugin plugin, ConfigManager configManager, MessageManager messageManager, MenuManager menuManager, RequestManager requestManager, TeleportManager teleportManager, PlayerDataManager playerDataManager, EconomyManager economyManager, HookManager hookManager, SchedulerAdapter scheduler) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.menuManager = menuManager;
        this.requestManager = requestManager;
        this.teleportManager = teleportManager;
        this.playerDataManager = playerDataManager;
        this.economyManager = economyManager;
        this.hookManager = hookManager;
        this.scheduler = scheduler;
    }

    public List<String> lines() {
        return List.of(
                "MaceTPA Debug Information",
                "Plugin Version: " + plugin.getDescription().getVersion(),
                "Server Software: " + plugin.getServer().getName(),
                "Minecraft Version: " + plugin.getServer().getMinecraftVersion(),
                "Java Version: " + System.getProperty("java.version"),
                "Folia Mode: " + scheduler.isFolia(),
                "Vault: " + economyManager.status(),
                "PlaceholderAPI: " + (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null ? "not hooked" : "hooked"),
                "Combat Hooks: " + hookManager.combatStatus(),
                "Active Requests: " + requestManager.activeCount(),
                "Active Teleports: " + teleportManager.activeCount(),
                "Loaded Player Data: " + playerDataManager.loadedCount(),
                "Config Version: " + configManager.configVersion(),
                "Messages Version: " + messageManager.version(),
                "Menus Version: " + menuManager.version(),
                "Back Enabled: " + configManager.backEnabled(),
                "Combat Enabled: " + configManager.combatEnabled(),
                "Economy Enabled: " + configManager.economyEnabled()
        );
    }

    public File createPasteFile() throws IOException {
        File folder = new File(plugin.getDataFolder(), "debug");
        folder.mkdirs();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        File file = new File(folder, "debug-" + timestamp + ".txt");
        Files.write(file.toPath(), lines());
        return file;
    }
}
