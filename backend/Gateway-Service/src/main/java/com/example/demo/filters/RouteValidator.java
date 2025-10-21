package com.example.demo.filters;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    /**
     * Liste complète des endpoints PUBLICS (accessibles sans JWT)
     */
    public static final List<String> openApiEndpoints = List.of(
            // ============ USER SERVICE ============
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password/validate",
            "/api/auth/reset-password",
            
            // ============ PRODUCT SERVICE ============
            // Consultation publique des produits et catégories
            "/products",              // GET tous les produits (liste publique)
            "/categories",            // GET toutes les catégories
            
            // ============ REVIEW SERVICE ============
            // Consultation publique des avis
            "/reviews/product",       // GET avis d'un produit
            "/reviews/images",        // GET images des avis
            
            // ============ PAYMENT SERVICE ============
            // Webhooks et callbacks (appelés par services externes)
            "/webhook/konnect",
            "/webhook/paypal",
            "/payment/paypal/success",
            "/payment/paypal/cancel",
            "/payment/konnect/success",
            "/payment/konnect/fail",
            
            // ============ MONITORING ============
            "/eureka",
            "/actuator/health"
    );

    /**
     * Prédicat pour vérifier si une route nécessite une authentification
     * Retourne TRUE si la route est SÉCURISÉE (JWT requis)
     * Retourne FALSE si la route est PUBLIQUE (pas de JWT)
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();
                
                // Vérifie si le chemin commence par un endpoint public
                boolean isPublic = openApiEndpoints.stream()
                        .anyMatch(uri -> path.contains(uri) || path.startsWith(uri));
                
                // Retourne l'inverse : true si sécurisé (pas public)
                return !isPublic;
            };
}

