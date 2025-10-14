package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.example.demo.enums.OrderStatus;

//Problem:
//An admin has 50 orders with status CONFIRMED and wants to mark them all as SHIPPED.
//Without this DTO, they would need to call the API 50 times (/api/orders/1/status, /api/orders/2/status, ...).

//Solution:
//This DTO allows updating multiple orders in a single request, saving time and network resources.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkStatusUpdateRequest {
    private List<Long> orderIds;
    private OrderStatus newStatus;
}