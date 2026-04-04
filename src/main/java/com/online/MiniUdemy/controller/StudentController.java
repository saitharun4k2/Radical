package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.config.CustomUserDetails;
import com.online.MiniUdemy.entity.Course;
import com.online.MiniUdemy.entity.CourseModule;
import com.online.MiniUdemy.entity.Lesson;
import com.online.MiniUdemy.entity.Review;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.LessonRepository;
import com.online.MiniUdemy.repository.ReviewRepository;
import com.online.MiniUdemy.repository.UserRepository;
import com.online.MiniUdemy.service.CommentService;
import com.online.MiniUdemy.service.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository; 
    private final ReviewRepository reviewRepository;
    private final CommentService commentService;

    public StudentController(CourseService courseService, UserRepository userRepository, LessonRepository lessonRepository, ReviewRepository reviewRepository, CommentService commentService) {
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.reviewRepository = reviewRepository;
        this.commentService = commentService;
    }
    // 1. SHOW THE DASHBOARD
    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        Set<Course> enrolledCourses = student.getEnrolledCourses();
        List<Course> allCourses = courseService.getAllCourses();
        
        List<Course> availableCourses = allCourses.stream()
                .filter(course -> !enrolledCourses.contains(course))
                .collect(Collectors.toList());
        
        // --- NEW: PROGRESS CALCULATOR ---
        Map<Long, Integer> courseProgress = new HashMap<>();
        for (Course c : enrolledCourses) {
            int totalLessons = 0;
            int completed = 0;
            
            for (CourseModule m : c.getModules()) {
                totalLessons += m.getLessons().size();
                for (Lesson l : m.getLessons()) {
                    if (student.getCompletedLessons().contains(l)) {
                        completed++;
                    }
                }
            }
            // Calculate percentage
            int progress = (totalLessons == 0) ? 0 : (int) Math.round((double) completed / totalLessons * 100);
            courseProgress.put(c.getId(), progress);
        }
        
        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("courseProgress", courseProgress); // Pass progress to the UI
        
        return "student-dashboard";
    }

    // 2. PROCESS AN ENROLLMENT
    @PostMapping("/enroll/{courseId}")
    public String enrollInCourse(@PathVariable Long courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        courseService.enrollStudent(courseId, student);
        return "redirect:/student/dashboard";
    }

    // 3. THE COURSE PLAYER
    @GetMapping("/courses/{courseId}/play")
    public String playCourse(@PathVariable Long courseId, 
                             @RequestParam(required = false) Long lessonId,
                             Model model, 
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        Course course = courseService.getCourseById(courseId);
        
        if (!student.getEnrolledCourses().contains(course)) {
            return "redirect:/student/dashboard";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("completedLessons", student.getCompletedLessons()); // Pass completed list so we can show green checkmarks
        
        Lesson currentLesson = null;
        if (lessonId != null) {
            for (CourseModule mod : course.getModules()) {
                for (Lesson l : mod.getLessons()) {
                    if (l.getId().equals(lessonId)) {
                        currentLesson = l;
                        break;
                    }
                }
                if (currentLesson != null) break;
            }
        } else {
            if (course.getModules() != null && !course.getModules().isEmpty()) {
                CourseModule firstMod = course.getModules().get(0);
                if (firstMod.getLessons() != null && !firstMod.getLessons().isEmpty()) {
                    currentLesson = firstMod.getLessons().get(0);
                }
            }
        }
        
        model.addAttribute("currentLesson", currentLesson);
        return "student-player";
    }

    // 4. NEW: MARK LESSON AS COMPLETE
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/complete")
    public String completeLesson(@PathVariable Long courseId, @PathVariable Long lessonId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        
        // Add the lesson to the student's completed list
        student.getCompletedLessons().add(lesson);
        userRepository.save(student);
        
        // Refresh the page so the button turns green
        return "redirect:/student/courses/" + courseId + "/play?lessonId=" + lessonId;
    }
    
 // ==========================================
    // GENERATE COURSE CERTIFICATE
    // ==========================================
    @GetMapping("/courses/{courseId}/certificate")
    public String viewCertificate(@PathVariable Long courseId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        Course course = courseService.getCourseById(courseId);
        
        // 1. Security Check: Are they even enrolled?
        if (!student.getEnrolledCourses().contains(course)) {
            return "redirect:/student/dashboard";
        }
        
        // 2. Verification Check: Did they complete 100% of the lessons?
        int totalLessons = 0;
        int completed = 0;
        for (CourseModule m : course.getModules()) {
            totalLessons += m.getLessons().size();
            for (Lesson l : m.getLessons()) {
                if (student.getCompletedLessons().contains(l)) {
                    completed++;
                }
            }
        }
        
        // If there are no lessons, or they haven't finished them all, deny access!
        if (totalLessons == 0 || completed < totalLessons) {
            return "redirect:/student/dashboard?error=not_completed";
        }
        
        // 3. Grant Access: Send the data to the certificate template
        model.addAttribute("student", student);
        model.addAttribute("course", course);
        
        // Get today's date for the certificate
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        model.addAttribute("issueDate", java.time.LocalDate.now().format(formatter));
        
        return "certificate";
    }
    
 // ==========================================
    // GLOBAL SEARCH API (AJAX)
    // ==========================================
    @GetMapping("/api/search")
    @ResponseBody
    public java.util.List<java.util.Map<String, String>> searchCourses(@RequestParam String query) {
        // Search the database for the query
    	// Search the database for the query
        java.util.List<Course> courses = courseService.searchCourses(query);
        
        // Convert the complex Course objects into simple JSON Maps 
        // (This prevents infinite loop errors when sending data to JavaScript)
        java.util.List<java.util.Map<String, String>> results = new java.util.ArrayList<>();
        for (Course c : courses) {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            map.put("id", c.getId().toString());
            map.put("title", c.getTitle());
            map.put("category", c.getCategory().getName());
            results.add(map);
        }
        
        return results;
    }
    
 // ==========================================
    // LEAVE A REVIEW
    // ==========================================
    @GetMapping("/courses/{courseId}/rate")
    public String showRateCourseForm(@PathVariable Long courseId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Course course = courseService.getCourseById(courseId);
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        
        // Security: Make sure they are enrolled!
        if (!student.getEnrolledCourses().contains(course)) {
            return "redirect:/student/dashboard";
        }
        
        model.addAttribute("course", course);
        return "rate-course";
    }

    @PostMapping("/courses/{courseId}/rate")
    public String submitReview(@PathVariable Long courseId, 
                               @RequestParam int rating, 
                               @RequestParam String comment, 
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
                               
        Course course = courseService.getCourseById(courseId);
        User student = userRepository.findById(userDetails.getUser().getId()).orElseThrow();
        
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setStudent(student);
        review.setCourse(course);
        
        // Save the review to the database
        reviewRepository.save(review);
        
        return "redirect:/student/dashboard";
    }
    
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/comment")
    public String postComment(@PathVariable Long courseId, @PathVariable Long lessonId, @RequestParam String content, @AuthenticationPrincipal CustomUserDetails userDetails) {

        commentService.addComment(userDetails.getUser().getId(), lessonId, content);
        
        return "redirect:/student/courses/" + courseId + "/play?lessonId=" + lessonId;
    }
}