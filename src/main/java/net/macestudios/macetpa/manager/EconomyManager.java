package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.hook.HookManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public final class EconomyManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final HookManager hookManager;

    public EconomyManager(Plugin plugin, ConfigManager configManager, MessageManager messageManager, HookManager hookManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.hookManager = hookManager;
    }

    public void warnIfNeeded() {
        if (configManager.economyEnabled() && configManager.economyRequireVault() && !hookManager.vault().isHooked()) {
            plugin.getLogger().warning("Economy is enabled, but Vault economy is not available. Economy costs will be blocked.");
        }
    }

    public boolean ensureCanPay(Player player, String command) {
        if (!configManager.economyEnabled()) {
            return true;
        }
        if (!hookManager.vault().isHooked()) {
            messageManager.send(player, "economy-disabled");
            return false;
        }
        double cost = configManager.economyCost(command);
        if (cost <= 0) {
            return true;
        }
        if (!hookManager.vault().has(player, cost)) {
            messageManager.send(player, "not-enough-money", Map.of("cost", format(cost)));
            return false;
        }
        if (!configManager.economyChargeOnlyOnSuccess()) {
            hookManager.vault().withdraw(player, cost);
            messageManager.send(player, "money-taken", Map.of("cost", format(cost)));
        }
        return true;
    }

    public void chargeOnSuccess(Player player, String command) {
        if (!configManager.economyEnabled() || !configManager.economyChargeOnlyOnSuccess() || !hookManager.vault().isHooked()) {
            return;
        }
        double cost = configManager.economyCost(command);
        if (cost <= 0) {
            return;
        }
        hookManager.vault().withdraw(player, cost);
        messageManager.send(player, "money-taken", Map.of("cost", format(cost)));
    }

    public void refundOnCancel(Player player, String command) {
        if (!configManager.economyEnabled() || configManager.economyChargeOnlyOnSuccess() || !configManager.economyRefundOnCancel() || !hookManager.vault().isHooked()) {
            return;
        }
        double cost = configManager.economyCost(command);
        if (cost <= 0) {
            return;
        }
        hookManager.vault().deposit(player, cost);
        messageManager.send(player, "money-refunded", Map.of("cost", format(cost)));
    }

    public String status() {
        if (!configManager.economyEnabled()) {
            return "disabled";
        }
        return hookManager.vault().isHooked() ? "hooked" : "missing";
    }

    private String format(double amount) {
        return amount == Math.rint(amount) ? String.valueOf((long) amount) : String.format("%.2f", amount);
    }
}
