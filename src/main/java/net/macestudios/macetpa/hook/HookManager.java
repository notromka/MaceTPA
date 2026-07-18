package net.macestudios.macetpa.hook;

import net.macestudios.macetpa.manager.CombatManager;
import net.macestudios.macetpa.manager.ConfigManager;
import org.bukkit.plugin.Plugin;

public final class HookManager {
    private final VaultHook vaultHook = new VaultHook();
    private CombatHook combatHook;

    public void load(Plugin plugin, ConfigManager configManager, CombatManager combatManager) {
        vaultHook.hook(plugin);
        CompositeCombatHook composite = new CompositeCombatHook();
        if (configManager.externalCombatHooksEnabled()) {
            if (configManager.deluxeCombatHookEnabled()) {
                composite.add(new ExternalCombatHook(plugin.getServer(), plugin.getLogger(), "DeluxeCombat", "DeluxeCombat", true));
            }
            if (configManager.combatLogXHookEnabled()) {
                composite.add(new ExternalCombatHook(plugin.getServer(), plugin.getLogger(), "CombatLogX", "CombatLogX", false));
            }
            if (configManager.combatPlusHookEnabled()) {
                composite.add(new ExternalCombatHook(plugin.getServer(), plugin.getLogger(), new String[]{"CombatPlus", "Combat+", "Combat Plus"}, "CombatPlus", false));
            }
        }
        if (configManager.internalCombatHookEnabled()) {
            composite.add(new InternalCombatHook(combatManager));
        }
        combatHook = composite;
    }

    public VaultHook vault() {
        return vaultHook;
    }

    public CombatHook combat() {
        return combatHook;
    }

    public String combatStatus() {
        return combatHook == null ? "not loaded" : combatHook.status();
    }
}
