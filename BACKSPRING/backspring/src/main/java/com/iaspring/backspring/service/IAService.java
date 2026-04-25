package com.iaspring.backspring.service;

import com.iaspring.backspring.dto.BotJouerRequest;
import com.iaspring.backspring.dto.BotJouerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IAService {

    private final RestTemplate restTemplate;

    // On récupère l'URL de ton IA configurée dans application.properties
    // Ex: python.api.url=http://localhost:8000
    @Value("${python.api.url}")
    private String pythonApiUrl;

    public IAService() {
        this.restTemplate = new RestTemplate();
    }

    public BotJouerResponse demanderReponseIA(BotJouerRequest request) {
        String url = pythonApiUrl + "/api/bot/jouer";
        try {
            // Fait la requête HTTP POST vers ton script Python (LiteLLM)
            return restTemplate.postForObject(url, request, BotJouerResponse.class);
        } catch (Exception e) {
            // Mécanique de Fallback de secours si le Python crash complètement
            BotJouerResponse fallback = new BotJouerResponse();
            fallback.setAction("PASSER");
            return fallback;
        }
    }
}