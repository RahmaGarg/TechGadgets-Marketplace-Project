package com.example.demo.repositories;

import com.example.demo.entities.PasswordResetToken;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(
        User user, 
        LocalDateTime now
    );
    
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.user = :user " +
           "AND p.createdAt > :since")
    long countRecentRequestsByUser(User user, LocalDateTime since);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user " +
           "AND p.used = false")
    void invalidateAllUserTokens(User user);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :date")
    void deleteExpiredTokens(LocalDateTime date);
}