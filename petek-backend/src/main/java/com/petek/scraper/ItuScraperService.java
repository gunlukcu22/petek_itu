package com.petek.scraper;

import com.petek.entity.Course;
import com.petek.entity.TimeSlot;
import com.petek.repository.CourseRepository;
import com.petek.util.MultiValueCellParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ItuScraperService {

    private static final Logger log = LoggerFactory.getLogger(ItuScraperService.class);

    private final CourseRepository courseRepository;

    public ItuScraperService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ScrapeResult scrapeFromUrl(String url) throws IOException {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank.");
        }
        log.info("Fetching ITU schedule HTML from {}", url);
        Document document = Jsoup.connect(url)
                .userAgent("PetekBot/1.0 (+https://github.com/petek-itu)")
                .timeout(30_000)
                .get();
        return scrapeFromDocument(document);
    }

    @Transactional
    public ScrapeResult scrapeFromHtml(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("HTML must not be blank.");
        }
        Document document = Jsoup.parse(html);
        return scrapeFromDocument(document);
    }

    private ScrapeResult scrapeFromDocument(Document document) {
        ScrapeResult.Builder result = ScrapeResult.builder();
        if (document == null) {
            result.addWarning("Document was null.");
            return result.totalRows(0).savedCount(0).skippedCount(0).build();
        }

        Element table = document.selectFirst("table");
        if (table == null) {
            result.addWarning("No <table> element found in HTML.");
            return result.totalRows(0).savedCount(0).skippedCount(0).build();
        }

        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            result.addWarning("Table contains no rows.");
            return result.totalRows(0).savedCount(0).skippedCount(0).build();
        }

        Map<ItuTableColumn, Integer> columnIndices = detectColumnIndices(rows);
        int saved = 0;
        int skipped = 0;
        int dataRowIndex = 0;

        for (Element row : rows) {
            if (isHeaderRow(row)) {
                continue;
            }
            if (row.selectFirst("td") == null) {
                continue;
            }

            dataRowIndex++;
            Optional<ParsedCourseRow> parsedRow = ItuHtmlRowParser.parseRow(row, columnIndices, dataRowIndex);
            if (parsedRow.isEmpty()) {
                skipped++;
                continue;
            }

            try {
                upsertCourse(parsedRow.get());
                saved++;
            } catch (Exception ex) {
                skipped++;
                String crn = String.valueOf(parsedRow.get().getCrn());
                log.warn("Failed to upsert CRN {}: {}", crn, ex.getMessage());
                result.addWarning("Failed to upsert CRN " + crn + ": " + ex.getMessage());
            }
        }

        log.info("Scrape finished: {} data rows, {} saved, {} skipped.", dataRowIndex, saved, skipped);
        return result.totalRows(dataRowIndex).savedCount(saved).skippedCount(skipped).build();
    }

    private void upsertCourse(ParsedCourseRow parsedRow) {
        Course course = courseRepository.findById(parsedRow.getCrn()).orElseGet(Course::new);
        course.setCrn(parsedRow.getCrn());
        course.setCourseCode(parsedRow.getCourseCode());
        course.setTitle(parsedRow.getTitle());
        course.setInstructor(parsedRow.getInstructor());
        course.setCapacity(parsedRow.getCapacity());
        course.setEnrolled(parsedRow.getEnrolled());
        course.setMajorRestrictions(parsedRow.getMajorRestrictions());
        course.setActive(true);

        course.getTimeSlots().clear();
        for (TimeSlot parsedSlot : parsedRow.getTimeSlots()) {
            TimeSlot slot = new TimeSlot();
            slot.setDayOfWeek(parsedSlot.getDayOfWeek());
            slot.setStartTimeString(parsedSlot.getStartTimeString());
            slot.setEndTimeString(parsedSlot.getEndTimeString());
            slot.setStartMinute(parsedSlot.getStartMinute());
            slot.setEndMinute(parsedSlot.getEndMinute());
            slot.setBuilding(parsedSlot.getBuilding());
            slot.setRoom(parsedSlot.getRoom());
            course.addTimeSlot(slot);
        }

        courseRepository.save(course);
    }

    private Map<ItuTableColumn, Integer> detectColumnIndices(Elements rows) {
        for (Element row : rows) {
            Elements headerCells = row.select("th");
            if (!headerCells.isEmpty()) {
                return ItuTableColumnMapper.resolveColumnIndices(headerCells);
            }

            Elements cells = row.select("td");
            if (cells.isEmpty()) {
                continue;
            }
            String firstCell = MultiValueCellParser.normalizeForMatch(cells.first().text());
            if (firstCell.contains("CRN") || firstCell.contains("DERS KOD")) {
                return ItuTableColumnMapper.resolveColumnIndices(cells);
            }
        }
        return ItuTableColumnMapper.resolveColumnIndices(new Elements());
    }

    private boolean isHeaderRow(Element row) {
        if (row == null) {
            return true;
        }
        if (!row.select("th").isEmpty()) {
            return true;
        }
        Element firstCell = row.selectFirst("td");
        if (firstCell == null) {
            return true;
        }
        String normalized = MultiValueCellParser.normalizeForMatch(firstCell.text());
        return normalized.contains("CRN") && !normalized.chars().allMatch(Character::isDigit);
    }
}
