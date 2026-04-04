package com.online.MiniUdemy.service;

import com.online.MiniUdemy.entity.Course;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.CourseRepository;
import com.online.MiniUdemy.repository.UserRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public void saveCourse(Course course, User instructor) {
        // Attach the instructor to the course before saving
        course.setInstructor(instructor);
        courseRepository.save(course);
    }
    
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }
    
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
    }

    public java.util.List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    
    public void enrollStudent(Long courseId, User student) {
        Course course = getCourseById(courseId);
        // Add the course to the student's list of enrolled courses
        student.getEnrolledCourses().add(course);
        // Save the updated student to the database (Assuming you inject UserRepository!)
        userRepository.save(student); 
    }
    
 // Add this search method
    public java.util.List<Course> searchCourses(String query) {
        return courseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }
}