package com.iaspring.backspring.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iaspring.backspring.dto.ApiDto;
import com.iaspring.backspring.dto.PartieEtatDto;
import com.iaspring.backspring.entity.Partie;
import com.iaspring.backspring.service.PartieService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") 
public class PartieRestController {

    private final PartieService service;

    // --- ROUTES SPÉCIFIQUES AU JEU ---

    @PostMapping("/nouvelle-partie-solo")
    public ResponseEntity<ApiDto.PartieResponse> nouvellePartieSolo(@RequestBody ApiDto.NouvellePartieSoloRequest request) {
        Partie partie = service.creerPartieSolo(request.getUtilisateurId(), request.getNbIa());
        ApiDto.PartieResponse response = new ApiDto.PartieResponse();
        response.setId(partie.getId());
        response.setStatut(partie.getStatut());
        response.setNombreJoueurs(request.getNbIa() + 1);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{partieId}/etat")
    public ResponseEntity<PartieEtatDto> getEtatPartie(@PathVariable Long partieId) {
        return ResponseEntity.ok(service.recupererEtatComplet(partieId));
    }

    // --- ROUTES CRUD CLASSIQUES ---

    @GetMapping
    public ResponseEntity<List<Partie>> getAll() {
        return ResponseEntity.ok(service.recupererToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partie> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.recupererParId(id));
    }

    @PostMapping
    public ResponseEntity<Partie> create(@RequestBody Partie donnees) {
        return ResponseEntity.ok(service.creer(donnees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Partie> update(@PathVariable Long id, @RequestBody Partie donnees) {
        return ResponseEntity.ok(service.modifier(id, donnees));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}