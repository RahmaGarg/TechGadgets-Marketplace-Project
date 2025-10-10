package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProfileCompletionInterceptor implements HandlerInterceptor {
    
    private final UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws IOException {
        
        // Récupérer l'authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si pas authentifié, laisser passer (géré par Spring Security)
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }
        
        String email = authentication.getName();
        String path = request.getRequestURI();
        
        // Autoriser les endpoints suivants sans vérification
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/api/profile/complete") ||
            path.startsWith("/api/profile") ||
            path.equals("/api/auth/logout")) {
            return true;
        }
        
        // Vérifier si le profil est complet
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null && !user.getIsProfileCompleted()) {
            // Profil incomplet - bloquer l'accès
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Profile incomplete\", " +
                "\"message\": \"Veuillez compléter votre profil avant d'accéder à cette ressource\", " +
                "\"redirectTo\": \"/complete-profile\"}"
            );
            return false;
        }
        
        return true;
    }
}