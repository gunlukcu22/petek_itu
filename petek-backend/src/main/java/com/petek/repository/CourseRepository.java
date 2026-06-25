package com.petek.repository;

import com.petek.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByIsActiveTrue();

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.timeSlots WHERE c.isActive = true ORDER BY c.crn")
    List<Course> findActiveWithTimeSlots();

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.timeSlots ORDER BY c.crn")
    List<Course> findAllWithTimeSlots();
}
