package net.macestudios.macetpa.listener;

import net.macestudios.macetpa.manager.ConfigManager;
import net.macestudios.macetpa.manager.TeleportManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerMoveListener implements Listener {
    private final ConfigManager configManager;
    private final TeleportManager teleportManager;

    public PlayerMoveListener(ConfigManager configManager, TeleportManager teleportManager) {
        this.configManager = configManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        var active = teleportManager.activeTeleport(event.getPlayer().getUniqueId());
        if (active == null || !active.cancelOnMove()) {
            return;
        }
        if (changedBlock(event.getFrom(), event.getTo())) {
            teleportManager.cancelWithMessage(event.getPlayer(), "teleport-cancelled-move");
        }
    }

    private boolean changedBlock(Location from, Location to) {
        return to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ());
    }
}
