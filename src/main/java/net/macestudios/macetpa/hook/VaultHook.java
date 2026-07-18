package net.macestudios.macetpa.hook;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class VaultHook {
    private Object economy;
    private Method hasMethod;
    private Method withdrawMethod;
    private Method depositMethod;

    public boolean hook(Plugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object registration = plugin.getServer().getServicesManager().getRegistration(economyClass);
            if (registration == null) {
                return false;
            }
            Method getProvider = registration.getClass().getMethod("getProvider");
            economy = getProvider.invoke(registration);
            hasMethod = economyClass.getMethod("has", org.bukkit.OfflinePlayer.class, double.class);
            withdrawMethod = economyClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
            depositMethod = economyClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class);
            return economy != null;
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Vault was found, but economy could not be hooked: " + exception.getMessage());
            return false;
        }
    }

    public boolean isHooked() {
        return economy != null;
    }

    public boolean has(Player player, double amount) {
        if (!isHooked() || amount <= 0) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(hasMethod.invoke(economy, player, amount));
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public boolean withdraw(Player player, double amount) {
        if (!isHooked() || amount <= 0) {
            return true;
        }
        try {
            withdrawMethod.invoke(economy, player, amount);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public boolean deposit(Player player, double amount) {
        if (!isHooked() || amount <= 0) {
            return true;
        }
        try {
            depositMethod.invoke(economy, player, amount);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }
}
