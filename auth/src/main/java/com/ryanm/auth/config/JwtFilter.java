package com.ryanm.auth.config;

import com.ryanm.auth.service.JwtService;
import com.ryanm.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Step 1: Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Step 2: Check if the header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token provided, continue to next filter
            // This allows public endpoints to work normally
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7); // "Bearer ".length() = 7
        
        try {
            // Step 4: Extract username from the JWT token
            username = jwtService.extractUsername(jwt);

            // Step 5: Check if username exists and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Step 6: Load user details from database
                UserDetails userDetails = this.userService.loadUserByUsername(username);

                // Step 7: Validate the token against the user
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    
                    // Step 8: Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No password needed (token is proof)
                            userDetails.getAuthorities()
                    );
                    
                    // Step 9: Set authentication details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Step 10: Set the authentication in Security Context
                    // This tells Spring Security: "This user is authenticated!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If anything goes wrong with token validation, just continue
            // The request will be treated as unauthenticated
            logger.error("JWT Authentication failed: " + e.getMessage());
        }

        // Step 11: Continue to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}