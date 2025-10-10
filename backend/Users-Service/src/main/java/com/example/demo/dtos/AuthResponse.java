package com.example.demo.dtos;

import com.example.demo.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private RoleType role;
    
    // Nouveaux champs
    private Boolean isProfileCompleted;
    private String redirectTo;
    
    // Constructeur pour la compatibilité avec l'ancien code
    public AuthResponse(String token, String email, String name, RoleType role) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}