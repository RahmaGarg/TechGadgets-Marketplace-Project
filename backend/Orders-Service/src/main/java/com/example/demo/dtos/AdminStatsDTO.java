package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Long totalOrders;
    private Long totalClients;
    private Long totalSellers;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private Long pendingOrders;
    private Long cancelledOrders;
}