package net.macestudios.macetpa.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SafeLocationUtil {
    private SafeLocationUtil() {
    }

    public static Location findSafe(Location origin, boolean searchNearby, int radius) {
        if (origin == null || origin.getWorld() == null) {
            return null;
        }
        Location centered = origin.clone().toCenterLocation();
        if (isSafe(centered)) {
            return centered;
        }
        if (!searchNearby) {
            return null;
        }
        World world = origin.getWorld();
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();
        int minY = world.getMinHeight() + 1;
        int maxY = world.getMaxHeight() - 2;
        for (int distance = 1; distance <= Math.max(1, radius); distance++) {
            for (int x = baseX - distance; x <= baseX + distance; x++) {
                for (int y = Math.max(minY, baseY - distance); y <= Math.min(maxY, baseY + distance); y++) {
                    for (int z = baseZ - distance; z <= baseZ + distance; z++) {
                        Location candidate = new Location(world, x + 0.5, y, z + 0.5, origin.getYaw(), origin.getPitch());
                        if (isSafe(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);
        return isPassable(feet) && isPassable(head) && ground.getType().isSolid() && !isDangerous(ground.getType());
    }

    private static boolean isPassable(Block block) {
        return block.isPassable() && !isDangerous(block.getType());
    }

    private static boolean isDangerous(Material material) {
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.CAMPFIRE
                || material == Material.SOUL_CAMPFIRE
                || material == Material.CACTUS
                || material == Material.MAGMA_BLOCK;
    }
}
