package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.enums.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
 private Long id;
 private Long clientId;
 private Long sellerId;
 private OrderStatus status;
 private BigDecimal totalPrice;
 private String shippingAddress;
 private LocalDateTime createdAt;
 private List<OrderItemDTO> items;
}
