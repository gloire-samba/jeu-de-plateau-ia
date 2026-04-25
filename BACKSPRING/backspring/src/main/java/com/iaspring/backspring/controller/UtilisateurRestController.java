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

import com.iaspring.backspring.dto.CrudDto;
import com.iaspring.backspring.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UtilisateurRestController {

    private final UtilisateurService service;

    @GetMapping
    public ResponseEntity<List<CrudDto.UtilisateurResponse>> getAll() {
        return ResponseEntity.ok(service.recupererTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrudDto.UtilisateurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.recupererParId(id));
    }

    @PostMapping
    public ResponseEntity<CrudDto.UtilisateurResponse> create(@RequestBody CrudDto.UtilisateurRequest donnees) {
        return ResponseEntity.ok(service.creer(donnees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrudDto.UtilisateurResponse> update(@PathVariable Long id, @RequestBody CrudDto.UtilisateurRequest donnees) {
        return ResponseEntity.ok(service.modifier(id, donnees));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}