package com.iaspring.backspring.dto;

import lombok.Data;

public class ApiDto {
    @Data
    public static class NouvellePartieSoloRequest {
        private Long utilisateurId;
        private int nbIa = 3;
    }

    @Data
    public static class PartieResponse {
        private Long id;
        private String statut;
        private int nombreJoueurs;
    }
}