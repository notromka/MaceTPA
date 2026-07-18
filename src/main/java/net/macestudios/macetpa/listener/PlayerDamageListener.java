package net.macestudios.macetpa.listener;

import net.macestudios.macetpa.manager.ConfigManager;
import net.macestudios.macetpa.manager.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class PlayerDamageListener implements Listener {
    private final ConfigManager configManager;
    private final TeleportManager teleportManager;

    public PlayerDamageListener(ConfigManager configManager, TeleportManager teleportManager) {
        this.configManager = configManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (configManager.cancelOnDamage() && event.getEntity() instanceof Player player && teleportManager.isTeleporting(player.getUniqueId())) {
            teleportManager.cancelWithMessage(player, "teleport-cancelled-damage");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (configManager.cancelOnAttack() && event.getDamager() instanceof Player player && teleportManager.isTeleporting(player.getUniqueId())) {
            teleportManager.cancelWithMessage(player, "teleport-cancelled-attack");
        }
    }
}
