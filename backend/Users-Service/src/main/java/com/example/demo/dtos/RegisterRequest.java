package com.example.demo.dtos;
import lombok.Data;
import com.example.demo.enums.RoleType;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private RoleType role; 
}