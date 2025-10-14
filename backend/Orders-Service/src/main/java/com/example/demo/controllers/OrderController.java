package com.example.demo.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dtos.AdminStatsDTO;
import com.example.demo.dtos.BulkStatusUpdateRequest;
import com.example.demo.dtos.CreateOrderRequest;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.dtos.SellerStatsDTO;
import com.example.demo.enums.OrderStatus;
import com.example.demo.services.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // ==================== CRUD BASIQUE ====================
    
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("POST request to create order");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.createOrder(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        log.info("GET request to fetch order with id: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderDTO>> getClientOrders(@PathVariable Long clientId) {
        log.info("GET request to fetch orders for client: {}", clientId);
        return ResponseEntity.ok(orderService.getClientOrders(clientId));
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderDTO>> getSellerOrders(@PathVariable Long sellerId) {
        log.info("GET request to fetch orders for seller: {}", sellerId);
        return ResponseEntity.ok(orderService.getSellerOrders(sellerId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET request to fetch orders with status: {}", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }
    
    @GetMapping("/client/{clientId}/status/{status}")
    public ResponseEntity<List<OrderDTO>> getClientOrdersByStatus(
            @PathVariable Long clientId,
            @PathVariable OrderStatus status) {
        log.info("GET request to fetch client {} orders with status: {}", clientId, status);
        return ResponseEntity.ok(orderService.getClientOrdersByStatus(clientId, status));
    }
    
    @GetMapping("/seller/{sellerId}/status/{status}")
    public ResponseEntity<List<OrderDTO>> getSellerOrdersByStatus(
            @PathVariable Long sellerId,
            @PathVariable OrderStatus status) {
        log.info("GET request to fetch seller {} orders with status: {}", sellerId, status);
        return ResponseEntity.ok(orderService.getSellerOrdersByStatus(sellerId, status));
    }
    
    @PutMapping("/{id}/status/{newStatus}")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable OrderStatus newStatus) {
        log.info("PUT request to update order {} status to {}", id, newStatus);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus));
    }
    
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        log.info("DELETE request to cancel order: {}", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("DELETE request to delete order: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== SELLER ENDPOINTS ====================
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long id) {
        log.info("PUT request to confirm order: {}", id);
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }
    @PutMapping("/{id}/reject")
    public ResponseEntity<OrderDTO> rejectOrder(@PathVariable Long id) {
        log.info("PUT request to reject order: {}", id);
        return ResponseEntity.ok(orderService.rejectOrder(id));
    }
    
    @PutMapping("/{id}/ship")
    public ResponseEntity<OrderDTO> shipOrder(@PathVariable Long id) {
        log.info("PUT request to ship order: {}", id);
        return ResponseEntity.ok(orderService.shipOrder(id));
    }
    
    @GetMapping("/seller/{sellerId}/stats")
    public ResponseEntity<SellerStatsDTO> getSellerStats(@PathVariable Long sellerId) {
        log.info("GET request for seller {} statistics", sellerId);
        return ResponseEntity.ok(orderService.getSellerStats(sellerId));
    }
    
    // ==================== CLIENT ENDPOINTS ====================
    
    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderDTO> markAsDelivered(@PathVariable Long id) {
        log.info("PUT request to mark order {} as delivered", id);
        return ResponseEntity.ok(orderService.markAsDelivered(id));
    }
    
    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<Page<OrderDTO>> getClientOrderHistory(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("GET request for client {} order history - page: {}, size: {}", clientId, page, size);
        return ResponseEntity.ok(orderService.getClientOrderHistory(clientId, page, size, sortBy, sortDir));
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long sellerId) {
        log.info("GET admin request for all orders");
        return ResponseEntity.ok(orderService.getAllOrdersFiltered(page, size, sortBy, sortDir, status, clientId, sellerId));
    }
    
    @GetMapping("/admin/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        log.info("GET request for admin statistics");
        return ResponseEntity.ok(orderService.getAdminStats());
    }
    
    @GetMapping("/admin/date-range")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET request for orders between {} and {}", startDate, endDate);
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }
    
    @GetMapping("/admin/search")
    public ResponseEntity<List<OrderDTO>> searchOrders(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String productName) {
        log.info("GET request for advanced order search");
        return ResponseEntity.ok(orderService.searchOrders(orderId, clientId, sellerId, status, productName));
    }
    
    @PutMapping("/admin/bulk-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateStatus(@RequestBody BulkStatusUpdateRequest request) {
        log.info("PUT request for bulk status update");
        return ResponseEntity.ok(orderService.bulkUpdateStatus(request));
    }
}