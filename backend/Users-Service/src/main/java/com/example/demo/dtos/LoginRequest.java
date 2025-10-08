package com.example.demo.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
