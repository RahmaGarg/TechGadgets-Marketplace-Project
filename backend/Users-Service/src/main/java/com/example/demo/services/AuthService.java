package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.enums.RoleType;
import com.example.demo.events.SellerRegisteredEvent;
import com.example.demo.events.UserRegisteredEvent;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.validators.PasswordValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordValidator passwordValidator;
    private final KafkaEventPublisher eventPublisher;


    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Valider le mot de passe
        passwordValidator.validate(request.getPassword());
        
        // Récupérer le rôle
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Créer l'utilisateur
        // Les admins n'ont pas besoin de compléter leur profil
        boolean isAdmin = role.getName() == RoleType.ADMIN;
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isProfileCompleted(isAdmin)  // true pour ADMIN, false pour les autres
                .build();
        
        user = userRepository.save(user);
        //  PUBLIEZ L'ÉVÉNEMENT KAFKA
        if (role.getName() == RoleType.SELLER) {
            SellerRegisteredEvent event = SellerRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .registeredAt(LocalDateTime.now())
                .build();
            eventPublisher.publishSellerRegisteredEvent(event);
        } else if (role.getName() == RoleType.CLIENT) {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(role.getName())
                .registeredAt(LocalDateTime.now())
                .build();
            eventPublisher.publishUserRegisteredEvent(event);
        }
        
        // Générer le token
        String token = jwtTokenProvider.generateToken(user.getEmail(), role.getName().name());
        
        // Créer la réponse complète avec redirectTo
        return buildAuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        // Authentifier
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Générer le token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().getName().name());
        
        // Créer la réponse complète avec redirectTo
        return buildAuthResponse(token, user);
    }

    /**
     * Construit la réponse d'authentification avec tous les champs nécessaires
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setRole(user.getRole().getName());
        response.setIsProfileCompleted(user.getIsProfileCompleted() != null ? user.getIsProfileCompleted() : false);
        response.setRedirectTo(determineRedirectPath(user));
        
        return response;
    }

    /**
     * Détermine le chemin de redirection basé sur le statut du profil et le rôle
     */
    private String determineRedirectPath(User user) {
        // Si le profil n'est pas complété, rediriger vers la page de complétion
        if (user.getIsProfileCompleted() == null || !user.getIsProfileCompleted()) {
            return "/complete-profile";
        }
        
        // Si le profil est complété, rediriger selon le rôle
        RoleType roleType = user.getRole().getName();
        return switch (roleType) {
            case ADMIN -> "/admin/dashboard";
            case CLIENT -> "/client/dashboard";
            case SELLER -> "/freelancer/dashboard";
            default -> "/dashboard";
        };
    }
}