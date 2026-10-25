package com.bouncyjelly.townsfolk.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClockTime {
    private static final Pattern TIME = Pattern.compile("^([01]?\\d|2[0-3]):([0-5]\\d)$");

    private ClockTime() {}

    public static String format(int ticks) {
        int t = ticks % 24000;
        int totalMinutes = (int) Math.round(t * 60 / 1000.0);
        int h = (6 + totalMinutes / 60) % 24;
        int m = totalMinutes % 60;
        return String.format("%02d:%02d", h, m);
    }

    /** @return ticks, or -1 if the text is not a valid HH:MM time */
    public static int parse(String s) {
        Matcher m = TIME.matcher(s.trim());
        if (!m.matches()) return -1;
        int h = Integer.parseInt(m.group(1));
        int min = Integer.parseInt(m.group(2));
        int minutesSince6 = ((h - 6 + 24) % 24) * 60 + min;
        return (int) Math.round(minutesSince6 * 1000 / 60.0);
    }
}