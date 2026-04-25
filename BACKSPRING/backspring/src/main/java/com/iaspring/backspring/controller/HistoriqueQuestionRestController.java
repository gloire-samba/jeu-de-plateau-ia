package com.iaspring.backspring.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iaspring.backspring.dto.HistoriqueQuestionDTO;
import com.iaspring.backspring.entity.HistoriqueQuestion;
import com.iaspring.backspring.service.HistoriqueQuestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/historique")
@RequiredArgsConstructor
public class HistoriqueQuestionRestController {

    private final HistoriqueQuestionService historiqueService;

    @GetMapping
    public ResponseEntity<List<HistoriqueQuestionDTO>> recupererTout() {
        // On récupère les entités et on les transforme en DTOs
        List<HistoriqueQuestionDTO> dtos = historiqueService.recupererTout().stream()
                .map(this::convertirEnDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        historiqueService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // --- MÉTHODE UTILITAIRE DE CONVERSION ---
    private HistoriqueQuestionDTO convertirEnDto(HistoriqueQuestion entite) {
        return HistoriqueQuestionDTO.builder()
                .id(entite.getId())
                .categorie(entite.getCategorie())
                .questionId(entite.getQuestion() != null ? entite.getQuestion().getId() : null)
                .utilisateurId(entite.getUtilisateur() != null ? entite.getUtilisateur().getId() : null)
                .partieJoueurId(entite.getPartieJoueur() != null ? entite.getPartieJoueur().getId() : null)
                .build();
    }
}