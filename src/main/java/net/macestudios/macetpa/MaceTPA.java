package net.macestudios.macetpa;

import net.macestudios.macetpa.command.MaceTpaCommand;
import net.macestudios.macetpa.command.TpaCommand;
import net.macestudios.macetpa.command.TpaautoCommand;
import net.macestudios.macetpa.command.TpacancelCommand;
import net.macestudios.macetpa.command.TpacceptCommand;
import net.macestudios.macetpa.command.TpahereCommand;
import net.macestudios.macetpa.command.TpdenyCommand;
import net.macestudios.macetpa.command.TpasettingsCommand;
import net.macestudios.macetpa.listener.InventoryClickListener;
import net.macestudios.macetpa.listener.PlayerCombatListener;
import net.macestudios.macetpa.listener.PlayerDamageListener;
import net.macestudios.macetpa.listener.PlayerMoveListener;
import net.macestudios.macetpa.listener.PlayerQuitListener;
import net.macestudios.macetpa.hook.HookManager;
import net.macestudios.macetpa.manager.AntiSpamManager;
import net.macestudios.macetpa.manager.BackManager;
import net.macestudios.macetpa.manager.CombatManager;
import net.macestudios.macetpa.manager.ConfirmSessionManager;
import net.macestudios.macetpa.manager.ConfigManager;
import net.macestudios.macetpa.manager.DebugManager;
import net.macestudios.macetpa.manager.EconomyManager;
import net.macestudios.macetpa.manager.MenuManager;
import net.macestudios.macetpa.manager.MessageManager;
import net.macestudios.macetpa.manager.PlayerDataManager;
import net.macestudios.macetpa.manager.RequestManager;
import net.macestudios.macetpa.manager.SoundManager;
import net.macestudios.macetpa.manager.TeleportManager;
import net.macestudios.macetpa.scheduler.ScheduledTaskHandle;
import net.macestudios.macetpa.scheduler.SchedulerAdapter;
import net.macestudios.macetpa.scheduler.SchedulerFactory;
import net.macestudios.macetpa.util.ConfigUpdater;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;

public final class MaceTPA extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerDataManager playerDataManager;
    private RequestManager requestManager;
    private SoundManager soundManager;
    private MenuManager menuManager;
    private TeleportManager teleportManager;
    private BackManager backManager;
    private CombatManager combatManager;
    private ConfirmSessionManager confirmSessionManager;
    private AntiSpamManager antiSpamManager;
    private HookManager hookManager;
    private EconomyManager economyManager;
    private DebugManager debugManager;
    private SchedulerAdapter scheduler;
    private ScheduledTaskHandle expiryTask;

    @Override
    public void onEnable() {
        scheduler = SchedulerFactory.create(this);
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();
        if (configManager.configUpdaterEnabled()) {
            ConfigUpdater.update(this, "config.yml", configManager.configUpdaterCreateBackups(), configManager.configUpdaterNotifyConsole());
            ConfigUpdater.update(this, "messages.yml", configManager.configUpdaterCreateBackups(), configManager.configUpdaterNotifyConsole());
            ConfigUpdater.update(this, "menus.yml", configManager.configUpdaterCreateBackups(), configManager.configUpdaterNotifyConsole());
            configManager.load();
        }
        messageManager = new MessageManager(this, configManager);
        messageManager.load();
        playerDataManager = new PlayerDataManager(this, configManager);
        playerDataManager.load();
        requestManager = new RequestManager(Clock.systemUTC(), configManager.requestExpireSeconds(), configManager.oneRequestAtATime());
        backManager = new BackManager(Clock.systemUTC());
        combatManager = new CombatManager(Clock.systemUTC());
        confirmSessionManager = new ConfirmSessionManager(Clock.systemUTC(), configManager.confirmGuiExpireSeconds());
        antiSpamManager = new AntiSpamManager(Clock.systemUTC());
        hookManager = new HookManager();
        hookManager.load(this, configManager, combatManager);
        soundManager = new SoundManager(this, configManager);
        menuManager = new MenuManager(this, playerDataManager, configManager, soundManager, scheduler);
        menuManager.setRequestManager(requestManager);
        menuManager.load();
        economyManager = new EconomyManager(this, configManager, messageManager, hookManager);
        economyManager.warnIfNeeded();
        teleportManager = new TeleportManager(configManager, messageManager, soundManager, scheduler, backManager, economyManager);
        debugManager = new DebugManager(this, configManager, messageManager, menuManager, requestManager, teleportManager, playerDataManager, economyManager, hookManager, scheduler);

        registerCommands();
        registerListeners();
        startExpiryTask();
        getLogger().info("MaceTPA enabled on " + (scheduler.isFolia() ? "Folia" : "Paper/Bukkit") + " scheduler mode.");
    }

    @Override
    public void onDisable() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        if (confirmSessionManager != null) {
            confirmSessionManager.clear();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
    }

    public void reloadMaceTpa() {
        configManager.load();
        messageManager.load();
        menuManager.load();
        playerDataManager.load();
        requestManager.updateSettings(configManager.requestExpireSeconds(), configManager.oneRequestAtATime());
        confirmSessionManager.updateSettings(configManager.confirmGuiExpireSeconds());
        hookManager.load(this, configManager, combatManager);
        economyManager.warnIfNeeded();
    }

    private void registerCommands() {
        register("tpa", new TpaCommand(this));
        register("tpahere", new TpahereCommand(this));
        register("tpaccept", new TpacceptCommand(this));
        register("tpdeny", new TpdenyCommand(this));
        register("tpacancel", new TpacancelCommand(this));
        register("tpauto", new TpaautoCommand(this));
        register("tpasettings", new TpasettingsCommand(this));
        register("macetpa", new MaceTpaCommand(this));
    }

    private void register(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(configManager, teleportManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(configManager, teleportManager), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(configManager, combatManager, teleportManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, menuManager, playerDataManager, messageManager, soundManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(requestManager, teleportManager, playerDataManager, antiSpamManager, combatManager, confirmSessionManager, menuManager), this);
    }

    private void startExpiryTask() {
        expiryTask = scheduler.runGlobalRepeating(() -> requestManager.removeExpiredRequests(), 20L, 20L);
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public RequestManager requestManager() {
        return requestManager;
    }

    public SoundManager soundManager() {
        return soundManager;
    }

    public MenuManager menuManager() {
        return menuManager;
    }

    public TeleportManager teleportManager() {
        return teleportManager;
    }

    public BackManager backManager() {
        return backManager;
    }

    public CombatManager combatManager() {
        return combatManager;
    }

    public ConfirmSessionManager confirmSessionManager() {
        return confirmSessionManager;
    }

    public HookManager hookManager() {
        return hookManager;
    }

    public AntiSpamManager antiSpamManager() {
        return antiSpamManager;
    }

    public EconomyManager economyManager() {
        return economyManager;
    }

    public DebugManager debugManager() {
        return debugManager;
    }
}
