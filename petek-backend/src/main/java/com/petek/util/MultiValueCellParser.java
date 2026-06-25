package com.petek.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Splits ITU table cells that contain multiple values separated by &lt;br&gt;, commas, or newlines.
 * Strips nested HTML (e.g. &lt;a&gt; tags) so attributes never leak into parsed values.
 */
public final class MultiValueCellParser {

    private static final Pattern BR_SPLIT = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern INLINE_SPLIT = Pattern.compile("[,;]");

    private MultiValueCellParser() {
    }

    public static List<String> splitCell(Element cell) {
        if (cell == null) {
            return List.of();
        }
        String html = cell.html();
        if (html == null || html.isBlank()) {
            return List.of();
        }

        List<String> segments = new ArrayList<>();
        for (String brPart : BR_SPLIT.split(html)) {
            String text = extractPlainText(brPart);
            if (text.isBlank()) {
                continue;
            }
            if (INLINE_SPLIT.matcher(text).find()) {
                Arrays.stream(INLINE_SPLIT.split(text))
                        .map(String::trim)
                        .map(MultiValueCellParser::extractPlainText)
                        .filter(s -> !s.isBlank())
                        .forEach(segments::add);
            } else {
                segments.add(text);
            }
        }
        return segments;
    }

    /**
     * Strips all HTML tags from a fragment and returns visible text only.
     */
    public static String extractPlainText(String htmlFragment) {
        if (htmlFragment == null || htmlFragment.isBlank()) {
            return "";
        }
        return collapseWhitespace(Jsoup.parseBodyFragment(htmlFragment).text());
    }

    /**
     * Extracts plain text from a table cell, including content inside nested tags like &lt;a&gt;.
     */
    public static String extractCellText(Element cell) {
        if (cell == null) {
            return "";
        }
        return collapseWhitespace(cell.text());
    }

    public static boolean isNullPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        return "-".equals(trimmed) || "--".equals(trimmed);
    }

    /**
     * Returns null for blank values and ITU null placeholders ({@code -}, {@code --}).
     */
    public static String sanitizePlaceholder(String value) {
        if (isNullPlaceholder(value)) {
            return null;
        }
        return collapseWhitespace(value);
    }

    public static String valueAt(List<String> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return values.get(index);
    }

    public static int maxSize(List<String>... lists) {
        int max = 0;
        if (lists == null) {
            return 0;
        }
        for (List<String> list : lists) {
            if (list != null) {
                max = Math.max(max, list.size());
            }
        }
        return max;
    }

    public static String normalizeForMatch(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toUpperCase(Locale.forLanguageTag("tr-TR"))
                .replace('İ', 'I')
                .replace('Ş', 'S')
                .replace('Ğ', 'G')
                .replace('Ü', 'U')
                .replace('Ö', 'O')
                .replace('Ç', 'C');
    }

    public static String collapseWhitespace(String text) {
        if (text == null) {
            return "";
        }
        return Arrays.stream(text.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
