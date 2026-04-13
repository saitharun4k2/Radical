package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal; // Added this import for Spring Security

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UserValidator userValidator;

    public AdminController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    // ==========================================
    // UPDATED DASHBOARD METHOD
    // ==========================================
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, Principal principal) {
        User currentUser = null;

        // 1. Try to get the actual logged-in user from Spring Security
        if (principal != null) {
            // NOTE: Make sure 'findByEmail' matches the actual method name in your UserService!
            // It might be findByUsername, getUserById, etc. depending on how you wrote it.
            currentUser = userService.findByEmail(principal.getName());
        }

        // 2. Fallback: If no user is found, create a dummy so Thymeleaf doesn't crash (500 Error)
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setName("System Admin");
        }

        // 3. Add the user to the model so the HTML template can use ${user.name}
        model.addAttribute("user", currentUser);

        return "admin-dashboard";
    }

    @GetMapping("/create-instructor")
    public String showCreateInstructorForm(Model model) {
        model.addAttribute("instructor", new User());
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