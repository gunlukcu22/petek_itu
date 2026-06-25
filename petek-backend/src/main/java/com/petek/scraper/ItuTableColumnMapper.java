package com.petek.scraper;

import com.petek.util.MultiValueCellParser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

final class ItuTableColumnMapper {

    private static final Logger log = LoggerFactory.getLogger(ItuTableColumnMapper.class);

    private ItuTableColumnMapper() {
    }

    static Map<ItuTableColumn, Integer> resolveColumnIndices(Elements headerCells) {
        Map<ItuTableColumn, Integer> mapped = new EnumMap<>(ItuTableColumn.class);
        if (headerCells == null || headerCells.isEmpty()) {
            return defaultMapping();
        }

        for (int i = 0; i < headerCells.size(); i++) {
            String header = MultiValueCellParser.normalizeForMatch(headerCells.get(i).text());
            if (header.isBlank()) {
                continue;
            }
            final int columnIndex = i;
            matchColumn(header).ifPresent(column -> mapped.putIfAbsent(column, columnIndex));
        }

        if (!mapped.containsKey(ItuTableColumn.CRN)) {
            log.warn("Could not detect CRN column from table header; falling back to default indices.");
            return defaultMapping();
        }

        for (ItuTableColumn column : ItuTableColumn.values()) {
            mapped.putIfAbsent(column, defaultIndexFor(column));
        }
        return mapped;
    }

    static Optional<String> cellText(Elements cells, Map<ItuTableColumn, Integer> columns, ItuTableColumn column) {
        Integer index = columns.get(column);
        if (index == null || index < 0 || index >= cells.size()) {
            return Optional.empty();
        }
        Element cell = cells.get(index);
        if (cell == null) {
            return Optional.empty();
        }
        String text = MultiValueCellParser.extractCellText(cell);
        String sanitized = MultiValueCellParser.sanitizePlaceholder(text);
        return sanitized == null ? Optional.empty() : Optional.of(sanitized);
    }

    static Optional<Element> cellElement(Elements cells, Map<ItuTableColumn, Integer> columns, ItuTableColumn column) {
        Integer index = columns.get(column);
        if (index == null || index < 0 || index >= cells.size()) {
            return Optional.empty();
        }
        Element cell = cells.get(index);
        return cell == null ? Optional.empty() : Optional.of(cell);
    }

    private static Map<ItuTableColumn, Integer> defaultMapping() {
        Map<ItuTableColumn, Integer> mapped = new EnumMap<>(ItuTableColumn.class);
        for (ItuTableColumn column : ItuTableColumn.values()) {
            mapped.put(column, defaultIndexFor(column));
        }
        return mapped;
    }

    private static int defaultIndexFor(ItuTableColumn column) {
        return switch (column) {
            case CRN -> 0;
            case COURSE_CODE -> 1;
            case TITLE -> 2;
            case INSTRUCTOR -> 4;
            case BUILDING -> 5;
            case DAY -> 6;
            case TIME -> 7;
            case ROOM -> 8;
            case CAPACITY -> 9;
            case ENROLLED -> 10;
            case MAJOR_RESTRICTIONS -> 12;
        };
    }

    private static Optional<ItuTableColumn> matchColumn(String header) {
        if (header.contains("CRN")) {
            return Optional.of(ItuTableColumn.CRN);
        }
        if (header.contains("DERS KOD") || header.equals("KOD") || header.contains("COURSE CODE")) {
            return Optional.of(ItuTableColumn.COURSE_CODE);
        }
        if (header.contains("DERS AD") || header.contains("TITLE") || header.equals("AD")) {
            return Optional.of(ItuTableColumn.TITLE);
        }
        if (header.contains("OGRETIM YONTEMI") || header.contains("TEACHING METHOD")) {
            return Optional.empty();
        }
        if (header.contains("UYESI") || header.contains("HOCA") || header.contains("INSTRUCTOR")) {
            return Optional.of(ItuTableColumn.INSTRUCTOR);
        }
        if (header.contains("BINA") || header.contains("BUILDING")) {
            return Optional.of(ItuTableColumn.BUILDING);
        }
        if (header.contains("GUN") || header.contains("DAY")) {
            return Optional.of(ItuTableColumn.DAY);
        }
        if (header.contains("SAAT") || header.contains("TIME")) {
            return Optional.of(ItuTableColumn.TIME);
        }
        if (header.contains("DERSLIK") || header.contains("ROOM") || header.contains("ODA")) {
            return Optional.of(ItuTableColumn.ROOM);
        }
        if (header.contains("KONTENJAN") || header.contains("KAPASITE") || header.contains("CAPACITY")) {
            return Optional.of(ItuTableColumn.CAPACITY);
        }
        if (header.contains("YAZILAN") || header.contains("ENROLLED")) {
            return Optional.of(ItuTableColumn.ENROLLED);
        }
        if (header.contains("PROGRAM") || header.contains("ALABILEN") || header.contains("MAJOR")) {
            return Optional.of(ItuTableColumn.MAJOR_RESTRICTIONS);
        }
        return Optional.empty();
    }
}
