package com.paladincloud.common.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

    private static final DateTimeFormatter zeroMinuteDateFormat = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:00Z");
    private static final DateTimeFormatter iso8601DateFormat = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ssZ");

    private TimeHelper() {
    }

    public static String formatZeroSeconds(ZonedDateTime time) {
        return zeroMinuteDateFormat.format(time);
    }

    public static String formatNowISO8601() {
        return formatISO8601(ZonedDateTime.now());
    }

    public static String formatISO8601(ZonedDateTime time) {
        return iso8601DateFormat.format(time);
    }
}
