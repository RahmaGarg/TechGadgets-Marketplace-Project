package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerStatsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
}
