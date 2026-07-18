package net.macestudios.macetpa.listener;

import net.macestudios.macetpa.MaceTPA;
import net.macestudios.macetpa.command.CommandSupport;
import net.macestudios.macetpa.manager.MenuManager;
import net.macestudios.macetpa.manager.MessageManager;
import net.macestudios.macetpa.manager.PlayerDataManager;
import net.macestudios.macetpa.manager.SoundManager;
import net.macestudios.macetpa.model.ConfirmSession;
import net.macestudios.macetpa.model.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.Optional;
import java.util.UUID;

public final class InventoryClickListener implements Listener {
    private final MaceTPA plugin;
    private final MenuManager menuManager;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messageManager;
    private final SoundManager soundManager;

    public InventoryClickListener(MaceTPA plugin, MenuManager menuManager, PlayerDataManager playerDataManager, MessageManager messageManager, SoundManager soundManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.playerDataManager = playerDataManager;
        this.messageManager = messageManager;
        this.soundManager = soundManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (menuManager.isRequestsInventory(event.getView().getTopInventory())) {
            handleRequestsClick(event, player);
            return;
        }
        if (menuManager.isConfirmInventory(event.getView().getTopInventory())) {
            handleConfirmClick(event, player);
            return;
        }
        if (!menuManager.isSettingsInventory(event.getView().getTopInventory())) return;
        event.setCancelled(true);
        String action = menuManager.actionForSlot(event.getRawSlot());
        PlayerSettings settings = playerDataManager.settings(player.getUniqueId());
        switch (action) {
            case "tpa-toggle" -> settings.setTpaEnabled(!settings.isTpaEnabled());
            case "tpahere-toggle" -> settings.setTpahereEnabled(!settings.isTpahereEnabled());
            case "confirm-request" -> settings.setConfirmRequest(!settings.isConfirmRequest());
            default -> {
                return;
            }
        }
        playerDataManager.save(player.getUniqueId());
        soundManager.play(player, "gui-toggle");
        menuManager.open(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (menuManager.isRequestsInventory(event.getView().getTopInventory())
                || menuManager.isSettingsInventory(event.getView().getTopInventory())
                || menuManager.isConfirmInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && menuManager.isRequestsInventory(event.getView().getTopInventory())) {
            menuManager.clearRequestSlots(player.getUniqueId());
        }
    }

    private void handleRequestsClick(InventoryClickEvent event, Player receiver) {
        event.setCancelled(true);
        UUID senderId = menuManager.requestSenderForSlot(receiver, event.getRawSlot());
        if (senderId == null) {
            return;
        }
        Player requestSender = receiver.getServer().getPlayer(senderId);
        if (requestSender == null) {
            messageManager.send(receiver, "player-not-found");
            messageManager.actionbar(receiver, "player-not-found", java.util.Map.of());
            soundManager.playTwice(receiver, "error");
            menuManager.openRequests(receiver);
            return;
        }
        if (event.isLeftClick()) {
            receiver.performCommand("tpaccept " + requestSender.getName());
            menuManager.openRequests(receiver);
        } else if (event.isRightClick()) {
            receiver.performCommand("tpdeny " + requestSender.getName());
            menuManager.openRequests(receiver);
        }
    }

    private void handleConfirmClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        String action = menuManager.confirmActionForSlot(event.getRawSlot());
        if ("cancel".equals(action)) {
            plugin.confirmSessionManager().removePlayer(player.getUniqueId());
            player.closeInventory();
            return;
        }
        if (!"confirm".equals(action)) {
            return;
        }
        Optional<ConfirmSession> session = plugin.confirmSessionManager().consume(player.getUniqueId());
        player.closeInventory();
        if (session.isEmpty()) {
            messageManager.send(player, "no-outgoing-request");
            return;
        }
        new CommandSupport(plugin).sendConfirmedRequest(player, session.get());
    }
}
