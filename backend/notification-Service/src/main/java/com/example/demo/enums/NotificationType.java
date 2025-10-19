package com.example.demo.enums;


public enum NotificationType {
    // SELLER Notifications
    NEW_ORDER("Nouvelle commande reçue", "SELLER"),
    NEW_REVIEW("Nouveau avis sur votre produit", "SELLER"),
    LOW_STOCK("Alerte: Stock faible", "SELLER"),
    ORDER_CANCELLED_BY_CLIENT("Commande annulée par le client", "SELLER"),
    PAYMENT_RECEIVED("Paiement reçu", "SELLER"),
    REFUND_REQUEST("Demande de remboursement", "SELLER"),
    
    // CLIENT Notifications
    ORDER_CONFIRMED("Commande confirmée", "CLIENT"),
    ORDER_SHIPPED("Commande expédiée", "CLIENT"),
    ORDER_DELIVERED("Commande livrée", "CLIENT"),
    ORDER_CANCELLED("Commande annulée", "CLIENT"),
    PAYMENT_SUCCESS("Paiement confirmé", "CLIENT"),
    PAYMENT_FAILED("Échec du paiement", "CLIENT"),
    SELLER_REPLIED_TO_REVIEW("Réponse du vendeur à votre avis", "CLIENT"),
    PRODUCT_BACK_IN_STOCK("Produit de nouveau en stock", "CLIENT"),
    PROMOTION_ALERT("Promotion sur un produit favori", "CLIENT"),
    
    // ADMIN Notifications
    NEW_PRODUCT_PENDING("Nouveau produit à valider", "ADMIN"),
    NEW_SELLER_PENDING("Nouveau vendeur à valider", "ADMIN"),
    PRODUCT_REPORTED("Produit signalé", "ADMIN"),
    REVIEW_REPORTED("Avis signalé", "ADMIN"),
    FRAUD_ALERT("Alerte fraude détectée", "ADMIN");
    
    private final String message;
    private final String targetRole;
    
    NotificationType(String message, String targetRole) {
        this.message = message;
        this.targetRole = targetRole;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getTargetRole() {
        return targetRole;
    }
}