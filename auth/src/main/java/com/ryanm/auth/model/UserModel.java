package com.ryanm.auth.model;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull 
    @Column(unique = true, nullable = false)
    private String username;
    @NonNull
    @Column(unique = true, nullable = false)
    private String email;
    private String password;

}
