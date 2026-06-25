package com.petek.repository;

import com.petek.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByCourseCrn(Integer crn);

    void deleteByCourseCrn(Integer crn);
}
