package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.config.CustomUserDetails;
import com.online.MiniUdemy.entity.Course;
import com.online.MiniUdemy.entity.Review;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.UserRepository;
import com.online.MiniUdemy.service.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class InstructorDashboardController {

    private final CourseService courseService;
    private final UserRepository userRepository;

    public InstructorDashboardController(CourseService courseService, UserRepository userRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @GetMapping("/instructor/dashboard")
    public String showInstructorDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User instructor = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        
        // Fetch all courses created by this instructor
        List<Course> instructorCourses = courseService.getCoursesByInstructor(instructor.getId());

        // Metric 1: Total Published Courses
        int totalCourses = instructorCourses.size();

        // Metric 2: Total Unique Students
        List<User> allUsers = userRepository.findAll();
        Set<User> uniqueStudents = new HashSet<>();
        
        for (User user : allUsers) {
            for (Course course : instructorCourses) {
                if (user.getEnrolledCourses().contains(course)) {
                    uniqueStudents.add(user);
                    break; // Move on to the next user once we confirm they are enrolled
                }
            }
        }
        int totalStudents = uniqueStudents.size();

        // Metric 3: Average Rating Across All Courses
        int totalReviews = 0;
        double sumRatings = 0;
        for (Course course : instructorCourses) {
            if (course.getReviews() != null) {
                for (Review review : course.getReviews()) {
                    sumRatings += review.getRating();
                    totalReviews++;
                }
            }
        }
        
        // Calculate average and round to 1 decimal place (e.g., 4.5)
        double averageRating = (totalReviews == 0) ? 0.0 : Math.round((sumRatings / totalReviews) * 10.0) / 10.0;

        // Pass data to the frontend
        model.addAttribute("user", instructor);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("averageRating", averageRating);

        return "instructor-dashboard"; 
    }
}