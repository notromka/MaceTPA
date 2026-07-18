package net.macestudios.macetpa.util;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static String formatSeconds(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return "%dm %02ds".formatted(minutes, remainingSeconds);
    }
}
