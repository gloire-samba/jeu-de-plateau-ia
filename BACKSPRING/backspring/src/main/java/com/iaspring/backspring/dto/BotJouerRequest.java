package com.iaspring.backspring.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BotJouerRequest {
    private String type_question; // "QCM" ou "TEXTE"
    private String question;
    private List<String> propositions;
}