package net.macestudios.macetpa.util;

import java.util.Map;

public final class SmallCapsUtil {
    private static final Map<Character, Character> SMALL_CAPS = Map.ofEntries(
            Map.entry('a', '\u1d00'),
            Map.entry('b', '\u0299'),
            Map.entry('c', '\u1d04'),
            Map.entry('d', '\u1d05'),
            Map.entry('e', '\u1d07'),
            Map.entry('f', '\ua730'),
            Map.entry('g', '\u0262'),
            Map.entry('h', '\u029c'),
            Map.entry('i', '\u026a'),
            Map.entry('j', '\u1d0a'),
            Map.entry('k', '\u1d0b'),
            Map.entry('l', '\u029f'),
            Map.entry('m', '\u1d0d'),
            Map.entry('n', '\u0274'),
            Map.entry('o', '\u1d0f'),
            Map.entry('p', '\u1d18'),
            Map.entry('q', '\u01eb'),
            Map.entry('r', '\u0280'),
            Map.entry('s', '\ua731'),
            Map.entry('t', '\u1d1b'),
            Map.entry('u', '\u1d1c'),
            Map.entry('v', '\u1d20'),
            Map.entry('w', '\u1d21'),
            Map.entry('x', 'x'),
            Map.entry('y', '\u028f'),
            Map.entry('z', '\u1d22')
    );

    private SmallCapsUtil() {
    }

    public static String toSmallCaps(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(text.length());
        String[] words = text.split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(convertWord(words[i]));
        }
        return builder.toString();
    }

    private static String convertWord(String word) {
        if ("ON/OFF".equals(word)) {
            return word;
        }
        StringBuilder builder = new StringBuilder(word.length());
        for (char character : word.toCharArray()) {
            builder.append(SMALL_CAPS.getOrDefault(Character.toLowerCase(character), character));
        }
        return builder.toString();
    }
}
