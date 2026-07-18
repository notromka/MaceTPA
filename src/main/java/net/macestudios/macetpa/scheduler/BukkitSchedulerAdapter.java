package net.macestudios.macetpa.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;

    public BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    public ScheduledTaskHandle runGlobalRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runEntityLater(Player player, Runnable runnable, long delayTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runEntityRepeating(Player player, Runnable runnable, long delayTicks, long periodTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runAtLocation(Location location, Runnable runnable) {
        BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, runnable);
        return task::cancel;
    }

    @Override
    public void teleport(Player player, Location location) {
        player.teleport(location);
    }
}
