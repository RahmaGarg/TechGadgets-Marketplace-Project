package com.example.demo.events;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerRegisteredEvent {
    private Long userId;           // Changed from sellerId
    private String email;
    private String name;            // Changed from businessName
    private LocalDateTime registeredAt;  // Added
}