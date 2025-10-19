package com.example.demo.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Récupérer toutes les notifications d'un utilisateur
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Récupérer les notifications non lues d'un utilisateur
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Compter les notifications non lues
    Long countByUserIdAndIsReadFalse(Long userId);
    
    // Récupérer par rôle (pour admin par exemple)
    List<Notification> findByUserRoleOrderByCreatedAtDesc(String userRole);
}