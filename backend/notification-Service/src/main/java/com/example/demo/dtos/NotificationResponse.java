package com.example.demo.dtos;

import java.time.LocalDateTime;

import com.example.demo.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
 public class NotificationResponse {
    private Long id;
    private Long userId;
    private String userRole;
    private NotificationType type;
    private String title;
    private String message;
    private String metadata;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
