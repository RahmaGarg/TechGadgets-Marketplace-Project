package com.example.demo.events;

import lombok.*;

import java.time.LocalDateTime;

import com.example.demo.enums.RoleType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String email;
    private String name;
    private RoleType role;
    private LocalDateTime registeredAt;
}