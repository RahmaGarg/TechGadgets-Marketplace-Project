package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.filters.AuthenticationFilter;

/**
 * Configuration complète de l'API Gateway
 * Gère le routage vers tous les microservices
 */
@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                
                // ========================================
                // 1. USER-SERVICE (Port 8081)
                // ========================================
                
                // Authentification (PUBLIC)
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://USERS-SERVICE"))
                
                // Profil utilisateur (PROTECTED)
                .route("user-profile", r -> r
                        .path("/api/profile/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://USERS-SERVICE"))
                
                // Gestion utilisateurs - ADMIN (PROTECTED)
                .route("user-management", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://USERS-SERVICE"))
                
                
                // ========================================
                // 2. PRODUCTS-SERVICE
                // ========================================
                
                // Produits (PUBLIC pour GET, PROTECTED pour POST/PUT/DELETE)
                .route("products", r -> r
                        .path("/products/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("productServiceCB")
                                        .setFallbackUri("forward:/fallback/products")))
                        .uri("lb://PRODUCTS-SERVICE"))
                
                // Catégories (PUBLIC)
                .route("categories", r -> r
                        .path("/categories/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("productServiceCB")
                                        .setFallbackUri("forward:/fallback/products")))
                        .uri("lb://PRODUCTS-SERVICE"))
                
                // Stock (PROTECTED)
                .route("stock", r -> r
                        .path("/stock/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("productServiceCB")
                                        .setFallbackUri("forward:/fallback/stock")))
                        .uri("lb://STOCK-SERVICE"))
                
                
                // ========================================
                // 3. ORDER-SERVICE
                // ========================================
                
                // Commandes (PROTECTED)
                .route("orders", r -> r
                        .path("/orders/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/orders")))
                        .uri("lb://ORDERS-SERVICE"))
                
                
                // ========================================
                // 4. REVIEW-SERVICE
                // ========================================
                
                // Avis clients (PUBLIC pour GET, PROTECTED pour POST/PUT/DELETE)
                .route("reviews", r -> r
                        .path("/reviews/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("reviewServiceCB")
                                        .setFallbackUri("forward:/fallback/reviews")))
                        .uri("lb://REVIEWS-SERVICE"))
                
                
                // ========================================
                // 5. NOTIFICATION-SERVICE
                // ========================================
                
                // Notifications (PROTECTED)
                .route("notifications", r -> r
                        .path("/notifications/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("notificationServiceCB")
                                        .setFallbackUri("forward:/fallback/notifications")))
                        .uri("lb://NOTIFICATION-SERVICE"))
                
                
                // ========================================
                // 6. PAYMENT-SERVICE
                // ========================================
                
                // Paiements (PROTECTED)
                .route("payments", r -> r
                        .path("/payment/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCB")
                                        .setFallbackUri("forward:/fallback/payments")))
                        .uri("lb://PAYMENTS-SERVICE"))
                
                // Webhooks paiement (PUBLIC - callbacks des fournisseurs)
                .route("payment-webhooks", r -> r
                        .path("/webhook/**")
                        .filters(f -> f
                                .filter(filter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCB")
                                        .setFallbackUri("forward:/fallback/payments")))
                        .uri("lb://PAYMENTS-SERVICE"))
                
                .build();
    }
}

