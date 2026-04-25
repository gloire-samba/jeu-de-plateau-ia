package com.iaspring.backspring.dto;

import lombok.Data;

@Data
public class BotJouerResponse {
    private String action;
    private String reponse;
    private boolean is_wrong_intentionally;
    private String modele_utilise;
}
