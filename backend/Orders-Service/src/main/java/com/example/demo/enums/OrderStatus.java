package com.example.demo.enums;

public enum OrderStatus {
    PENDING,  //La commande vient d'être créée, en attente de confirmation du vendeur
    CONFIRMED,// Le vendeur a accepté/confirmé la commande (il a le stock)
    REJECTED, // Le vendeur a n'a pas accepté la commande (il n'a pas le stock suffisant)
    SHIPPED,  //La commande a été expédiée (colis en transit)
    DELIVERED,  //Le client a reçu la commande
    CANCELLED
}
