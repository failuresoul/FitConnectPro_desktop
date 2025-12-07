package com.gym.utils;

import java.time.LocalDate;
import java. time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatDate(LocalDate date) {
        try {
            if (date == null) {
                return "";
            }
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting date", e);
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        try {
            if (dateTime == null) {
                return "";
            }
            return dateTime.format(DATETIME_FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting datetime", e);
        }
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime. now();
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        try {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Start and end dates cannot be null");
            }
            return ChronoUnit. DAYS.between(start, end);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating days between dates", e);
        }
    }
}