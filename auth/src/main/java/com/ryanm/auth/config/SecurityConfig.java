package com.ryanm.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ryanm.auth.service.UserService;


@Configuration //indicates that this class contains Spring Security configuration
@EnableWebSecurity //enables Spring Security's web security support and provides the Spring MVC integration
public  class SecurityConfig {

    @Autowired
    private UserService userService; //injects the UserService to handle user details

    @Bean
    public UserDetailsService userDetailsService() {
        return userService; 
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService()); //sets the UserDetailsService to be used for authentication
        provider.setPasswordEncoder(passwordEncoder()); //sets the PasswordEncoder to be used for encoding passwords
        return provider; //returns the configured DaoAuthenticationProvider
    } //provides an authentication provider that uses the UserDetailsService to authenticate users

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //returns a BCryptPasswordEncoder for password hashing
    } //provides a password encoder bean to be used for encoding passwords

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/api/users/signup", "/api/users/login").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }//abstract method to be implemented by subclasses to define the security filter chain
}
