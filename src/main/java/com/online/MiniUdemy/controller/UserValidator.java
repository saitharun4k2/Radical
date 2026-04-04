package com.online.MiniUdemy.controller;


import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty", "Full name is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty", "Email is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty", "Password is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "role", "NotEmpty", "Account type is required.");

        if (user.getEmail() != null && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "Pattern.user.email", "Please enter a valid email address.");
        }

        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            errors.rejectValue("email", "Duplicate.user.email", "An account with this email already exists.");
        }

        if (user.getPassword() != null && user.getPassword().length() < 6) {
            errors.rejectValue("password", "Size.user.password", "Password must be at least 6 characters long.");
        }
    }
}