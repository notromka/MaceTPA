package net.macestudios.macetpa.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SchedulerAdapter {
    boolean isFolia();

    ScheduledTaskHandle runGlobalRepeating(Runnable runnable, long delayTicks, long periodTicks);

    ScheduledTaskHandle runEntityLater(Player player, Runnable runnable, long delayTicks);

    ScheduledTaskHandle runEntityRepeating(Player player, Runnable runnable, long delayTicks, long periodTicks);

    ScheduledTaskHandle runAtLocation(Location location, Runnable runnable);

    void teleport(Player player, Location location);
}
