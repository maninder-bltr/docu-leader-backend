package com.maninder.fileBrain.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private DateUtils() {}

    /**
     * Convert epoch millis to LocalDate
     */
    public static LocalDate toLocalDate(Long epochMillis) {
        if (epochMillis == null) return null;
        return Instant.ofEpochMilli(epochMillis)
                .atZone(DEFAULT_ZONE)
                .toLocalDate();
    }

    /**
     * Convert LocalDate to epoch millis (start of day)
     */
    public static long toEpochMillis(LocalDate date) {
        return date.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * Get current time in epoch millis
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Get start of today in epoch millis
     */
    public static long startOfToday() {
        return LocalDate.now().atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * Get start of day for given epoch
     */
    public static long startOfDay(long epochMillis) {
        LocalDate date = toLocalDate(epochMillis);
        return toEpochMillis(date);
    }

    /**
     * Get end of day for given epoch (23:59:59.999)
     */
    public static long endOfDay(long epochMillis) {
        LocalDate date = toLocalDate(epochMillis);
        return date.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli() - 1;
    }

    /**
     * Parse date string to epoch millis
     */
    public static Long parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr, ISO_DATE_FORMATTER);
            return toEpochMillis(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Format epoch millis to ISO date string
     */
    public static String formatDate(Long epochMillis) {
        if (epochMillis == null) return null;
        return toLocalDate(epochMillis).toString();
    }
}
