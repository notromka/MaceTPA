package net.macestudios.macetpa.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MaceMenuHolder implements InventoryHolder {
    private final Type type;
    private Inventory inventory;

    public MaceMenuHolder(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public enum Type {
        SETTINGS,
        REQUESTS,
        CONFIRM
    }
}
