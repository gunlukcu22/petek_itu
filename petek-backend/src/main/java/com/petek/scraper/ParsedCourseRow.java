package com.petek.scraper;

import com.petek.entity.TimeSlot;

import java.util.ArrayList;
import java.util.List;

final class ParsedCourseRow {

    private Integer crn;
    private String courseCode;
    private String title;
    private String instructor;
    private Integer capacity;
    private Integer enrolled;
    private String majorRestrictions;
    private final List<TimeSlot> timeSlots = new ArrayList<>();

    Integer getCrn() {
        return crn;
    }

    void setCrn(Integer crn) {
        this.crn = crn;
    }

    String getCourseCode() {
        return courseCode;
    }

    void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getInstructor() {
        return instructor;
    }

    void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    Integer getCapacity() {
        return capacity;
    }

    void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    Integer getEnrolled() {
        return enrolled;
    }

    void setEnrolled(Integer enrolled) {
        this.enrolled = enrolled;
    }

    String getMajorRestrictions() {
        return majorRestrictions;
    }

    void setMajorRestrictions(String majorRestrictions) {
        this.majorRestrictions = majorRestrictions;
    }

    List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    void addTimeSlot(TimeSlot timeSlot) {
        timeSlots.add(timeSlot);
    }

    boolean isValid() {
        return crn != null
                && courseCode != null && !courseCode.isBlank()
                && title != null && !title.isBlank()
                && !timeSlots.isEmpty();
    }
}
