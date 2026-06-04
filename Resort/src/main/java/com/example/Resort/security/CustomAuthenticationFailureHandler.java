package com.example.Resort.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String cause = "system";

        if (exception instanceof UsernameNotFoundException) {
            cause = "username";
        } else if (exception instanceof BadCredentialsException) {
            cause = "password";
        }

        String encoded = URLEncoder.encode(cause, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?error=true&cause=" + encoded);
    }
}
