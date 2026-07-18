package net.macestudios.macetpa.scheduler;

@FunctionalInterface
public interface ScheduledTaskHandle {
    void cancel();
}
