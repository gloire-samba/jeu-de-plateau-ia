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

import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.service.PartieJoueurService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/partie-joueurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartieJoueurRestController {

    private final PartieJoueurService service;

    @GetMapping
    public ResponseEntity<List<PartieJoueur>> getAll() {
        return ResponseEntity.ok(service.recupererTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartieJoueur> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.recupererParId(id));
    }

    @GetMapping("/partie/{partieId}")
    public ResponseEntity<List<PartieJoueur>> getByPartie(@PathVariable Long partieId) {
        return ResponseEntity.ok(service.recupererParPartie(partieId));
    }

    @PostMapping
    public ResponseEntity<PartieJoueur> create(@RequestBody PartieJoueur donnees) {
        return ResponseEntity.ok(service.creer(donnees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartieJoueur> update(@PathVariable Long id, @RequestBody PartieJoueur donnees) {
        return ResponseEntity.ok(service.modifier(id, donnees));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}