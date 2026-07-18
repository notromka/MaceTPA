package net.macestudios.macetpa.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String prefix() {
        return config.getString("prefix", "<gradient:#8c52ff:#5ce0e6><bold>MaceTPA</bold></gradient> <dark_gray>»</dark_gray>");
    }

    public int requestExpireSeconds() {
        return config.getInt("request-expire-seconds", 60);
    }

    public int cooldownSeconds() {
        return config.getInt("cooldown-seconds", 15);
    }

    public int teleportDelaySeconds() {
        return config.getInt("teleport-delay-seconds", 5);
    }

    public int teleportDelaySeconds(String worldName) {
        if (worldSettingsEnabled() && config.contains("world-settings.worlds." + worldName + ".teleport-delay-seconds")) {
            return config.getInt("world-settings.worlds." + worldName + ".teleport-delay-seconds", teleportDelaySeconds());
        }
        return teleportDelaySeconds();
    }

    public boolean cancelOnMove() {
        return config.getBoolean("cancel-on-move", true);
    }

    public boolean cancelOnMove(String worldName) {
        if (worldSettingsEnabled() && config.contains("world-settings.worlds." + worldName + ".cancel-on-move")) {
            return config.getBoolean("world-settings.worlds." + worldName + ".cancel-on-move", cancelOnMove());
        }
        return cancelOnMove();
    }

    public boolean cancelOnDamage() {
        return config.getBoolean("cancel-on-damage", true);
    }

    public boolean cancelOnAttack() {
        return config.getBoolean("cancel-on-attack", true);
    }

    public boolean oneRequestAtATime() {
        return config.getBoolean("one-request-at-a-time", true);
    }

    public boolean clickableButtons() {
        return config.getBoolean("clickable-buttons", true);
    }

    public boolean actionbarCountdown() {
        return config.getBoolean("actionbar-countdown", true);
    }

    public boolean safeTeleportEnabled() {
        return config.getBoolean("safe-teleport.enabled", true);
    }

    public boolean searchNearbySafeLocation() {
        return config.getBoolean("safe-teleport.search-nearby-safe-location", true);
    }

    public int safeTeleportRadius() {
        return config.getInt("safe-teleport.radius", 5);
    }

    public boolean defaultTpaEnabled() {
        return config.getBoolean("default-player-settings.tpa-enabled", true);
    }

    public boolean defaultTpahereEnabled() {
        return config.getBoolean("default-player-settings.tpahere-enabled", true);
    }

    public boolean defaultConfirmRequest() {
        return config.getBoolean("default-player-settings.confirm-request", true);
    }

    public boolean soundsEnabled() {
        return config.getBoolean("sounds.enabled", true);
    }

    public String sound(String key) {
        String path = "sounds." + key;
        if (config.isString(path)) {
            return config.getString(path, "");
        }
        return config.getString(path + ".name", "");
    }

    public boolean soundEnabled(String key) {
        String path = "sounds." + key;
        ConfigurationSection section = config.getConfigurationSection(path);
        return section == null || section.getBoolean("enabled", true);
    }

    public float soundVolume(String key) {
        return (float) config.getDouble("sounds." + key + ".volume", 1.0D);
    }

    public float soundPitch(String key) {
        return (float) config.getDouble("sounds." + key + ".pitch", 1.0D);
    }

    public boolean confirmGuiEnabled() {
        return config.getBoolean("confirm-gui.enabled", true);
    }

    public int confirmGuiExpireSeconds() {
        return config.getInt("confirm-gui.expire-seconds", 30);
    }

    public String confirmGuiRegionFallback() {
        return config.getString("confirm-gui.region-fallback", "Europe");
    }

    public boolean debug() {
        return config.getBoolean("debug", false);
    }

    public int configVersion() {
        return config.getInt("config-version", 1);
    }

    public boolean backEnabled() {
        return config.getBoolean("back.enabled", true);
    }

    public int backCooldownSeconds() {
        return config.getInt("back.cooldown-seconds", 30);
    }

    public int backExpireAfterSeconds() {
        return config.getInt("back.expire-after-seconds", 300);
    }

    public boolean backUseTeleportDelay() {
        return config.getBoolean("back.use-teleport-delay", true);
    }

    public boolean backCancelOnMove() {
        return config.getBoolean("back.cancel-on-move", true);
    }

    public boolean backSaveLocationBeforeBack() {
        return config.getBoolean("back.save-location-before-back", true);
    }

    public boolean blockedWorldsEnabled() {
        return config.getBoolean("blocked-worlds.enabled", true);
    }

    public boolean blockedWorldsCheckTargetWorld() {
        return config.getBoolean("blocked-worlds.check-target-world", true);
    }

    public boolean isBlockedWorld(String worldName) {
        return blockedWorldsEnabled() && config.getStringList("blocked-worlds.worlds").stream().anyMatch(world -> world.equalsIgnoreCase(worldName));
    }

    public boolean combatEnabled() {
        return config.getBoolean("combat.enabled", true);
    }

    public int combatTimeSeconds() {
        return config.getInt("combat.combat-time-seconds", 15);
    }

    public boolean combatBlockSendingRequests() {
        return config.getBoolean("combat.block-sending-requests", true);
    }

    public boolean combatBlockAcceptingRequests() {
        return config.getBoolean("combat.block-accepting-requests", true);
    }

    public boolean combatBlockBack() {
        return config.getBoolean("combat.block-back", true);
    }

    public boolean combatCancelTeleportOnCombat() {
        return config.getBoolean("combat.cancel-teleport-on-combat", true);
    }

    public boolean externalCombatHooksEnabled() {
        return config.getBoolean("combat.external-hooks.enabled", true);
    }

    public boolean internalCombatHookEnabled() {
        return config.getBoolean("combat.external-hooks.use-internal-combat", true);
    }

    public boolean deluxeCombatHookEnabled() {
        return config.getBoolean("combat.external-hooks.plugins.deluxecombat", true);
    }

    public boolean combatLogXHookEnabled() {
        return config.getBoolean("combat.external-hooks.plugins.combatlogx", true);
    }

    public boolean combatPlusHookEnabled() {
        return config.getBoolean("combat.external-hooks.plugins.combatplus", true);
    }

    public boolean economyEnabled() {
        return config.getBoolean("economy.enabled", false);
    }

    public boolean economyRequireVault() {
        return config.getBoolean("economy.require-vault", true);
    }

    public double economyCost(String command) {
        return config.getDouble("economy.costs." + command, 0.0D);
    }

    public boolean economyChargeOnlyOnSuccess() {
        return config.getBoolean("economy.charge-only-on-success", true);
    }

    public boolean economyRefundOnCancel() {
        return config.getBoolean("economy.refund-on-cancel", true);
    }

    public boolean worldSettingsEnabled() {
        return config.getBoolean("world-settings.enabled", true);
    }

    public boolean worldTpaEnabled(String worldName) {
        if (worldSettingsEnabled() && config.contains("world-settings.worlds." + worldName + ".tpa-enabled")) {
            return config.getBoolean("world-settings.worlds." + worldName + ".tpa-enabled", true);
        }
        return true;
    }

    public boolean antiSpamEnabled() {
        return config.getBoolean("anti-spam.enabled", true);
    }

    public int antiSpamMaxRequestsPerMinute() {
        return config.getInt("anti-spam.max-requests-per-minute", 5);
    }

    public int antiSpamTemporaryBlockSeconds() {
        return config.getInt("anti-spam.temporary-block-seconds", 60);
    }

    public boolean antiSpamNotifyAdmins() {
        return config.getBoolean("anti-spam.notify-admins", true);
    }

    public boolean configUpdaterEnabled() {
        return config.getBoolean("config-updater.enabled", true);
    }

    public boolean configUpdaterCreateBackups() {
        return config.getBoolean("config-updater.create-backups", true);
    }

    public boolean configUpdaterNotifyConsole() {
        return config.getBoolean("config-updater.notify-console", true);
    }
}
