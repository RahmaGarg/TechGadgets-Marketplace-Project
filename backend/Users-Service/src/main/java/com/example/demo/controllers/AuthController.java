package com.example.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dtos.*;
import com.example.demo.services.AuthService;
import com.example.demo.services.PasswordResetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok(
            "Si cet email existe, un lien de réinitialisation a été envoyé"
        );
    }
    
    @GetMapping("/reset-password/validate")
    public ResponseEntity<String> validateResetToken(@RequestParam String token) {
        passwordResetService.validateToken(token);
        return ResponseEntity.ok("Token valide");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetConfirm request) {
        passwordResetService.resetPassword(
            request.getToken(),
            request.getNewPassword(),
            request.getConfirmPassword()
        );
        return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
    }
}