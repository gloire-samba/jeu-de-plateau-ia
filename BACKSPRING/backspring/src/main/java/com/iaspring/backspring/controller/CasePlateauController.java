package com.iaspring.backspring.controller;

import com.iaspring.backspring.dto.CasePlateauDto;
import com.iaspring.backspring.service.CasePlateauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plateau")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CasePlateauController {

    private final CasePlateauService casePlateauService;

    @PostMapping
    public ResponseEntity<CasePlateauDto> creerCase(@RequestBody CasePlateauDto dto) {
        return ResponseEntity.ok(casePlateauService.creerCase(dto));
    }

    @GetMapping("/partie/{partieId}")
    public ResponseEntity<List<CasePlateauDto>> getPlateau(@PathVariable Long partieId) {
        // Angular appellera cette route pour dessiner le plateau à l'écran !
        return ResponseEntity.ok(casePlateauService.getPlateauByPartie(partieId));
    }

    // 👉 NOUVEAU : Récupérer toutes les cases
    @GetMapping
    public ResponseEntity<List<CasePlateauDto>> getToutesLesCases() {
        return ResponseEntity.ok(casePlateauService.getToutesLesCases());
    }

    // 👉 NOUVEAU : Supprimer une case
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCase(@PathVariable Long id) {
        casePlateauService.supprimerCase(id);
        return ResponseEntity.noContent().build();
    }
}