package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.AdminStatsDTO;
import com.example.demo.dtos.BulkStatusUpdateRequest;
import com.example.demo.dtos.CreateOrderRequest;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.dtos.OrderItemDTO;
import com.example.demo.dtos.SellerStatsDTO;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    // ==================== CRUD BASIQUE ====================
    
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order for client: {} and seller: {}", request.getClientId(), request.getSellerId());
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        Order order = new Order();
        order.setClientId(request.getClientId());
        order.setSellerId(request.getSellerId());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        
        BigDecimal total = request.getItems()
            .stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);
        
        List<OrderItem> items = request.getItems().stream()
            .map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setOrder(savedOrder);
                item.setProductId(itemDTO.getProductId());
                item.setProductName(itemDTO.getProductName());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                return item;
            })
            .collect(Collectors.toList());
        
        orderItemRepository.saveAll(items);
        savedOrder.setItems(items);
        
        log.info("Order created successfully with id: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }
    
    public OrderDTO getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToDTO(order);
    }
    
    public List<OrderDTO> getClientOrders(Long clientId) {
        log.info("Fetching orders for client: {}", clientId);
        return orderRepository.findByClientId(clientId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getSellerOrders(Long sellerId) {
        log.info("Fetching orders for seller: {}", sellerId);
        return orderRepository.findBySellerId(sellerId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getClientOrdersByStatus(Long clientId, OrderStatus status) {
        log.info("Fetching orders for client: {} with status: {}", clientId, status);
        return orderRepository.findByClientIdAndStatus(clientId, status)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderDTO> getSellerOrdersByStatus(Long sellerId, OrderStatus status) {
        log.info("Fetching orders for seller: {} with status: {}", sellerId, status);
        return orderRepository.findBySellerIdAndStatus(sellerId, status)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Validation du changement de statut
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        orderRepository.save(order);
        
        log.info("Order status updated successfully");
        return mapToDTO(order);
    }
    
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel a delivered order");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        log.info("Order cancelled successfully");
    }
    
    public void deleteOrder(Long orderId) {
        log.info("Deleting order: {}", orderId);
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully");
    }
    
    // ==================== SELLER METHODS ====================
    
    public OrderDTO confirmOrder(Long orderId) {
        log.info("Confirming order: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be confirmed");
        }
        
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order {} confirmed successfully", orderId);
        return mapToDTO(order);
    }
    public OrderDTO rejectOrder(Long orderId) {
        log.info("Rejecting order: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be rejected");
        }
        
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
        log.info("Order {} rejected successfully", orderId);
        return mapToDTO(order);
    }
    
    public OrderDTO shipOrder(Long orderId) {
        log.info("Shipping order: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only CONFIRMED orders can be shipped");
        }
        
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        log.info("Order {} shipped successfully", orderId);
        return mapToDTO(order);
    }
    
    public SellerStatsDTO getSellerStats(Long sellerId) {
        log.info("Calculating stats for seller: {}", sellerId);
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        
        SellerStatsDTO stats = new SellerStatsDTO();
        stats.setTotalOrders((long) orders.size());
        stats.setPendingOrders(orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        stats.setConfirmedOrders(orders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count());
        stats.setShippedOrders(orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count());
        stats.setDeliveredOrders(orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        stats.setCancelledOrders(orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        
        BigDecimal totalRevenue = orders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyRevenue = orders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthlyRevenue(monthlyRevenue);
        
        return stats;
    }
    
    // ==================== CLIENT METHODS ====================
    
    public OrderDTO markAsDelivered(Long orderId) {
        log.info("Marking order {} as delivered", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Only SHIPPED orders can be marked as delivered");
        }
        
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        log.info("Order {} marked as delivered", orderId);
        return mapToDTO(order);
    }
    
    public Page<OrderDTO> getClientOrderHistory(Long clientId, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching order history for client: {}", clientId);
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Order> orderPage = orderRepository.findByClientId(clientId, pageable);
        return orderPage.map(this::mapToDTO);
    }
    
    // ==================== ADMIN METHODS ====================
    
    public Page<OrderDTO> getAllOrdersFiltered(int page, int size, String sortBy, String sortDir, 
                                               OrderStatus status, Long clientId, Long sellerId) {
        log.info("Fetching all orders with filters");
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Order> orderPage;
        
        if (status != null && clientId != null) {
            orderPage = orderRepository.findByClientIdAndStatus(clientId, status, pageable);
        } else if (status != null && sellerId != null) {
            orderPage = orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        } else if (status != null) {
            orderPage = orderRepository.findByStatus(status, pageable);
        } else if (clientId != null) {
            orderPage = orderRepository.findByClientId(clientId, pageable);
        } else if (sellerId != null) {
            orderPage = orderRepository.findBySellerId(sellerId, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }
        
        return orderPage.map(this::mapToDTO);
    }
    
    public AdminStatsDTO getAdminStats() {
        log.info("Calculating admin statistics");
        List<Order> allOrders = orderRepository.findAll();
        
        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalOrders((long) allOrders.size());
        stats.setTotalClients(allOrders.stream().map(Order::getClientId).distinct().count());
        stats.setTotalSellers(allOrders.stream().map(Order::getSellerId).distinct().count());
        stats.setPendingOrders(allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        stats.setCancelledOrders(allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        
        BigDecimal totalRevenue = allOrders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        BigDecimal todayRevenue = allOrders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .filter(o -> o.getCreatedAt().isAfter(startOfDay))
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRevenue(todayRevenue);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthRevenue = allOrders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthRevenue(monthRevenue);
        
        return stats;
    }
    
    public List<OrderDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        return orderRepository.findByCreatedAtBetween(startDate, endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderDTO> searchOrders(Long orderId, Long clientId, Long sellerId, 
                                       OrderStatus status, String productName) {
        log.info("Searching orders with advanced filters");
        List<Order> orders = orderRepository.findAll();
        
        return orders.stream()
            .filter(o -> orderId == null || o.getId().equals(orderId))
            .filter(o -> clientId == null || o.getClientId().equals(clientId))
            .filter(o -> sellerId == null || o.getSellerId().equals(sellerId))
            .filter(o -> status == null || o.getStatus() == status)
            .filter(o -> productName == null || o.getItems().stream()
                .anyMatch(item -> item.getProductName().toLowerCase().contains(productName.toLowerCase())))
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> bulkUpdateStatus(BulkStatusUpdateRequest request) {
        log.info("Bulk updating status for {} orders", request.getOrderIds().size());
        int success = 0;
        int failure = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long orderId : request.getOrderIds()) {
            try {
                updateOrderStatus(orderId, request.getNewStatus());
                success++;
            } catch (Exception e) {
                failure++;
                errors.add("Order " + orderId + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", success);
        result.put("failureCount", failure);
        result.put("errors", errors);
        result.put("total", request.getOrderIds().size());
        
        return result;
    }
    
    // ==================== VALIDATION PRIVÉE ====================
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValid = false;
        String errorMessage = "";
        
        switch (currentStatus) {
        case PENDING:
            isValid = newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REJECTED;
            errorMessage = "PENDING orders can only be CONFIRMED, REJECTED or CANCELLED";
            break;              
            case CONFIRMED:
                isValid = newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
                errorMessage = "CONFIRMED orders can only be SHIPPED or CANCELLED";
                break;
            case SHIPPED:
                isValid = newStatus == OrderStatus.DELIVERED;
                errorMessage = "SHIPPED orders can only be marked as DELIVERED";
                break;
            case DELIVERED:
                isValid = false;
                errorMessage = "Cannot change status of DELIVERED orders";
                break;
            case CANCELLED:
                isValid = false;
                errorMessage = "Cannot change status of CANCELLED orders";
                break;
        }
        
        if (!isValid) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    // ==================== MAPPING ====================
    
    private OrderDTO mapToDTO(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getClientId(),
            order.getSellerId(),
            order.getStatus(),
            order.getTotalPrice(),
            order.getShippingAddress(),
            order.getCreatedAt(),
            order.getItems() != null
                ? order.getItems().stream()
                    .map((OrderItem item) -> new OrderItemDTO(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                    ))
                    .collect(Collectors.toList())
                : List.of()
        );
    }
}