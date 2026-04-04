package com.online.MiniUdemy.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // Get the roles of the currently logged-in user
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Redirect based on role
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
                return;
            } else if (role.equals("ROLE_INSTRUCTOR")) {
                response.sendRedirect("/instructor/dashboard");
                return;
            } else if (role.equals("ROLE_STUDENT")) {
                response.sendRedirect("/student/dashboard");
                return;
            }
        }

        // Fallback if no specific role matched
        response.sendRedirect("/");
    }
}