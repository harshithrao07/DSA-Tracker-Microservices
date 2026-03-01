package com.harshith.dsa_question_picker.security;

import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.repository.UserRepository;
import com.harshith.dsa_question_picker.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        String jwt = null;
        String providerId = null;

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            providerId = jwtService.extractProviderId(jwt);
        } catch (Exception e) {
            // Token is invalid or expired, proceed without authentication
            chain.doFilter(request, response);
            return;
        }

        if (providerId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Optional<User> user = userRepository.findByProviderId(providerId);
                if (user.isEmpty()) {
                    throw new UsernameNotFoundException("User does not exist");
                }

                if (jwtService.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (UsernameNotFoundException e) {
                // User not found in DB, proceed without authentication
            } catch (Exception e) {
                // Handle other potential exceptions during authentication
            }
        }

        chain.doFilter(request, response);
    }
}
