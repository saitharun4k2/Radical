package com.online.MiniUdemy.controller;

import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.repository.UserRepository;
import com.online.MiniUdemy.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.Random;

@Controller
public class AuthController {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final EmailService emailService;

	public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	// Displays the login page
	@GetMapping("/login")
	public String showLoginForm() {
		return "login";
	}
	// ==========================================
	// FORGOT PASSWORD FLOW
	// ==========================================

	// 1. Show email request form
	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "forgot-password";
	}

	// 1. Process email and send OTP
	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam("email") String email, HttpSession session, Model model) {
		Optional<User> optionalUser = userRepository.findByEmail(email);

		if (optionalUser.isEmpty()) {
			model.addAttribute("error", "No account found with that email address.");
			return "forgot-password";
		}

		// Generate 6-digit OTP
		String otp = String.format("%06d", new Random().nextInt(999999));

		// Save to session
		session.setAttribute("resetPasswordEmail", email);
		session.setAttribute("resetPasswordOtp", otp);

		// Send Email
		emailService.sendOtpEmail(email, otp);

		return "redirect:/reset-password-otp";
	}

	// 2. Show OTP verification form
	@GetMapping("/reset-password-otp")
	public String showResetPasswordOtpForm(HttpSession session) {
		if (session.getAttribute("resetPasswordEmail") == null) {
			return "redirect:/forgot-password";
		}
		return "reset-password-otp";
	}

	// 2. Verify OTP
	@PostMapping("/reset-password-otp")
	public String processResetPasswordOtp(@RequestParam("otp") String enteredOtp, HttpSession session, Model model) {
		String sessionOtp = (String) session.getAttribute("resetPasswordOtp");

		if (sessionOtp != null && sessionOtp.equals(enteredOtp)) {
			session.setAttribute("isOtpVerified", true); // Flag to allow access to final step
			return "redirect:/reset-password";
		} else {
			model.addAttribute("error", "Invalid OTP. Please try again.");
			return "reset-password-otp";
		}
	}

	// 3. Show new password form
	@GetMapping("/reset-password")
	public String showResetPasswordForm(HttpSession session) {
		Boolean isVerified = (Boolean) session.getAttribute("isOtpVerified");
		if (isVerified == null || !isVerified) {
			return "redirect:/forgot-password";
		}
		return "reset-password";
	}

	// 3. Save new password
	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam("password") String newPassword, HttpSession session) {
		String email = (String) session.getAttribute("resetPasswordEmail");

		if (email != null) {
			Optional<User> optionalUser = userRepository.findByEmail(email);
			if (optionalUser.isPresent()) {
				User user = optionalUser.get();
				// Encrypt the new password and save
				user.setPassword(passwordEncoder.encode(newPassword));
				userRepository.save(user);
			}
		}

		// Clean up session
		session.removeAttribute("resetPasswordEmail");
		session.removeAttribute("resetPasswordOtp");
		session.removeAttribute("isOtpVerified");

		return "redirect:/login?resetSuccess=true";
	}

	// Displays the registration page
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	// Intercepts the registration form submission
	@PostMapping("/register")
	public String processRegistration(@ModelAttribute User user, Model model, HttpSession session) {
		// Check if email already exists
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			model.addAttribute("error", "Email already in use.");
			return "register";
		}

		// Generate 6-digit OTP
		String otp = String.format("%06d", new Random().nextInt(999999));

		// Encrypt password now so it's ready to save later
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(Role.STUDENT); // Default new registrations to Student

		// Save User object and OTP in the HTTP Session temporarily
		session.setAttribute("pendingUser", user);
		session.setAttribute("registrationOtp", otp);

		// Send the OTP via Email
		emailService.sendOtpEmail(user.getEmail(), otp);

		return "redirect:/verify-otp";
	}

	// Displays the OTP verification page
	@GetMapping("/verify-otp")
	public String showVerifyOtpPage(HttpSession session, Model model) {
		// Prevent direct access if no user is in session
		if (session.getAttribute("pendingUser") == null) {
			return "redirect:/register";
		}
		return "verify-otp";
	}

	// Processes the entered OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String enteredOtp, HttpSession session, Model model) {
		String sessionOtp = (String) session.getAttribute("registrationOtp");
		User pendingUser = (User) session.getAttribute("pendingUser");

		if (sessionOtp != null && sessionOtp.equals(enteredOtp)) {
			// OTP is correct! Create the user in the database
			userRepository.save(pendingUser);

			// Clean up session
			session.removeAttribute("pendingUser");
			session.removeAttribute("registrationOtp");

			return "redirect:/login?verified=true";
		} else {
			// OTP is wrong
			model.addAttribute("error", "Invalid OTP. Please try again.");
			return "verify-otp";
		}
	}
}