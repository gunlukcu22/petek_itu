package com.petek.service;

import com.petek.dto.CourseDTO;
import com.petek.entity.Course;
import com.petek.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCourses(String majorCode) {
        return courseRepository.findActiveWithTimeSlots().stream()
                .filter(course -> matchesMajorCode(course, majorCode))
                .map(CourseDTO::from)
                .toList();
    }

    private boolean matchesMajorCode(Course course, String majorCode) {
        if (majorCode == null || majorCode.isBlank()) {
            return true;
        }

        String restrictions = course.getMajorRestrictions();
        if (restrictions == null || restrictions.isBlank()) {
            return true;
        }

        String normalizedCode = majorCode.trim().toUpperCase(Locale.ROOT);
        return restrictions.toUpperCase(Locale.ROOT).contains(normalizedCode);
    }
}
