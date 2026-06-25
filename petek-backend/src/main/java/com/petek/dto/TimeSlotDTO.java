package com.petek.dto;

import com.petek.entity.DayOfWeek;

public record TimeSlotDTO(
        Long id,
        DayOfWeek dayOfWeek,
        String startTimeString,
        String endTimeString,
        Integer startMinute,
        Integer endMinute,
        String building,
        String room
) {
}
