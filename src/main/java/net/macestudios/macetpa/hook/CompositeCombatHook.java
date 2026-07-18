package net.macestudios.macetpa.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CompositeCombatHook implements CombatHook {
    private final List<CombatHook> hooks = new ArrayList<>();

    public void add(CombatHook hook) {
        hooks.add(hook);
    }

    @Override
    public boolean isInCombat(UUID playerId) {
        for (CombatHook hook : hooks) {
            if (hook.isInCombat(playerId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long remainingSeconds(UUID playerId) {
        long remaining = 0;
        for (CombatHook hook : hooks) {
            if (hook.isInCombat(playerId)) {
                remaining = Math.max(remaining, hook.remainingSeconds(playerId));
            }
        }
        return remaining <= 0 ? 1 : remaining;
    }

    @Override
    public String name() {
        return "Composite";
    }

    @Override
    public String status() {
        List<String> statuses = new ArrayList<>();
        for (CombatHook hook : hooks) {
            statuses.add(hook.name() + ": " + hook.status());
        }
        return String.join(", ", statuses);
    }
}
