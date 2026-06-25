package com.petek.dto;

import com.petek.entity.Course;
import com.petek.entity.TimeSlot;

import java.util.Comparator;
import java.util.List;

public record CourseDTO(
        Integer crn,
        String courseCode,
        String title,
        String instructor,
        Integer capacity,
        Integer enrolled,
        Integer availableQuota,
        String majorRestrictions,
        List<TimeSlotDTO> timeSlots
) {

    public static CourseDTO from(Course course) {
        Integer availableQuota = null;
        if (course.getCapacity() != null && course.getEnrolled() != null) {
            availableQuota = course.getCapacity() - course.getEnrolled();
        }

        List<TimeSlotDTO> slots = course.getTimeSlots().stream()
                .sorted(Comparator
                        .comparing(TimeSlot::getDayOfWeek)
                        .thenComparing(TimeSlot::getStartMinute))
                .map(CourseDTO::toTimeSlotDto)
                .toList();

        return new CourseDTO(
                course.getCrn(),
                course.getCourseCode(),
                course.getTitle(),
                course.getInstructor(),
                course.getCapacity(),
                course.getEnrolled(),
                availableQuota,
                course.getMajorRestrictions(),
                slots
        );
    }

    private static TimeSlotDTO toTimeSlotDto(TimeSlot slot) {
        return new TimeSlotDTO(
                slot.getId(),
                slot.getDayOfWeek(),
                slot.getStartTimeString(),
                slot.getEndTimeString(),
                slot.getStartMinute(),
                slot.getEndMinute(),
                slot.getBuilding(),
                slot.getRoom()
        );
    }
}
