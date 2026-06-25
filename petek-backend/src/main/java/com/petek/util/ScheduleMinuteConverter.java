package com.petek.util;

import com.petek.entity.DayOfWeek;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ITU day + clock strings into absolute minutes from week start.
 * Formula: (DayIndex * 24 * 60) + (Hours * 60) + Minutes, Monday = 1.
 */
public final class ScheduleMinuteConverter {

    private static final Pattern CLOCK_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private static final int MINUTES_PER_DAY = 24 * 60;

    private ScheduleMinuteConverter() {
    }

    public static Optional<Integer> parseClockToMinutesOfDay(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = CLOCK_PATTERN.matcher(raw.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            return Optional.empty();
        }
        return Optional.of(hours * 60 + minutes);
    }

    public static Optional<Integer> toAbsoluteMinute(DayOfWeek dayOfWeek, String clock) {
        if (dayOfWeek == null) {
            return Optional.empty();
        }
        return parseClockToMinutesOfDay(clock)
                .map(clockMinutes -> dayOfWeek.getIndex() * MINUTES_PER_DAY + clockMinutes);
    }

    public static Optional<TimeRange> parseTimeRange(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.trim().split("/");
        if (parts.length != 2) {
            return Optional.empty();
        }
        Optional<Integer> start = parseClockToMinutesOfDay(parts[0].trim());
        Optional<Integer> end = parseClockToMinutesOfDay(parts[1].trim());
        if (start.isEmpty() || end.isEmpty()) {
            return Optional.empty();
        }
        String startString = parts[0].trim();
        String endString = parts[1].trim();
        return Optional.of(new TimeRange(startString, endString, start.get(), end.get()));
    }

    public static Optional<AbsoluteTimeRange> toAbsoluteTimeRange(DayOfWeek dayOfWeek, String timeRangeRaw) {
        return parseTimeRange(timeRangeRaw).flatMap(range -> {
            Optional<Integer> startMinute = toAbsoluteMinute(dayOfWeek, range.startTimeString());
            Optional<Integer> endMinute = toAbsoluteMinute(dayOfWeek, range.endTimeString());
            if (startMinute.isEmpty() || endMinute.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new AbsoluteTimeRange(
                    dayOfWeek,
                    range.startTimeString(),
                    range.endTimeString(),
                    startMinute.get(),
                    endMinute.get()
            ));
        });
    }

    public record TimeRange(String startTimeString, String endTimeString, int startOfDayMinute, int endOfDayMinute) {
    }

    public record AbsoluteTimeRange(
            DayOfWeek dayOfWeek,
            String startTimeString,
            String endTimeString,
            int startMinute,
            int endMinute
    ) {
    }
}
