package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(buildEmailContent(userName, resetLink));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send reset email", e);
        }
    }
    
    private String buildEmailContent(String userName, String resetLink) {
        return String.format(
            "Bonjour %s,\n\n" +
            "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
            "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
            "%s\n\n" +
            "Ce lien expirera dans 1 heure.\n\n" +
            "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
            "Cordialement,\n" +
            "L'équipe",
            userName,
            resetLink
        );
    }
}