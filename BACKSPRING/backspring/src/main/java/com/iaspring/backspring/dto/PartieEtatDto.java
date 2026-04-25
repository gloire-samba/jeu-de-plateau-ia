package com.iaspring.backspring.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PartieEtatDto {
    private Long partieId;
    private String codeRejoindre;
    private String statut;
    private int tourActuel;
    private List<JoueurDto> joueurs;

    @Data
    @Builder
    public static class JoueurDto {
        private Long id;
        private String nom; // Pseudo ou Nom IA
        private int position;
        private int ordreTour;
        private String effetActif;
        private int dureeEffet;
        private boolean estIa;
    }
}