package com.iaspring.backspring.service;

import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.repository.PartieJoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartieJoueurService {

    private final PartieJoueurRepository joueurRepository;

    // CREATE (POST)
    public PartieJoueur creer(PartieJoueur joueur) {
        return joueurRepository.save(joueur);
    }

    // READ (GET)
    public List<PartieJoueur> recupererTous() {
        return joueurRepository.findAll();
    }

    public PartieJoueur recupererParId(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Joueur de partie introuvable"));
    }

    public List<PartieJoueur> recupererParPartie(Long partieId) {
        return joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId);
    }

    // UPDATE (PUT)
    public PartieJoueur modifier(Long id, PartieJoueur donnees) {
        PartieJoueur existant = recupererParId(id);
        existant.setPositionPlateau(donnees.getPositionPlateau());
        existant.setOrdreTour(donnees.getOrdreTour());
        existant.setEffetActif(donnees.getEffetActif());
        existant.setDureeEffet(donnees.getDureeEffet());
        return joueurRepository.save(existant);
    }

    // DELETE
    public void supprimer(Long id) {
        joueurRepository.deleteById(id);
    }
}