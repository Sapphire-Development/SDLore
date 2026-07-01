package dev.sapphire.sdlore.util;

public final class DurationUtil {

    private DurationUtil() {
    }

    public static String format(final long millis) {
        if (millis < 1000L) {
            return millis + "ms";
        }

        if (millis < 60_000L) {
            final double seconds = millis / 1000.0D;

            if (seconds == Math.floor(seconds)) {
                return (long) seconds + " seconds";
            }

            return String.format("%.2f seconds", seconds);
        }

        final double minutes = millis / 60_000.0D;

        if (minutes == Math.floor(minutes)) {
            return (long) minutes + " minutes";
        }

        return String.format("%.2f minutes", minutes);
    }
}
