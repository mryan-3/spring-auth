package com.ryanm.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ryanm.auth.service.UserService;

import lombok.RequiredArgsConstructor;


@Configuration //indicates that this class contains Spring Security configuration
@EnableWebSecurity //enables Spring Security's web security support and provides the Spring MVC integration
@RequiredArgsConstructor
public  class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserService userService; //injects the UserService to handle user details

    @Bean
    public UserDetailsService userDetailsService() {
        return userService; 
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder()); //sets the PasswordEncoder to be used for encoding passwords
        return provider; //returns the configured DaoAuthenticationProvider
    } //provides an authentication provider that uses the UserDetailsService to authenticate users

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //returns a BCryptPasswordEncoder for password hashing
    } //provides a password encoder bean to be used for encoding passwords

    @Bean
     public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/api/users/signup", "/api/users/login",  "/api/users/refresh-token",  // 👈 Add refresh endpoint
                    "/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()))
                ;
        return http.build();
    }//abstract method to be implemented by subclasses to define the security filter chain
}
