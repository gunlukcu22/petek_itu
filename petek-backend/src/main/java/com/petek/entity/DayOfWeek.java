package com.petek.entity;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * ITU schedule days. Monday = 1 per collision algorithm spec.
 */
public enum DayOfWeek {

    MONDAY(1, "Pazartesi", "Pzt"),
    TUESDAY(2, "Salı", "Sal"),
    WEDNESDAY(3, "Çarşamba", "Çar"),
    THURSDAY(4, "Perşembe", "Per"),
    FRIDAY(5, "Cuma", "Cum"),
    SATURDAY(6, "Cumartesi", "Cmt"),
    SUNDAY(7, "Pazar", "Paz");

    private static final Map<String, DayOfWeek> LOOKUP = Map.ofEntries(
            Map.entry("PAZARTESI", MONDAY),
            Map.entry("PAZARTESİ", MONDAY),
            Map.entry("PZT", MONDAY),
            Map.entry("MONDAY", MONDAY),
            Map.entry("MON", MONDAY),
            Map.entry("SALI", TUESDAY),
            Map.entry("SAL", TUESDAY),
            Map.entry("TUESDAY", TUESDAY),
            Map.entry("TUE", TUESDAY),
            Map.entry("CARSAMBA", WEDNESDAY),
            Map.entry("ÇARŞAMBA", WEDNESDAY),
            Map.entry("CAR", WEDNESDAY),
            Map.entry("ÇAR", WEDNESDAY),
            Map.entry("WEDNESDAY", WEDNESDAY),
            Map.entry("WED", WEDNESDAY),
            Map.entry("PERSEMBE", THURSDAY),
            Map.entry("PERŞEMBE", THURSDAY),
            Map.entry("PER", THURSDAY),
            Map.entry("THURSDAY", THURSDAY),
            Map.entry("THU", THURSDAY),
            Map.entry("CUMA", FRIDAY),
            Map.entry("CUM", FRIDAY),
            Map.entry("FRIDAY", FRIDAY),
            Map.entry("FRI", FRIDAY),
            Map.entry("CUMARTESI", SATURDAY),
            Map.entry("CUMARTESİ", SATURDAY),
            Map.entry("CMT", SATURDAY),
            Map.entry("SATURDAY", SATURDAY),
            Map.entry("SAT", SATURDAY),
            Map.entry("PAZAR", SUNDAY),
            Map.entry("PAZ", SUNDAY),
            Map.entry("SUNDAY", SUNDAY),
            Map.entry("SUN", SUNDAY)
    );

    private final int index;
    private final String turkishName;
    private final String abbreviation;

    DayOfWeek(int index, String turkishName, String abbreviation) {
        this.index = index;
        this.turkishName = turkishName;
        this.abbreviation = abbreviation;
    }

    public int getIndex() {
        return index;
    }

    public String getTurkishName() {
        return turkishName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static Optional<DayOfWeek> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim()
                .toUpperCase(Locale.forLanguageTag("tr-TR"))
                .replace(".", "");
        return Optional.ofNullable(LOOKUP.get(normalized));
    }
}
