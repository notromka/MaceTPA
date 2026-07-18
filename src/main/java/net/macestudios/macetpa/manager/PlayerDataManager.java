package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.model.PlayerSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerSettings> cache = new ConcurrentHashMap<>();
    private File file;
    private FileConfiguration data;

    public PlayerDataManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                plugin.getLogger().warning("Could not create playerdata.yml: " + exception.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        cache.clear();
    }

    public PlayerSettings settings(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadSettings);
    }

    public void save(UUID uuid) {
        PlayerSettings settings = settings(uuid);
        String path = "players." + uuid;
        data.set(path + ".tpa-enabled", settings.isTpaEnabled());
        data.set(path + ".tpahere-enabled", settings.isTpahereEnabled());
        data.set(path + ".tpaauto-enabled", settings.isTpaautoEnabled());
        data.set(path + ".confirm-request", settings.isConfirmRequest());
        saveFile();
    }

    public void saveAll() {
        cache.keySet().forEach(this::save);
    }

    public int loadedCount() {
        return cache.size();
    }

    private PlayerSettings loadSettings(UUID uuid) {
        String path = "players." + uuid;
        return new PlayerSettings(
                data.getBoolean(path + ".tpa-enabled", configManager.defaultTpaEnabled()),
                data.getBoolean(path + ".tpahere-enabled", configManager.defaultTpahereEnabled()),
                data.getBoolean(path + ".tpaauto-enabled", false),
                data.getBoolean(path + ".confirm-request", configManager.defaultConfirmRequest())
        );
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save playerdata.yml: " + exception.getMessage());
        }
    }
}
