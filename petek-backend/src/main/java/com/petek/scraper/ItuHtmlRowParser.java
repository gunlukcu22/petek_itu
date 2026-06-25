package com.petek.scraper;

import com.petek.entity.DayOfWeek;
import com.petek.entity.TimeSlot;
import com.petek.util.MultiValueCellParser;
import com.petek.util.ScheduleMinuteConverter;
import com.petek.util.ScheduleMinuteConverter.AbsoluteTimeRange;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ItuHtmlRowParser {

    private static final Logger log = LoggerFactory.getLogger(ItuHtmlRowParser.class);
    private static final Pattern COMBINED_DAY_TIME = Pattern.compile(
            "(?i)(Pazartesi|Salı|Sali|Çarşamba|Carsamba|Perşembe|Persembe|Cuma|Cumartesi|Pazar|Pzt|Sal|Çar|Car|Per|Cum|Cmt|Paz)"
                    + "\\s+(\\d{1,2}:\\d{2}/\\d{1,2}:\\d{2})"
    );

    private ItuHtmlRowParser() {
    }

    static Optional<ParsedCourseRow> parseRow(Element row, Map<ItuTableColumn, Integer> columnIndices, int rowIndex) {
        if (row == null) {
            return Optional.empty();
        }

        Elements cells = row.select("td");
        if (cells.isEmpty()) {
            return Optional.empty();
        }

        ParsedCourseRow parsed = new ParsedCourseRow();

        Optional<Integer> crn = ItuTableColumnMapper
                .cellText(cells, columnIndices, ItuTableColumn.CRN)
                .flatMap(ItuHtmlRowParser::parseCrn);
        if (crn.isEmpty()) {
            log.warn("Row {} skipped: missing or invalid CRN.", rowIndex);
            return Optional.empty();
        }
        parsed.setCrn(crn.get());

        String courseCode = ItuTableColumnMapper
                .cellText(cells, columnIndices, ItuTableColumn.COURSE_CODE)
                .orElse(null);
        if (courseCode == null) {
            log.warn("Row {} (CRN {}): missing course code; skipping.", rowIndex, parsed.getCrn());
            return Optional.empty();
        }
        parsed.setCourseCode(courseCode);

        String title = ItuTableColumnMapper
                .cellText(cells, columnIndices, ItuTableColumn.TITLE)
                .orElse(null);
        if (title == null) {
            log.warn("Row {} (CRN {}): missing title; skipping.", rowIndex, parsed.getCrn());
            return Optional.empty();
        }
        parsed.setTitle(title);

        ItuTableColumnMapper.cellText(cells, columnIndices, ItuTableColumn.INSTRUCTOR)
                .ifPresent(parsed::setInstructor);

        ItuTableColumnMapper.cellText(cells, columnIndices, ItuTableColumn.CAPACITY)
                .flatMap(ItuHtmlRowParser::parseInteger)
                .ifPresent(parsed::setCapacity);

        ItuTableColumnMapper.cellText(cells, columnIndices, ItuTableColumn.ENROLLED)
                .flatMap(ItuHtmlRowParser::parseInteger)
                .ifPresent(parsed::setEnrolled);

        ItuTableColumnMapper.cellText(cells, columnIndices, ItuTableColumn.MAJOR_RESTRICTIONS)
                .ifPresent(parsed::setMajorRestrictions);

        List<String> days = new ArrayList<>(ItuTableColumnMapper
                .cellElement(cells, columnIndices, ItuTableColumn.DAY)
                .map(MultiValueCellParser::splitCell)
                .orElse(List.of()));

        List<String> times = new ArrayList<>(ItuTableColumnMapper
                .cellElement(cells, columnIndices, ItuTableColumn.TIME)
                .map(MultiValueCellParser::splitCell)
                .orElse(List.of()));

        List<String> buildings = ItuTableColumnMapper
                .cellElement(cells, columnIndices, ItuTableColumn.BUILDING)
                .map(MultiValueCellParser::splitCell)
                .orElse(List.of());

        List<String> rooms = ItuTableColumnMapper
                .cellElement(cells, columnIndices, ItuTableColumn.ROOM)
                .map(MultiValueCellParser::splitCell)
                .orElse(List.of());

        extractCombinedDayTimeEntries(days, times);
        if (days.isEmpty() && times.isEmpty()) {
            extractCombinedFromCells(days, times, cells, columnIndices);
        }

        int slotCount = MultiValueCellParser.maxSize(days, times, buildings, rooms);
        if (slotCount == 0) {
            log.warn("Row {} (CRN {}): no schedule data found; skipping.", rowIndex, parsed.getCrn());
            return Optional.empty();
        }

        for (int i = 0; i < slotCount; i++) {
            String dayRaw = MultiValueCellParser.valueAt(days, i);
            String timeRaw = MultiValueCellParser.valueAt(times, i);
            String building = blankToNull(MultiValueCellParser.valueAt(buildings, i));
            String room = blankToNull(MultiValueCellParser.valueAt(rooms, i));

            if (dayRaw.isBlank() || timeRaw.isBlank()) {
                log.warn("Row {} (CRN {}): incomplete day/time at index {}; skipping slot.", rowIndex, parsed.getCrn(), i);
                continue;
            }

            Optional<DayOfWeek> dayOfWeek = DayOfWeek.fromString(dayRaw);
            if (dayOfWeek.isEmpty()) {
                log.warn("Row {} (CRN {}): unrecognized day '{}' at index {}; skipping slot.", rowIndex, parsed.getCrn(), dayRaw, i);
                continue;
            }

            Optional<AbsoluteTimeRange> absoluteRange = ScheduleMinuteConverter.toAbsoluteTimeRange(dayOfWeek.get(), timeRaw);
            if (absoluteRange.isEmpty()) {
                log.warn("Row {} (CRN {}): invalid time '{}' for day '{}' at index {}; skipping slot.",
                        rowIndex, parsed.getCrn(), timeRaw, dayRaw, i);
                continue;
            }

            AbsoluteTimeRange range = absoluteRange.get();
            TimeSlot slot = new TimeSlot();
            slot.setDayOfWeek(range.dayOfWeek());
            slot.setStartTimeString(range.startTimeString());
            slot.setEndTimeString(range.endTimeString());
            slot.setStartMinute(range.startMinute());
            slot.setEndMinute(range.endMinute());
            slot.setBuilding(building);
            slot.setRoom(room);
            parsed.addTimeSlot(slot);
        }

        if (!parsed.isValid()) {
            log.warn("Row {} (CRN {}): no valid time slots after parsing; skipping.", rowIndex, parsed.getCrn());
            return Optional.empty();
        }

        return Optional.of(parsed);
    }

    private static void extractCombinedDayTimeEntries(List<String> days, List<String> times) {
        List<String> expandedDays = new ArrayList<>();
        List<String> expandedTimes = new ArrayList<>();

        int pairCount = Math.max(days.size(), times.size());
        for (int i = 0; i < pairCount; i++) {
            String dayRaw = MultiValueCellParser.valueAt(days, i);
            String timeRaw = MultiValueCellParser.valueAt(times, i);

            if (!dayRaw.isBlank() && timeRaw.isBlank()) {
                Optional<CombinedDayTime> combined = parseCombined(dayRaw);
                if (combined.isPresent()) {
                    expandedDays.add(combined.get().day());
                    expandedTimes.add(combined.get().time());
                    continue;
                }
            }
            if (!timeRaw.isBlank() && dayRaw.isBlank()) {
                Optional<CombinedDayTime> combined = parseCombined(timeRaw);
                if (combined.isPresent()) {
                    expandedDays.add(combined.get().day());
                    expandedTimes.add(combined.get().time());
                    continue;
                }
            }
            if (!dayRaw.isBlank()) {
                expandedDays.add(dayRaw);
            }
            if (!timeRaw.isBlank()) {
                expandedTimes.add(timeRaw);
            }
        }

        if (!expandedDays.isEmpty() || !expandedTimes.isEmpty()) {
            days.clear();
            times.clear();
            days.addAll(expandedDays);
            times.addAll(expandedTimes);
        }
    }

    private static void extractCombinedFromCells(
            List<String> days,
            List<String> times,
            Elements cells,
            Map<ItuTableColumn, Integer> columnIndices
    ) {
        List<String> candidates = new ArrayList<>();
        ItuTableColumnMapper.cellElement(cells, columnIndices, ItuTableColumn.DAY)
                .map(MultiValueCellParser::splitCell)
                .ifPresent(candidates::addAll);
        ItuTableColumnMapper.cellElement(cells, columnIndices, ItuTableColumn.TIME)
                .map(MultiValueCellParser::splitCell)
                .ifPresent(candidates::addAll);

        for (String candidate : candidates) {
            parseCombined(candidate).ifPresent(combined -> {
                days.add(combined.day());
                times.add(combined.time());
            });
        }
    }

    private static Optional<CombinedDayTime> parseCombined(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = COMBINED_DAY_TIME.matcher(raw.trim());
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new CombinedDayTime(matcher.group(1), matcher.group(2)));
    }

    private record CombinedDayTime(String day, String time) {
    }

    private static Optional<Integer> parseCrn(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(digits));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseInteger(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(digits));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static String blankToNull(String value) {
        return MultiValueCellParser.sanitizePlaceholder(value);
    }
}
