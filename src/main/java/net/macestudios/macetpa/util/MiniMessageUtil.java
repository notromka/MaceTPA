package net.macestudios.macetpa.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MiniMessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // &x&R&R&G&G&B&B  (Bukkit/BungeeCord legacy hex)
    private static final Pattern HEX_X = Pattern.compile(
            "(?i)&x&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])");
    // &#RRGGBB
    private static final Pattern HEX_AMP = Pattern.compile("(?i)&#([0-9a-fA-F]{6})");

    private static final Map<Character, String> CODES = Map.ofEntries(
            Map.entry('0', "<black>"),
            Map.entry('1', "<dark_blue>"),
            Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"),
            Map.entry('4', "<dark_red>"),
            Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"),
            Map.entry('7', "<gray>"),
            Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"),
            Map.entry('a', "<green>"),
            Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"),
            Map.entry('d', "<light_purple>"),
            Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>"));

    private MiniMessageUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(preprocess(input));
    }

    static String preprocess(String input) {
        // 1. &x&R&R&G&G&B&B → <#RRGGBB>
        Matcher m = HEX_X.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<#" + m.group(1) + m.group(2) + m.group(3)
                    + m.group(4) + m.group(5) + m.group(6) + ">");
        }
        m.appendTail(sb);
        input = sb.toString();

        // 2. &#RRGGBB → <#RRGGBB>
        input = HEX_AMP.matcher(input).replaceAll("<#$1>");

        // 3. &a, &b, &l, etc. → MiniMessage tags
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length()) {
                String tag = CODES.get(Character.toLowerCase(input.charAt(i + 1)));
                if (tag != null) {
                    result.append(tag);
                    i++;
                    continue;
                }
            }
            result.append(c);
        }

        return result.toString();
    }
}
