package com.example.demo.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.ChangePasswordRequest;
import com.example.demo.dtos.CompleteProfileRequest;
import com.example.demo.dtos.ProfileResponse;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.validators.PasswordValidator;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    
    @Transactional
    public ProfileResponse completeProfile(String email, CompleteProfileRequest request) {
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is ADMIN
        boolean isAdmin = user.getRole() != null && user.getRole().equals("ADMIN");
        
        // Vérifier si le profil n'est pas déjà complété (skip for ADMIN)
        if (user.getIsProfileCompleted() && !isAdmin) {
            throw new RuntimeException("Profile already completed");
        }
        
        // Mettre à jour les informations (only for non-ADMIN users)
        if (!isAdmin) {
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setCity(request.getCity());
            user.setCountry(request.getCountry());
        }
        
        // Set profile completed flag
        user.setIsProfileCompleted(true);
        user.setProfileCompletedAt(LocalDateTime.now());
        
        // Sauvegarder
        user = userRepository.save(user);
        
        // Retourner la réponse
        return mapToProfileResponse(user);
    }
    
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return mapToProfileResponse(user);
    }
    
    @Transactional
    public ProfileResponse updateProfile(String email, CompleteProfileRequest request) {
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is ADMIN
        boolean isAdmin = user.getRole() != null && user.getRole().equals("ADMIN");
        
        // Mettre à jour les informations (only for non-ADMIN users)
        if (!isAdmin) {
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setCity(request.getCity());
            user.setCountry(request.getCountry());
        }
        
        // Vérifier si le profil est maintenant complet
        if ((isAdmin || user.canMarkProfileAsCompleted()) && !user.getIsProfileCompleted()) {
            user.setIsProfileCompleted(true);
            user.setProfileCompletedAt(LocalDateTime.now());
        }
        
        // Sauvegarder
        user = userRepository.save(user);
        
        return mapToProfileResponse(user);
    }

    @Transactional
    public Map<String, String> changePassword(String email, ChangePasswordRequest request) {
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que le nouveau mot de passe correspond à la confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Vérifier que l'ancien mot de passe est correct
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        // Valider le nouveau mot de passe
        passwordValidator.validate(request.getNewPassword());

        // Encoder et sauvegarder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return Map.of("message", "Password changed successfully");
    }

    
    private ProfileResponse mapToProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setCity(user.getCity());
        response.setCountry(user.getCountry());
        response.setIsProfileCompleted(user.getIsProfileCompleted());
        return response;
    }
}