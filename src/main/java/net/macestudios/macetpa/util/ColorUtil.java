package net.macestudios.macetpa.util;

import net.kyori.adventure.text.Component;

public final class ColorUtil {
    public static final String BRAND_GRADIENT = "<gradient:#8c52ff:#5ce0e6>";
    public static final String BRAND_GRADIENT_END = "</gradient>";

    private ColorUtil() {
    }

    public static Component branded(String text) {
        return MiniMessageUtil.parse(BRAND_GRADIENT + text + BRAND_GRADIENT_END);
    }
}
