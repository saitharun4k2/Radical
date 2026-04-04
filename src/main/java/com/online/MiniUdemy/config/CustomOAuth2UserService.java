package com.online.MiniUdemy.config;

import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID; // ADDED: Required for generating the dummy password

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Fetch user data from Google
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. Extract email and name
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // 3. Check if user already exists in our database using Optional
        Optional<User> optionalUser = userRepository.findByEmail(email);

        User user; // Declare the actual User object

        if (optionalUser.isEmpty()) {
            // 4. If new user, create an account automatically
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole(Role.STUDENT);

            // FIXED: Generate a highly secure, random password for Google users.
            // This prevents the "Column 'password' cannot be null" database crash!
            user.setPassword(UUID.randomUUID().toString());

            userRepository.save(user); // Save the actual User
        } else {
            // If they exist, extract the User object from the Optional wrapper
            user = optionalUser.get();
        }

        // 5. Return our CustomUserDetails so your SuccessHandler works perfectly
        return new CustomUserDetails(user, oauth2User.getAttributes());
    }
}