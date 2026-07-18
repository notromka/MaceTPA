package net.macestudios.macetpa.scheduler;

import org.bukkit.plugin.Plugin;

public final class SchedulerFactory {
    private SchedulerFactory() {
    }

    public static SchedulerAdapter create(Plugin plugin) {
        if (isFolia()) {
            return new FoliaSchedulerAdapter(plugin);
        }
        return new BukkitSchedulerAdapter(plugin);
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
