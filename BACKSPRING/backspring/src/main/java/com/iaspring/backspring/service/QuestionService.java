package com.iaspring.backspring.service;

import com.iaspring.backspring.entity.Question;
import com.iaspring.backspring.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    // CREATE (POST)
    public Question creer(Question question) {
        return questionRepository.save(question);
    }

    // READ (GET)
    public List<Question> recupererToutes() {
        return questionRepository.findAll();
    }

    public Question recupererParId(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question introuvable"));
    }

    public List<Question> recupererParCategorie(String categorie) {
        return questionRepository.findByCategorie(categorie);
    }

    // UPDATE (PUT)
    public Question modifier(Long id, Question donnees) {
        Question existante = recupererParId(id);
        existante.setCategorie(donnees.getCategorie());
        existante.setTypeQuestion(donnees.getTypeQuestion());
        existante.setTexteQuestion(donnees.getTexteQuestion());
        existante.setBonneReponse(donnees.getBonneReponse());
        existante.setMauvaiseProp1(donnees.getMauvaiseProp1());
        existante.setMauvaiseProp2(donnees.getMauvaiseProp2());
        existante.setMauvaiseProp3(donnees.getMauvaiseProp3());
        existante.setSynonymesAcceptes(donnees.getSynonymesAcceptes());
        return questionRepository.save(existante);
    }

    // DELETE
    public void supprimer(Long id) {
        questionRepository.deleteById(id);
    }
}
