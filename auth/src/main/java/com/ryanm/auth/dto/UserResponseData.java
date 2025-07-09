package com.ryanm.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseData {
    private Long id;
    private String username;
    private String email;
    private String token;
}
