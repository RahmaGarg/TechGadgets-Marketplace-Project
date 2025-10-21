package com.example.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de fallback GÉNÉRIQUE pour tous les microservices
 * Une seule méthode gère tous les cas d'indisponibilité
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback générique pour TOUS les microservices
     * Le path /fallback/** capture toutes les routes
     */
    @RequestMapping("/**")
    public ResponseEntity<Map<String, Object>> genericFallback(ServerWebExchange exchange) {
        // Extraire le nom du service depuis le path
        String path = exchange.getRequest().getURI().getPath();
        String serviceName = extractServiceName(path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", String.format(
            "Le service %s est temporairement indisponible. Veuillez réessayer dans quelques instants.",
            getServiceDisplayName(serviceName)
        ));
        response.put("service", serviceName);
        response.put("path", exchange.getRequest().getURI().getPath());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Extrait le nom du service depuis le path fallback
     * Exemple: /fallback/users -> users
     */
    private String extractServiceName(String path) {
        if (path.startsWith("/fallback/")) {
            String serviceName = path.substring("/fallback/".length());
            int slashIndex = serviceName.indexOf('/');
            return slashIndex > 0 ? serviceName.substring(0, slashIndex) : serviceName;
        }
        return "unknown";
    }
    
    /**
     * Retourne un nom d'affichage convivial pour chaque service
     */
    private String getServiceDisplayName(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "users" -> "d'authentification et de gestion des utilisateurs";
            case "products" -> "de gestion des produits et catégories";
            case "orders" -> "de gestion des commandes";
            case "reviews" -> "de gestion des avis clients";
            case "notifications" -> "de notifications";
            case "payments" -> "de paiement";
            default -> serviceName;
        };
    }
}

