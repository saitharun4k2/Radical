package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.entity.InstructorApplication;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.ApplicationStatus;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.repository.InstructorApplicationRepository;
import com.online.MiniUdemy.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UserValidator userValidator;
    private final InstructorApplicationRepository instructorApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserService userService,
                           UserValidator userValidator,
                           InstructorApplicationRepository instructorApplicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userValidator = userValidator;
        this.instructorApplicationRepository = instructorApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // DASHBOARD METHOD
    // ==========================================
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, Principal principal) {
        User currentUser = null;

        if (principal != null) {
            currentUser = userService.findByEmail(principal.getName());
        }

        if (currentUser == null) {
            currentUser = new User();
            currentUser.setName("System Admin");
        }

        model.addAttribute("user", currentUser);

        return "admin-dashboard";
    }

    // ==========================================
    // CREATE INSTRUCTOR (MANUAL & FROM APP)
    // ==========================================
    @GetMapping("/create-instructor")
    public String showCreateInstructorForm(Model model) {
        model.addAttribute("instructor", new User());
        return "create-instructor";
    }

    @PostMapping("/create-instructor")
    public String processCreateInstructor(@ModelAttribute("instructor") User instructor,
                                          BindingResult result,
                                          @RequestParam(value = "pendingAppId", required = false) Long pendingAppId,
                                          Model model,
                                          RedirectAttributes ra) {

        instructor.setRole(Role.INSTRUCTOR);
        userValidator.validate(instructor, result);

        if (result.hasErrors()) {
            // If validation fails, we must pass the pendingAppId back to the view
            // so it isn't lost when the user fixes their errors and resubmits.
            if (pendingAppId != null) {
                model.addAttribute("pendingAppId", pendingAppId);
            }
            return "create-instructor";
        }

        // Assuming your userService doesn't automatically encode inside saveUser,
        // it's best practice to encode the raw password from the form here.
        if (instructor.getPassword() != null && !instructor.getPassword().isEmpty()) {
            instructor.setPassword(passwordEncoder.encode(instructor.getPassword()));
        }

        userService.saveUser(instructor);

        // Workflow Hook: If this creation came from an application, mark it as approved
        if (pendingAppId != null) {
            InstructorApplication app = instructorApplicationRepository.findById(pendingAppId).orElse(null);
            if (app != null) {
                app.setStatus(ApplicationStatus.APPROVED);
                instructorApplicationRepository.save(app);
            }
        }

        ra.addFlashAttribute("success", "Instructor account created successfully!");
        return "redirect:/admin/dashboard";
    }

    // ==========================================
    // INSTRUCTOR APPLICATIONS (APPROVAL WORKFLOW)
    // ==========================================
    @GetMapping("/applications")
    public String viewPendingApplications(Model model) {
        List<InstructorApplication> pendingApps = instructorApplicationRepository.findByStatus(ApplicationStatus.PENDING);
        model.addAttribute("applications", pendingApps);
        return "admin-applications";
    }

    @PostMapping("/applications/approve/{id}")
    public String prepareInstructorCreation(@PathVariable Long id, Model model, RedirectAttributes ra) {
        InstructorApplication application = instructorApplicationRepository.findById(id).orElse(null);

        if (application == null) {
            ra.addFlashAttribute("error", "Application not found.");
            return "redirect:/admin/applications";
        }

        // 1. Create a new User object and pre-fill the applicant's details
        User newInstructor = new User();
        newInstructor.setName(application.getFullName());
        newInstructor.setEmail(application.getEmail());

        // 2. Send the pre-filled object to the form
        model.addAttribute("instructor", newInstructor);

        // 3. Pass the application ID so the form can send it back upon submission
        model.addAttribute("pendingAppId", id);

        // 4. Render the create-instructor HTML page directly
        return "create-instructor";
    }
}