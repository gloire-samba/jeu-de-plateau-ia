package com.iaspring.backspring.controller;

import com.iaspring.backspring.entity.Question;
import com.iaspring.backspring.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestionRestController {

    private final QuestionService service;

    @GetMapping
    public ResponseEntity<List<Question>> getAll() {
        return ResponseEntity.ok(service.recupererToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.recupererParId(id));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Question>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(service.recupererParCategorie(categorie));
    }

    @PostMapping
    public ResponseEntity<Question> create(@RequestBody Question donnees) {
        return ResponseEntity.ok(service.creer(donnees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> update(@PathVariable Long id, @RequestBody Question donnees) {
        return ResponseEntity.ok(service.modifier(id, donnees));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}