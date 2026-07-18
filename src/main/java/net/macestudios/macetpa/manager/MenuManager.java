package net.macestudios.macetpa.manager;

import net.macestudios.macetpa.menu.MaceMenuHolder;
import net.macestudios.macetpa.model.RequestType;
import net.macestudios.macetpa.model.PlayerSettings;
import net.macestudios.macetpa.scheduler.SchedulerAdapter;
import net.macestudios.macetpa.util.MiniMessageUtil;
import net.macestudios.macetpa.util.SmallCapsUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MenuManager {
    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final SoundManager soundManager;
    private final SchedulerAdapter scheduler;
    private RequestManager requestManager;
    private FileConfiguration menus;
    private String plainTitle = "MaceTPA Settings";
    private String requestsPlainTitle = "Teleport Requests";
    private String confirmPlainTitle = "Confirm Request";
    private final Map<UUID, Map<Integer, UUID>> requestSlots = new HashMap<>();

    public MenuManager(JavaPlugin plugin, PlayerDataManager playerDataManager, ConfigManager configManager, SoundManager soundManager, SchedulerAdapter scheduler) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
        this.soundManager = soundManager;
        this.scheduler = scheduler;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "menus.yml");
        if (!file.exists()) {
            plugin.saveResource("menus.yml", false);
        }
        menus = YamlConfiguration.loadConfiguration(file);
        plainTitle = PlainTextComponentSerializer.plainText().serialize(MiniMessageUtil.parse(menus.getString("settings-menu.title", SmallCapsUtil.toSmallCaps("TPA Settings"))));
        requestsPlainTitle = PlainTextComponentSerializer.plainText().serialize(MiniMessageUtil.parse(menus.getString("requests-menu.title", SmallCapsUtil.toSmallCaps("TPA Requests"))));
        confirmPlainTitle = PlainTextComponentSerializer.plainText().serialize(MiniMessageUtil.parse(menus.getString("confirm-menu.title", SmallCapsUtil.toSmallCaps("Confirm Request"))));
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public int version() {
        return menus.getInt("config-version", 1);
    }

    public void open(Player player) {
        int size = normalizeSize(menus.getInt("settings-menu.size", 27));
        MaceMenuHolder holder = new MaceMenuHolder(MaceMenuHolder.Type.SETTINGS);
        Inventory inventory = Bukkit.createInventory(holder, size, MiniMessageUtil.parse(menus.getString("settings-menu.title", "MaceTPA Settings")));
        holder.inventory(inventory);
        if (menus.getBoolean("settings-menu.filler.enabled", true)) {
            ItemStack filler = item(
                    menus.getString("settings-menu.filler.material", "GRAY_STAINED_GLASS_PANE"),
                    menus.getString("settings-menu.filler.name", " "),
                    List.of()
            );
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        PlayerSettings settings = playerDataManager.settings(player.getUniqueId());
        setToggle(inventory, "tpa-toggle", settings.isTpaEnabled());
        setToggle(inventory, "tpahere-toggle", settings.isTpahereEnabled());
        setToggle(inventory, "confirm-request", settings.isConfirmRequest());
        openInventory(player, inventory);
    }

    public boolean isSettingsTitle(String title) {
        return plainTitle.equals(title);
    }

    public boolean isRequestsTitle(String title) {
        return requestsPlainTitle.equals(title);
    }

    public boolean isConfirmTitle(String title) {
        return confirmPlainTitle.equals(title);
    }

    public boolean isSettingsInventory(Inventory inventory) {
        return hasHolderType(inventory, MaceMenuHolder.Type.SETTINGS);
    }

    public boolean isRequestsInventory(Inventory inventory) {
        return hasHolderType(inventory, MaceMenuHolder.Type.REQUESTS);
    }

    public boolean isConfirmInventory(Inventory inventory) {
        return hasHolderType(inventory, MaceMenuHolder.Type.CONFIRM);
    }

    public String actionForSlot(int slot) {
        ConfigurationSection items = menus.getConfigurationSection("settings-menu.items");
        if (items == null) {
            return "";
        }
        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot", -1) == slot) {
                return key;
            }
        }
        return "";
    }

    public void openRequests(Player player) {
        int size = normalizeSize(menus.getInt("requests-menu.size", 27));
        MaceMenuHolder holder = new MaceMenuHolder(MaceMenuHolder.Type.REQUESTS);
        Inventory inventory = Bukkit.createInventory(holder, size, MiniMessageUtil.parse(menus.getString("requests-menu.title", "Teleport Requests")));
        holder.inventory(inventory);
        if (menus.getBoolean("requests-menu.filler.enabled", true)) {
            ItemStack filler = item(
                    menus.getString("requests-menu.filler.material", "GRAY_STAINED_GLASS_PANE"),
                    menus.getString("requests-menu.filler.name", " "),
                    List.of()
            );
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        Map<Integer, UUID> slots = new HashMap<>();
        List<net.macestudios.macetpa.model.TeleportRequest> requests = requestManager == null ? List.of() : requestManager.getIncomingRequests(player.getUniqueId());
        if (requests.isEmpty()) {
            int slot = menus.getInt("requests-menu.no-requests.slot", 13);
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, item(menus.getString("requests-menu.no-requests.material", "BARRIER"), menus.getString("requests-menu.no-requests.name", "<red><bold>No Requests</bold></red>"), menus.getStringList("requests-menu.no-requests.lore")));
            }
        } else {
            int slot = 0;
            for (net.macestudios.macetpa.model.TeleportRequest request : requests) {
                if (slot >= size) {
                    break;
                }
                Player sender = plugin.getServer().getPlayer(request.sender());
                String senderName = sender == null ? "Unknown" : sender.getName();
                long remaining = Math.max(1, request.expiresAt().getEpochSecond() - Instant.now().getEpochSecond());
                ItemStack requestItem = requestItem(sender, senderName, request.type().name(), remaining);
                inventory.setItem(slot, requestItem);
                slots.put(slot, request.sender());
                slot++;
            }
        }
        requestSlots.put(player.getUniqueId(), slots);
        openInventory(player, inventory);
    }

    public UUID requestSenderForSlot(Player player, int slot) {
        return requestSlots.getOrDefault(player.getUniqueId(), Map.of()).get(slot);
    }

    public void clearRequestSlots(UUID playerId) {
        requestSlots.remove(playerId);
    }

    public void openConfirm(Player player, Player target, RequestType type, int expireSeconds) {
        int size = normalizeSize(menus.getInt("confirm-menu.size", 27));
        MaceMenuHolder holder = new MaceMenuHolder(MaceMenuHolder.Type.CONFIRM);
        Inventory inventory = Bukkit.createInventory(holder, size, MiniMessageUtil.parse(menus.getString("confirm-menu.title", SmallCapsUtil.toSmallCaps("Confirm Request"))));
        holder.inventory(inventory);
        Map<String, String> placeholders = confirmPlaceholders(player, target, type, expireSeconds);
        if (menus.getBoolean("confirm-menu.filler.enabled", false)) {
            ItemStack filler = item(
                    menus.getString("confirm-menu.filler.material", "BLACK_CONCRETE"),
                    menus.getString("confirm-menu.filler.name", "<!italic> "),
                    List.of(),
                    placeholders
            );
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        setConfirmItem(inventory, "cancel", placeholders, null);
        setConfirmItem(inventory, "location", placeholders, null);
        setConfirmItem(inventory, "target-player", placeholders, target);
        setConfirmItem(inventory, "region", placeholders, null);
        setConfirmItem(inventory, "confirm", placeholders, null);
        openInventory(player, inventory);
    }

    public String confirmActionForSlot(int slot) {
        ConfigurationSection items = menus.getConfigurationSection("confirm-menu.items");
        if (items == null) {
            return "";
        }
        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot", -1) == slot) {
                return key;
            }
        }
        return "";
    }

    private void setToggle(Inventory inventory, String key, boolean enabled) {
        String path = "settings-menu.items." + key + ".";
        int slot = menus.getInt(path + "slot", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        String status = enabled ? "ON" : "OFF";
        Map<String, String> placeholders = Map.of(
                "status", status,
                "status_color", enabled ? "green" : "red"
        );
        inventory.setItem(slot, item(
                menus.getString(path + (enabled ? "material-enabled" : "material-disabled"), "STONE"),
                menus.getString(path + (enabled ? "name-enabled" : "name-disabled"), key),
                menus.getStringList(path + "lore"),
                placeholders
        ));
    }

    private void setStatic(Inventory inventory, String key) {
        String path = "settings-menu.items." + key + ".";
        int slot = menus.getInt(path + "slot", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        inventory.setItem(slot, item(menus.getString(path + "material", "BARRIER"), menus.getString(path + "name", key), menus.getStringList(path + "lore")));
    }

    private ItemStack item(String materialName, String name, List<String> lore) {
        return item(materialName, name, lore, Map.of());
    }

    private ItemStack item(String materialName, String name, List<String> lore, Map<String, String> placeholders) {
        Material material = Material.matchMaterial(materialName == null ? "STONE" : materialName);
        ItemStack item = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessageUtil.parse(applyPlaceholders(name, placeholders)));
            List<net.kyori.adventure.text.Component> parsedLore = new ArrayList<>();
            lore.forEach(line -> parsedLore.add(MiniMessageUtil.parse(applyPlaceholders(line, placeholders))));
            meta.lore(parsedLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack requestItem(Player sender, String playerName, String requestType, long remainingSeconds) {
        String name = menus.getString("requests-menu.request-item.name", "<gradient:#8c52ff:#5ce0e6><bold>{player}</bold></gradient>");
        Map<String, String> placeholders = Map.of(
                "player", playerName,
                "target", playerName,
                "request_type", requestType,
                "time", String.valueOf(remainingSeconds),
                "expire_time", String.valueOf(remainingSeconds)
        );
        List<String> lore = menus.getStringList("requests-menu.request-item.lore").stream()
                .toList();
        ItemStack item = item(menus.getString("requests-menu.request-item.material", "PLAYER_HEAD"), name, lore, placeholders);
        if (sender != null && item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(sender);
            item.setItemMeta(skullMeta);
        }
        return item;
    }

    private void setConfirmItem(Inventory inventory, String key, Map<String, String> placeholders, Player headOwner) {
        String path = "confirm-menu.items." + key + ".";
        int slot = menus.getInt(path + "slot", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        ItemStack item = item(
                menus.getString(path + "material", "STONE"),
                menus.getString(path + "name", key),
                menus.getStringList(path + "lore"),
                placeholders
        );
        if (headOwner != null && item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(headOwner);
            item.setItemMeta(skullMeta);
        }
        inventory.setItem(slot, item);
    }

    private Map<String, String> confirmPlaceholders(Player player, Player target, RequestType type, int expireSeconds) {
        Location location = type == RequestType.TPA ? target.getLocation() : player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String world = location.getWorld() == null ? "world" : location.getWorld().getName();
        String locationText = x + ", " + y + ", " + z;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("target", target.getName());
        placeholders.put("request_type", type.name());
        placeholders.put("location", locationText);
        placeholders.put("world", world);
        placeholders.put("x", String.valueOf(x));
        placeholders.put("y", String.valueOf(y));
        placeholders.put("z", String.valueOf(z));
        placeholders.put("region", configManager.confirmGuiRegionFallback());
        placeholders.put("ping", String.valueOf(Math.max(0, target.getPing())));
        placeholders.put("expire_time", String.valueOf(expireSeconds));
        placeholders.put("time", String.valueOf(expireSeconds));
        return placeholders;
    }

    private String applyPlaceholders(String text, Map<String, String> placeholders) {
        String result = text == null ? "" : text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private void openInventory(Player player, Inventory inventory) {
        scheduler.runEntityLater(player, () -> {
            player.openInventory(inventory);
            soundManager.play(player, "gui-open");
        }, 1L);
    }

    private int normalizeSize(int size) {
        if (size < 9) {
            return 9;
        }
        if (size > 54) {
            return 54;
        }
        return ((size + 8) / 9) * 9;
    }

    private boolean hasHolderType(Inventory inventory, MaceMenuHolder.Type type) {
        if (inventory == null) {
            return false;
        }
        InventoryHolder holder = inventory.getHolder();
        return holder instanceof MaceMenuHolder menuHolder && menuHolder.type() == type;
    }
}
