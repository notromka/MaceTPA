package net.macestudios.macetpa.hook;

import net.macestudios.macetpa.manager.CombatManager;

import java.util.UUID;

public final class InternalCombatHook implements CombatHook {
    private final CombatManager combatManager;

    public InternalCombatHook(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @Override
    public boolean isInCombat(UUID playerId) {
        return combatManager.isInCombat(playerId);
    }

    @Override
    public long remainingSeconds(UUID playerId) {
        return combatManager.remainingSeconds(playerId);
    }

    @Override
    public String name() {
        return "Internal";
    }
}
