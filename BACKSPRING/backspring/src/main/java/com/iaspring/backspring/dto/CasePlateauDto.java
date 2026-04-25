package com.iaspring.backspring.dto;

import com.iaspring.backspring.entity.TypeEffet;

import lombok.Data;

@Data
public class CasePlateauDto {
    private Long id;
    private Long partieId;
    private int positionPlateau;
    private TypeEffet effet;
    private String categorie;
    private int points;
}