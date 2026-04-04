package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.config.CustomUserDetails;
import com.online.MiniUdemy.entity.Course;
import com.online.MiniUdemy.entity.CourseModule;
import com.online.MiniUdemy.entity.Lesson;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.CourseModuleRepository;
import com.online.MiniUdemy.repository.CourseRepository;
import com.online.MiniUdemy.repository.LessonRepository;
import com.online.MiniUdemy.repository.UserRepository;
import com.online.MiniUdemy.service.CategoryService;
import com.online.MiniUdemy.service.CommentService;
import com.online.MiniUdemy.service.CourseService;
import com.online.MiniUdemy.service.FileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/instructor/courses")
public class InstructorCourseController {

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final FileService fileService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    // All necessary services and repositories injected
    public InstructorCourseController(CourseService courseService, CategoryService categoryService, 
                                      CourseModuleRepository courseModuleRepository, 
                                      LessonRepository lessonRepository, FileService fileService, CourseRepository courseRepository, UserRepository userRepository, 
                                      CommentService commentService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.courseModuleRepository = courseModuleRepository;
        this.lessonRepository = lessonRepository;
        this.fileService = fileService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
    }

    // 1. LIST ALL COURSES
    @GetMapping
    public String listCourses(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User instructor = userDetails.getUser();
        model.addAttribute("courses", courseService.getCoursesByInstructor(instructor.getId()));
        return "instructor/manage_courses"; 
    }

