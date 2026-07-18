package net.macestudios.macetpa.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ConfigUpdater {
    private static final int CURRENT_VERSION = 1;

    private ConfigUpdater() {
    }

    public static void update(JavaPlugin plugin, String fileName, boolean backups, boolean notifyConsole) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }
        FileConfiguration current = YamlConfiguration.loadConfiguration(file);
        InputStream resource = plugin.getResource(fileName);
        if (resource == null) {
            return;
        }
        FileConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
        boolean changed = current.getInt("config-version", 0) < CURRENT_VERSION;
        for (String key : defaults.getKeys(true)) {
            if (!current.contains(key)) {
                current.set(key, defaults.get(key));
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        if (backups) {
            backup(plugin, file, fileName);
        }
        try {
            current.save(file);
            if (notifyConsole) {
                plugin.getLogger().warning(fileName + " was outdated or missing options. A backup was created and missing options were added.");
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not update " + fileName + ": " + exception.getMessage());
        }
    }

    private static void backup(JavaPlugin plugin, File file, String fileName) {
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        backupFolder.mkdirs();
        String baseName = fileName.replace(".yml", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        File backup = new File(backupFolder, baseName + "-" + timestamp + ".yml");
        try {
            Files.copy(file.toPath(), backup.toPath());
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not create backup for " + fileName + ": " + exception.getMessage());
        }
    }
}
