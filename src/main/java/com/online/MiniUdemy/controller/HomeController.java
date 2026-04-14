package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.entity.InstructorApplication;
import com.online.MiniUdemy.service.CourseService;
import com.online.MiniUdemy.service.InstructorApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final InstructorApplicationService applicationService;

    // Inject the Services ONLY (No Entities!)
    public HomeController(CourseService courseService, InstructorApplicationService applicationService) {
        this.courseService = courseService;
        this.applicationService = applicationService;
    }

    // This maps to both "localhost:8080/" and "localhost:8080/home"
    @GetMapping({"/", "/home"})
    public String showHomePage(Model model) {
        // Grab every course in the database and send it to the HTML page
        model.addAttribute("courses", courseService.getAllCourses());
        return "home";
    }

    @GetMapping("/apply-instructor")
    public String showInstructorApplicationForm(Model model) {
        // Pass a fresh entity for Thymeleaf to bind the form inputs to
        model.addAttribute("application", new InstructorApplication());
        return "instructor-application";
    }

    @PostMapping("/apply-instructor")
    public String submitInstructorApplication(@ModelAttribute("application") InstructorApplication application,
                                              @RequestParam("resume") MultipartFile resume,
                                              RedirectAttributes ra) {
        try {
            // Hand off the business logic to the service
            applicationService.submitApplication(application, resume);

            // Show success message on the home page
            ra.addFlashAttribute("success", "Your application has been submitted successfully! We will review it shortly.");
            return "redirect:/";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "There was an error submitting your application.");
            return "redirect:/apply-instructor";
        }
    }
}