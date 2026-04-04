package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.config.CustomUserDetails;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Displays the profile page
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Fetch fresh user from DB to get the most up-to-date enrolled courses
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        if (optionalUser.isPresent()) {
            model.addAttribute("user", optionalUser.get());
            return "profile";
        }

        return "redirect:/login";
    }

    // Handles the name update form submission
    @PostMapping("/profile/update-name")
    public String updateName(@RequestParam("name") String newName,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // Update the user's name
                user.setName(newName);
                userRepository.save(user);
            }
        }
        // Redirect back to profile with a success flag
        return "redirect:/profile?updated=true";
    }
}