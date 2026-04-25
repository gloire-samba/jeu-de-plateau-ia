package com.iaspring.backspring.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoriqueQuestionDTO {
    private Long id;
    private String categorie;
    
    // On ne renvoie que les IDs pour éviter les boucles infinies de JSON
    private Long questionId;
    private Long utilisateurId;
    private Long partieJoueurId;
}