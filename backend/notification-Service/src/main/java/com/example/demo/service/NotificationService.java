package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.dtos.NotificationResponse;
import com.example.demo.dtos.WebSocketNotification;
import com.example.demo.entities.Notification;
import com.example.demo.repositories.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;
    
    /**
     * Créer et envoyer une notification
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {} with type: {}", 
                request.getUserId(), request.getType());
        
        // Sauvegarder en DB
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Envoyer via WebSocket en temps réel
        WebSocketNotification wsNotif = WebSocketNotification.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .timestamp(notification.getCreatedAt())
                .build();
        
        webSocketService.sendNotificationToUser(request.getUserId(), wsNotif);
        
        return mapToResponse(notification);
    }
    
    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les notifications non lues
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Compter les notifications non lues
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    /**
     * Marquer une notification comme lue
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return mapToResponse(notificationRepository.save(notification));
    }
    
    /**
     * Marquer toutes les notifications comme lues
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        notifications.forEach(notif -> {
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(notifications);
    }
    
    /**
     * Supprimer une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    // ===== HELPER METHODS =====
    
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .userRole(notification.getUserRole())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}