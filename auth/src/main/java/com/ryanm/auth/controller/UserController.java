package com.ryanm.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ryanm.auth.dto.ApiResponse;
import com.ryanm.auth.dto.LoginRequest;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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

            String token = jwtService.generateToken(
                savedUser.getUsername(), 
                savedUser.getId()
            );
            
            // Create response data (without password)
            UserResponseData responseData = new UserResponseData(savedUser.getId(), 
                savedUser.getUsername(), 
                savedUser.getEmail(),
                token);
            
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

            String token = jwtService.generateToken(
                user.getUsername(), 
                user.getId()
            );


            UserResponseData responseData = new UserResponseData(user.getId(), 
                user.getUsername(), 
                user.getEmail(), token);

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
    
    
}
