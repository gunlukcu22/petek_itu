package com.petek.runner;

import com.petek.entity.Course;
import com.petek.entity.TimeSlot;
import com.petek.repository.CourseRepository;
import com.petek.scraper.ItuScraperService;
import com.petek.scraper.ScrapeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/**
 * Temporary runner to validate the scraper against real ITU HTML on startup.
 * Remove or disable before production deployment.
 */

@Component
public class ScraperTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ScraperTestRunner.class);
    private static final String SAMPLE_HTML = "sample-itu-data.html";

    private final ItuScraperService ituScraperService;
    private final CourseRepository courseRepository;

    public ScraperTestRunner(ItuScraperService ituScraperService, CourseRepository courseRepository) {
        this.ituScraperService = ituScraperService;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("=== ScraperTestRunner: loading {} ===", SAMPLE_HTML);

        ClassPathResource resource = new ClassPathResource(SAMPLE_HTML);
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        ScrapeResult result = ituScraperService.scrapeFromHtml(html);
        log.info("Scrape result: totalRows={}, saved={}, skipped={}, warnings={}",
                result.getTotalRows(), result.getSavedCount(), result.getSkippedCount(), result.getWarnings().size());
        result.getWarnings().forEach(warning -> log.warn("Scrape warning: {}", warning));

        List<Course> courses = courseRepository.findAllWithTimeSlots();
        log.info("=== {} courses in database ===", courses.size());

        courses.stream()
                .sorted(Comparator.comparing(Course::getCrn))
                .forEach(this::printCourse);
    }

    private void printCourse(Course course) {
        String majorRestrictions = course.getMajorRestrictions();
        String majorPreview = majorRestrictions == null
                ? "null"
                : majorRestrictions.length() > 80
                ? majorRestrictions.substring(0, 80) + "..."
                : majorRestrictions;

        log.info("""
                
                --------------------------------------------------
                CRN: {}
                Code: {}
                Title: {}
                Instructor: {}
                Capacity: {}
                Major Restrictions: {}
                Active: {}
                TimeSlots ({}):
                """,
                course.getCrn(),
                course.getCourseCode(),
                course.getTitle(),
                course.getInstructor(),
                course.getCapacity(),
                majorPreview,
                course.isActive(),
                course.getTimeSlots().size());

        course.getTimeSlots().stream()
                .sorted(Comparator
                        .comparing(TimeSlot::getDayOfWeek)
                        .thenComparing(TimeSlot::getStartMinute))
                .forEach(slot -> log.info(
                        "  - {} {}-{} | building={} room={} | startMinute={} endMinute={}",
                        slot.getDayOfWeek().getTurkishName(),
                        slot.getStartTimeString(),
                        slot.getEndTimeString(),
                        slot.getBuilding(),
                        slot.getRoom(),
                        slot.getStartMinute(),
                        slot.getEndMinute()));
    }
}
