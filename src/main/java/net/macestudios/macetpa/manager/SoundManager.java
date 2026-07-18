package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.util.SoundKeyResolver;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoundManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public SoundManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void play(Player player, String key) {
        if (!configManager.soundsEnabled()) {
            return;
        }
        if (!configManager.soundEnabled(key)) {
            return;
        }
        String configured = configManager.sound(key);
        if (configured == null || configured.isBlank()) {
            return;
        }
        try {
            Sound sound = SoundKeyResolver.resolve(configured, name -> {
                try {
                    Sound.valueOf(name);
                    return true;
                } catch (IllegalArgumentException exception) {
                    return false;
                }
            }).map(Sound::valueOf).orElse(null);
            if (sound == null) {
                if (configManager.debug()) {
                    plugin.getLogger().warning("Invalid sound for " + key + ": " + configured);
                }
                return;
            }
            player.playSound(player.getLocation(), sound, configManager.soundVolume(key), configManager.soundPitch(key));
        } catch (IllegalArgumentException exception) {
            if (configManager.debug()) {
                plugin.getLogger().warning("Invalid sound for " + key + ": " + configured);
            }
        }
    }

    public void playTwice(Player player, String key) {
        play(player, key);
        play(player, key);
    }
}
