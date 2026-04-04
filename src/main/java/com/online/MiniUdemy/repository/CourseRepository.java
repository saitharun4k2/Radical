package com.online.MiniUdemy.repository;

import com.online.MiniUdemy.entity.Course;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	List<Course> findByInstructorId(Long instructorId);
	List<Course> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}