package com.example.demo.services;

import com.example.demo.entities.PasswordResetToken;
import com.example.demo.entities.User;
import com.example.demo.repositories.PasswordResetTokenRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.validators.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    
    @Value("${app.reset-token.expiration:3600000}")
    private long tokenExpirationMs;
    
    @Value("${app.reset-token.max-requests-per-hour:3}")
    private int maxRequestsPerHour;
    
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Vérifier le nombre de demandes récentes
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = tokenRepository.countRecentRequestsByUser(user, oneHourAgo);
        
        if (recentRequests >= maxRequestsPerHour) {
            throw new RuntimeException(
                "Trop de demandes de réinitialisation. Veuillez réessayer plus tard."
            );
        }
        
        // Vérifier s'il existe déjà un token actif valide
        List<PasswordResetToken> activeTokens = tokenRepository
                .findByUserAndUsedFalseAndExpiryDateAfter(user, LocalDateTime.now());
        
        if (!activeTokens.isEmpty()) {
            // Invalider les anciens tokens
            tokenRepository.invalidateAllUserTokens(user);
            log.info("Invalidated {} active tokens for user: {}", activeTokens.size(), email);
        }
        
        // Créer un nouveau token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(tokenExpirationMs / 1000);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();
        
        tokenRepository.save(resetToken);
        
        // Envoyer l'email
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getName());
        
        log.info("Password reset token created for user: {}", email);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        // Vérifier que les mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        // Valider le nouveau mot de passe
        passwordValidator.validate(newPassword);
        
        // Récupérer le token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));
        
        // Vérifier si le token est valide
        if (resetToken.isUsed()) {
            throw new RuntimeException("Ce token a déjà été utilisé");
        }
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Ce token a expiré");
        }
        
        // Mettre à jour le mot de passe
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);
        
        // Invalider tous les autres tokens de l'utilisateur
        tokenRepository.invalidateAllUserTokens(user);
        
        log.info("Password reset successful for user: {}", user.getEmail());
    }
    
    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));
        
        if (!resetToken.isValid()) {
            throw new RuntimeException("Token invalide ou expiré");
        }
    }
    
    // Nettoyage automatique des tokens expirés (chaque jour à 2h du matin)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        tokenRepository.deleteExpiredTokens(cutoffDate);
        log.info("Cleaned up expired password reset tokens");
    }
}