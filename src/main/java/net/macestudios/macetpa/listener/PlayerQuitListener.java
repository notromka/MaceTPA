package net.macestudios.macetpa.listener;

import net.macestudios.macetpa.manager.PlayerDataManager;
import net.macestudios.macetpa.manager.RequestManager;
import net.macestudios.macetpa.manager.TeleportManager;
import net.macestudios.macetpa.manager.AntiSpamManager;
import net.macestudios.macetpa.manager.CombatManager;
import net.macestudios.macetpa.manager.ConfirmSessionManager;
import net.macestudios.macetpa.manager.MenuManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {
    private final RequestManager requestManager;
    private final TeleportManager teleportManager;
    private final PlayerDataManager playerDataManager;
    private final AntiSpamManager antiSpamManager;
    private final CombatManager combatManager;
    private final ConfirmSessionManager confirmSessionManager;
    private final MenuManager menuManager;

    public PlayerQuitListener(RequestManager requestManager, TeleportManager teleportManager, PlayerDataManager playerDataManager, AntiSpamManager antiSpamManager, CombatManager combatManager, ConfirmSessionManager confirmSessionManager, MenuManager menuManager) {
        this.requestManager = requestManager;
        this.teleportManager = teleportManager;
        this.playerDataManager = playerDataManager;
        this.antiSpamManager = antiSpamManager;
        this.combatManager = combatManager;
        this.confirmSessionManager = confirmSessionManager;
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        requestManager.removePlayer(event.getPlayer().getUniqueId());
        teleportManager.cancel(event.getPlayer().getUniqueId());
        antiSpamManager.clear(event.getPlayer().getUniqueId());
        combatManager.clear(event.getPlayer().getUniqueId());
        confirmSessionManager.removePlayer(event.getPlayer().getUniqueId());
        menuManager.clearRequestSlots(event.getPlayer().getUniqueId());
        playerDataManager.save(event.getPlayer().getUniqueId());
    }
}
