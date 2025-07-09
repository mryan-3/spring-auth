package com.ryanm.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ryanm.auth.dto.ApiResponse;
import com.ryanm.auth.dto.LoginRequest;
import com.ryanm.auth.dto.RefreshTokenRequest;
import com.ryanm.auth.dto.SignupRequest;
import com.ryanm.auth.dto.UserResponseData;
import com.ryanm.auth.model.UserModel;
import com.ryanm.auth.repository.UserRepository;
import com.ryanm.auth.service.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository repository;

    
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseData>> signup(@RequestBody SignupRequest signupRequest) {
        try {
            // Check if username already exists
            if (repository.findByUsername(signupRequest.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username already exists"));
            }

            // Create new user
            UserModel user = new UserModel();
            user.setUsername(signupRequest.getUsername());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

            // Save user
            UserModel savedUser = repository.save(user);

            String accessToken = jwtService.generateToken(
                savedUser.getUsername(), 
                savedUser.getId()
            );
            String refreshToken = jwtService.generateRefreshToken(
                savedUser.getUsername(), 
                savedUser.getId()
            );
            
            // Create response data (without password)
            UserResponseData responseData = new UserResponseData(savedUser.getId(), 
                savedUser.getUsername(), 
                savedUser.getEmail(),
                accessToken,
                refreshToken);
            
            // Return standardized success response
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", responseData));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseData>> login(@RequestBody LoginRequest req) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                req.getUsername(), 
                req.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(authToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserModel user = repository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String accessToken = jwtService.generateToken(
                user.getUsername(), 
                user.getId()
            );
            String refreshToken = jwtService.generateRefreshToken(
                user.getUsername(), 
                user.getId()
            );


            UserResponseData responseData = new UserResponseData(user.getId(), 
                user.getUsername(), 
                user.getEmail(), accessToken, refreshToken);

            return ResponseEntity.ok(ApiResponse.success("Login successful", responseData));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
        catch (Exception e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<UserResponseData>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // Generate new access token using refresh token
            String newAccessToken = jwtService.refreshAccessToken(request.getRefreshToken());
            
            // Extract user info from refresh token
            String username = jwtService.extractUsername(request.getRefreshToken());
            UserModel user = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Create response (keep the same refresh token)
            UserResponseData userData = new UserResponseData(
                user.getId(), 
                user.getUsername(), 
                user.getEmail(),
                newAccessToken,
                request.getRefreshToken()
            );
            

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", userData));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseData>> getProfile() {
        try {
            // Step 1: Get authentication from Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Step 2: Check if authentication exists (should not be null if JWT filter worked)
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
            }
            
            // Step 3: Extract username from authentication
            String username = authentication.getName();
            
            // Step 4: Check if username is valid
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication data"));
            }
            
            // Step 5: Find user in database (this can throw exception)
            UserModel user = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User account not found. Account may have been deleted."));

            // Step 6: Create response data (don't include tokens in profile response)
            UserResponseData userData = new UserResponseData(
                user.getId(), 
                user.getUsername(), 
                user.getEmail(),
                null,  // No need to return tokens in profile
                null   // No need to return tokens in profile
            );

            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userData));
            
        } catch (RuntimeException e) {
            // Handle specific business logic errors (like user not found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Profile not found: " + e.getMessage()));
                
        } catch (Exception e) {
            // Handle unexpected errors (database issues, etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve profile: " + e.getMessage()));
        }
    }
}
