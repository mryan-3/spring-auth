package com.ryanm.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ryanm.auth.model.UserModel;
import com.ryanm.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public UserModel signup(@RequestBody UserModel req) {
        req.setPassword(passwordEncoder.encode(req.getPassword()));
        return repository.save(req);
    }

    @PostMapping("/login")
    public String login(@RequestBody String req) {
        //TODO: process POST request
        
        return req;
    }
    
    
}
