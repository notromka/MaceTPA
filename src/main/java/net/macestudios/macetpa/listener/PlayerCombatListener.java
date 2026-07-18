package net.macestudios.macetpa.listener;

import net.macestudios.macetpa.manager.CombatManager;
import net.macestudios.macetpa.manager.ConfigManager;
import net.macestudios.macetpa.manager.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class PlayerCombatListener implements Listener {
    private final ConfigManager configManager;
    private final CombatManager combatManager;
    private final TeleportManager teleportManager;

    public PlayerCombatListener(ConfigManager configManager, CombatManager combatManager, TeleportManager teleportManager) {
        this.configManager = configManager;
        this.combatManager = combatManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCombat(EntityDamageByEntityEvent event) {
        if (!configManager.combatEnabled() || !(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        combatManager.markInCombat(victim.getUniqueId(), configManager.combatTimeSeconds());
        combatManager.markInCombat(attacker.getUniqueId(), configManager.combatTimeSeconds());
        if (configManager.combatCancelTeleportOnCombat()) {
            if (teleportManager.isTeleporting(victim.getUniqueId())) {
                teleportManager.cancelWithMessage(victim, "teleport-cancelled-combat");
            }
            if (teleportManager.isTeleporting(attacker.getUniqueId())) {
                teleportManager.cancelWithMessage(attacker, "teleport-cancelled-combat");
            }
        }
    }
}
