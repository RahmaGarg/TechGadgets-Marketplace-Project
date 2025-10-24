package com.example.demo.enums;


public enum NotificationType {
    // ============ ACCOUNT & SYSTEM ============
	SELLER_ACCOUNT_CREATED("Compte vendeur créé", "SELLER"),  
	CLIENT_ACCOUNT_CREATED("Compte client créé", "CLIENT"),
    // ============ ORDER - CLIENT ============
    ORDER_CREATED("Commande créée", "CLIENT"),
    ORDER_CONFIRMED("Commande confirmée", "CLIENT"),
    ORDER_SHIPPED("Commande expédiée", "CLIENT"),
    ORDER_DELIVERED("Commande livrée", "CLIENT"),
    ORDER_CANCELLED("Commande annulée", "CLIENT"),
    
    // ============ ORDER - SELLER ============
    NEW_ORDER("Nouvelle commande reçue", "SELLER"),
    ORDER_CONFIRMED_SELLER("Commande confirmée - Préparez l'expédition", "SELLER"),
    ORDER_DELIVERED_SELLER("Commande livrée avec succès", "SELLER"),
    ORDER_CANCELLED_BY_CLIENT("Commande annulée par le client", "SELLER"),
    
    // ============ PAYMENT - CLIENT ============
    PAYMENT_SUCCESS("Paiement confirmé", "CLIENT"),
    PAYMENT_FAILED("Échec du paiement", "CLIENT"),
    
    // ============ PAYMENT - SELLER ============
    PAYMENT_RECEIVED("Paiement reçu", "SELLER"),
    
    // ============ STOCK - SELLER ============
    LOW_STOCK("Alerte: Stock faible", "SELLER"),
    STOCK_DEPLETED("Rupture de stock", "SELLER"),
    STOCK_RESERVATION_FAILED("Échec réservation stock", "CLIENT"),
    
    // ============ PRODUCT - SELLER ============
    PRODUCT_APPROVED("Produit approuvé", "SELLER"),
    PRODUCT_REJECTED("Produit rejeté", "SELLER"),
    
    // ============ PRODUCT - ADMIN ============
    NEW_PRODUCT_PENDING("Nouveau produit à valider", "ADMIN"),
    
    // ============ REVIEW ============
    NEW_REVIEW("Nouveau avis sur votre produit", "SELLER"),
    
    // ============ ADMIN ALERTS ============
    PAYMENT_FAILED_ADMIN("Alerte: Paiement échoué", "ADMIN"),
    FRAUD_ALERT("Alerte fraude détectée", "ADMIN");

    private final String description;
    private final String targetRole;

    NotificationType(String description, String targetRole) {
        this.description = description;
        this.targetRole = targetRole;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetRole() {
        return targetRole;
    }
}