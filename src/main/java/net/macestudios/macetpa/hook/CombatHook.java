package net.macestudios.macetpa.hook;

import java.util.UUID;

public interface CombatHook {
    boolean isInCombat(UUID playerId);

    long remainingSeconds(UUID playerId);

    default String name() {
        return getClass().getSimpleName();
    }

    default String status() {
        return "hooked";
    }
}
