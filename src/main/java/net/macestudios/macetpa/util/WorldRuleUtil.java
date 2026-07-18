package net.macestudios.macetpa.util;

import net.macestudios.macetpa.manager.ConfigManager;
import net.macestudios.macetpa.manager.MessageManager;
import org.bukkit.entity.Player;

public final class WorldRuleUtil {
    private WorldRuleUtil() {
    }

    public static boolean canUseFrom(Player player, ConfigManager configManager, MessageManager messageManager) {
        String worldName = player.getWorld().getName();
        if (configManager.isBlockedWorld(worldName)) {
            messageManager.send(player, "world-blocked");
            return false;
        }
        if (!configManager.worldTpaEnabled(worldName)) {
            messageManager.send(player, "world-tpa-disabled");
            return false;
        }
        return true;
    }

    public static boolean canTarget(Player player, Player target, ConfigManager configManager, MessageManager messageManager) {
        if (!configManager.blockedWorldsCheckTargetWorld()) {
            return true;
        }
        if (configManager.isBlockedWorld(target.getWorld().getName())) {
            messageManager.send(player, "target-world-blocked");
            return false;
        }
        if (!configManager.worldTpaEnabled(target.getWorld().getName())) {
            messageManager.send(player, "world-tpa-disabled");
            return false;
        }
        return true;
    }
}
