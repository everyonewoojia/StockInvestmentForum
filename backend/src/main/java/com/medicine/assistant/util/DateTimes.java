package com.medicine.assistant.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class DateTimes {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private DateTimes() {
    }

    public static LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME);
    }

    public static String format(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    public static List<LocalDate> daysBetween(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<LocalDate>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            days.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    public static Range rangeForStat(String type, String date) {
        if ("month".equals(type)) {
            YearMonth month = YearMonth.parse(date, YEAR_MONTH);
            return new Range(month.atDay(1).atStartOfDay(), month.atEndOfMonth().atTime(23, 59, 59));
        }
        LocalDate day = LocalDate.parse(date, DATE);
        LocalDate monday = day.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        return new Range(monday.atStartOfDay(), sunday.atTime(23, 59, 59));
    }

    public static class Range {
        public final LocalDateTime start;
        public final LocalDateTime end;

        public Range(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
