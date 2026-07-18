package net.macestudios.macetpa.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class FoliaSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;

    public FoliaSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public ScheduledTaskHandle runGlobalRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, scheduledTask -> runnable.run(), delayTicks, periodTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runEntityLater(Player player, Runnable runnable, long delayTicks) {
        ScheduledTask task = player.getScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), null, delayTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runEntityRepeating(Player player, Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = player.getScheduler()
                .runAtFixedRate(plugin, scheduledTask -> runnable.run(), null, delayTicks, periodTicks);
        return task::cancel;
    }

    @Override
    public ScheduledTaskHandle runAtLocation(Location location, Runnable runnable) {
        ScheduledTask task = plugin.getServer().getRegionScheduler()
                .run(plugin, location, scheduledTask -> runnable.run());
        return task::cancel;
    }

    @Override
    public void teleport(Player player, Location location) {
        player.teleportAsync(location);
    }
}
