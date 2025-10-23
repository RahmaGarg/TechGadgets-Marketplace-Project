package com.example.demo.events;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.demo.enums.RoleType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerRegisteredEvent {
    private Long userId;
    private String email;
    private String name;
    private LocalDateTime registeredAt;
}
