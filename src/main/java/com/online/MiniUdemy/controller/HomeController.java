package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CourseService courseService;

    public HomeController(CourseService courseService) {
        this.courseService = courseService;
    }

    // This maps to both "localhost:8080/" and "localhost:8080/home"
    @GetMapping({"/", "/home"})
    public String showHomePage(Model model) {
        // Grab every course in the database and send it to the HTML page
        model.addAttribute("courses", courseService.getAllCourses());
        return "home";
    }
}