    // 2. SHOW CREATE COURSE FORM
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/course_form";
    }

    // 3. SAVE NEW COURSE
    @PostMapping("/create")
    public String saveCourse(@ModelAttribute("course") Course course, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User instructor = userDetails.getUser();
        courseService.saveCourse(course, instructor);
        return "redirect:/instructor/dashboard?success";
    }

    // 4. SHOW EDIT COURSE FORM
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("course", courseService.getCourseById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "instructor/course_edit";
    }

    // 5. SAVE EDITED COURSE
    @PostMapping("/{id}/update")
    public String updateCourse(@PathVariable("id") Long id, @ModelAttribute("course") Course course, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User instructor = userDetails.getUser();
        course.setInstructor(instructor); 
        courseService.saveCourse(course, instructor); 
        return "redirect:/instructor/courses"; 
    }

    // 6. SHOW MANAGE MODULES PAGE
    @GetMapping("/{id}/modules")
    public String manageModules(@PathVariable("id") Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        model.addAttribute("newModule", new CourseModule()); 
        
        // ULTIMATE NULL FIX: We grab the list directly and guarantee it is never null
        List<CourseModule> moduleList = courseModuleRepository.findByCourseId(id);
        if (moduleList == null) {
            moduleList = new ArrayList<>();
        }
        model.addAttribute("moduleList", moduleList);
        
        return "instructor/manage_modules";
    }

    // 7. SAVE NEW MODULE
    @PostMapping("/{id}/module")
    public String saveModule(@PathVariable("id") Long courseId, @ModelAttribute("newModule") CourseModule courseModule) {
        courseModule.setId(null); // Fixes the StaleObjectStateException!
        Course course = courseService.getCourseById(courseId);
        courseModule.setCourse(course); 
        courseModuleRepository.save(courseModule);
        return "redirect:/instructor/courses/" + courseId + "/modules"; 
    }

    // 8. SHOW ADD LESSON FORM
    @GetMapping("/{courseId}/modules/{moduleId}/lessons/create")
    public String showAddLessonForm(@PathVariable Long courseId, @PathVariable Long moduleId, Model model) {
        CourseModule module = courseModuleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid module Id"));
        model.addAttribute("courseId", courseId);
        model.addAttribute("module", module);
        model.addAttribute("lesson", new Lesson()); 
        return "instructor/lesson_form";
    }

 // 9. SAVE NEW LESSON (TEXT OR PDF)
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/create")
    public String saveLesson(@PathVariable Long courseId, 
                             @PathVariable Long moduleId, 
                             @ModelAttribute("lesson") Lesson lesson,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            CourseModule module = courseModuleRepository.findById(moduleId).get();
            lesson.setModule(module);

            // Check if they uploaded a PDF
            if (file != null && !file.isEmpty() && file.getOriginalFilename().endsWith(".pdf")) {
                String fileUrl = fileService.saveFile(file); 
                lesson.setFileUrl(fileUrl);
                lesson.setContentType("PDF");
            } else {
                // If no file, treat it as a Text lesson
                lesson.setContentType("Text");
            }

            lessonRepository.save(lesson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "redirect:/instructor/courses/" + courseId + "/modules";
    }
    
 // 10. VIEW SPECIFIC LESSON
    @GetMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    public String viewLesson(@PathVariable Long courseId, 
                             @PathVariable Long moduleId, 
                             @PathVariable Long lessonId, 
                             Model model) {
                             
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid lesson Id"));
            
        model.addAttribute("courseId", courseId);
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("lesson", lesson);
        
        return "instructor/view_lesson";
    }
    
 // --- DELETE MODULE ---
    @PostMapping("/{courseId}/modules/{moduleId}/delete")
    public String deleteModule(@PathVariable Long courseId, @PathVariable Long moduleId) {
        // Because of CascadeType.ALL, this safely deletes the module AND all its lessons
        courseModuleRepository.deleteById(moduleId);
        return "redirect:/instructor/courses/" + courseId + "/modules";
    }

    // --- DELETE LESSON ---
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId, @PathVariable Long moduleId, @PathVariable Long lessonId) {
        lessonRepository.deleteById(lessonId);
        return "redirect:/instructor/courses/" + courseId + "/modules";
    }
    
 // ==========================================
    // EDIT MODULE
    // ==========================================
    @GetMapping("/{courseId}/modules/{moduleId}/edit")
    public String showEditModuleForm(@PathVariable Long courseId, @PathVariable Long moduleId, Model model) {
        Course course = courseService.getCourseById(courseId);
        CourseModule module = courseModuleRepository.findById(moduleId).orElseThrow();
        
        // FIXED: This is now addAttribute instead of append!
        model.addAttribute("course", course);
        model.addAttribute("module", module);
        
        return "edit-module";
    }

    @PostMapping("/{courseId}/modules/{moduleId}/edit")
    public String updateModule(@PathVariable Long courseId, @PathVariable Long moduleId, @ModelAttribute CourseModule updatedModule) {
        CourseModule existingModule = courseModuleRepository.findById(moduleId).orElseThrow();
        existingModule.setTitle(updatedModule.getTitle());
        courseModuleRepository.save(existingModule);
        
        return "redirect:/instructor/courses/" + courseId + "/modules";
    }

    // ==========================================
    // EDIT LESSON
    // ==========================================
    @GetMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}/edit")
    public String showEditLessonForm(@PathVariable Long courseId, @PathVariable Long moduleId, @PathVariable Long lessonId, Model model) {
        Course course = courseService.getCourseById(courseId);
        CourseModule module = courseModuleRepository.findById(moduleId).orElseThrow();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        
        model.addAttribute("course", course);
        model.addAttribute("module", module);
        model.addAttribute("lesson", lesson);
        return "edit-lesson";
    }

    @PostMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}/edit")
    public String updateLesson(@PathVariable Long courseId, @PathVariable Long moduleId, @PathVariable Long lessonId, @ModelAttribute Lesson updatedLesson) {
        Lesson existingLesson = lessonRepository.findById(lessonId).orElseThrow();
        
        // Update the fields
        existingLesson.setTitle(updatedLesson.getTitle());
        existingLesson.setContentType(updatedLesson.getContentType());
        existingLesson.setTextContent(updatedLesson.getTextContent());
        existingLesson.setFileUrl(updatedLesson.getFileUrl());
        
        lessonRepository.save(existingLesson);
        
        return "redirect:/instructor/courses/" + courseId + "/modules";
    }
    
 // ==========================================
    // DELETE ENTIRE COURSE
    // ==========================================
    @PostMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        
        // 1. SAFETY SWEEP: Remove this course from all students' desks so the database doesn't crash!
        java.util.List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getEnrolledCourses().contains(course)) {
                user.getEnrolledCourses().remove(course);
                userRepository.save(user); // Save the student's newly cleaned desk
            }
        }
        
        // 2. THE NUCLEAR OPTION: Delete the course. 
        // (Because of CascadeType.ALL, this automatically deletes all Modules and Lessons too!)
        courseRepository.deleteById(courseId);
        
        return "redirect:/instructor/courses";
    }
    
 // ==========================================
    // VIEW ENROLLED STUDENTS (COURSE ROSTER)
    // ==========================================
    @GetMapping("/{courseId}/students")
    public String viewEnrolledStudents(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        
        // 1. Fetch all students to see who has this course on their desk
        java.util.List<User> allUsers = userRepository.findAll();
        java.util.List<User> enrolledStudents = new java.util.ArrayList<>();
        java.util.Map<Long, Integer> studentProgress = new java.util.HashMap<>();
        
        // 2. Count total lessons in the course
        int totalLessons = 0;
        for (CourseModule m : course.getModules()) {
            totalLessons += m.getLessons().size();
        }
        
        // 3. Find the enrolled students and calculate their progress
        for (User user : allUsers) {
            if (user.getEnrolledCourses().contains(course)) {
                enrolledStudents.add(user);
                
                // Calculate this specific student's progress
                int completed = 0;
                if (totalLessons > 0) {
                    for (CourseModule m : course.getModules()) {
                        for (Lesson l : m.getLessons()) {
                            if (user.getCompletedLessons().contains(l)) {
                                completed++;
                            }
                        }
                    }
                }
                
                int progress = (totalLessons == 0) ? 0 : (int) Math.round((double) completed / totalLessons * 100);
                studentProgress.put(user.getId(), progress);
            }
        }
        
        model.addAttribute("course", course);
        model.addAttribute("enrolledStudents", enrolledStudents);
        model.addAttribute("studentProgress", studentProgress);
        
        return "course-students";
    }
    
 // ==========================================
    // VIEW COURSE REVIEWS
    // ==========================================
    @GetMapping("/{courseId}/reviews")
    public String viewCourseReviews(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        
        // Pass the course and its reviews to the template
        model.addAttribute("course", course);
        model.addAttribute("reviews", course.getReviews());
        
        return "course-reviews";
    }
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}/reply")
    public String replyToDiscussion(@PathVariable Long courseId, @PathVariable Long moduleId, @PathVariable Long lessonId, @RequestParam String content, @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // The service handles the exact same logic for instructors
        commentService.addComment(userDetails.getUser().getId(), lessonId, content);
        
        return "redirect:/instructor/courses/" + courseId + "/modules/" + moduleId + "/lessons/" + lessonId;
    }
}