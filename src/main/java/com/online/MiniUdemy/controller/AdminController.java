package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.service.UserService;
import com.online.MiniUdemy.controller.UserValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin") 
public class AdminController {

    private final UserService userService;
    private final UserValidator userValidator;

    public AdminController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping("/create-instructor")
    public String showCreateInstructorForm(Model model) {
        model.addAttribute("instructor", new User());
        // This looks for src/main/resources/templates/admin/create-instructor.html
        return "create-instructor"; 
    }

    @PostMapping("/create-instructor")
    public String processCreateInstructor(@ModelAttribute("instructor") User instructor, 
                                          BindingResult result, 
                                          RedirectAttributes ra) {
        
        instructor.setRole(Role.INSTRUCTOR);
        userValidator.validate(instructor, result);
        
        if (result.hasErrors()) {
            return "create-instructor";
        }

        userService.saveUser(instructor);
        ra.addFlashAttribute("success", "Instructor account created successfully!");
        return "redirect:/admin/dashboard";
    }
}