package com.iaspring.backspring.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

public class CrudDto {

    // --- UTILISATEUR ---
    @Data
    public static class UtilisateurRequest {
        private String pseudo;
        private String email;
        private String motDePasse;
        private String role; // 👉 NOUVEAU : On ajoute le rôle à la création
    }

    @Data
    @Builder
    public static class UtilisateurResponse {
        private Long id;
        private String pseudo;
        private String email;
        private String role; // 👉 NOUVEAU : On renvoie le rôle au Frontend
        private LocalDateTime dateInscription;
        // On ne met délibérément pas le mot de passe ici !
    }

    // --- PARTIE (Version CRUD Admin) ---
    @Data
    public static class PartieRequest {
        private String statut;
        private int tourActuel;
        private Long vainqueurId;
    }

    @Data
    @Builder
    public static class PartieResponse {
        private Long id;
        private String statut;
        private String codeRejoindre;
        private int tourActuel;
        private Long createurId;
        private String pseudoCreateur;
    }
}