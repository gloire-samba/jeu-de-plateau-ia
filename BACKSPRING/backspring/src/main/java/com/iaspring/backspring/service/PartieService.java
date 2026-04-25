package com.iaspring.backspring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iaspring.backspring.dto.PartieEtatDto;
import com.iaspring.backspring.entity.Partie;
import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.TypeEffet;
import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.PartieJoueurRepository;
import com.iaspring.backspring.repository.PartieRepository;
import com.iaspring.backspring.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartieService {

    private final PartieRepository partieRepository;
    private final PartieJoueurRepository joueurRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ==========================================
    // LOGIQUE DE JEU (EXISTANTE)
    // ==========================================

    @Transactional
    public Partie creerPartieSolo(Long utilisateurId, int nbIa) {
        Utilisateur createur = utilisateurRepository.findById(utilisateurId).orElseThrow();

        Partie partie = new Partie();
        partie.setStatut("EN_COURS");
        partie.setCreateur(createur);
        partie = partieRepository.save(partie);

        PartieJoueur humain = new PartieJoueur();
        humain.setPartie(partie);
        humain.setUtilisateur(createur);
        humain.setEstIa(false);
        humain.setPositionPlateau(0);
        humain.setOrdreTour(1);
        humain.setEffetActif("AUCUN");
        humain.setDureeEffet(0);
        joueurRepository.save(humain);

        int nbIaFinal = (nbIa > 0 && nbIa <= 3) ? nbIa : 3;
        for (int i = 0; i < nbIaFinal; i++) {
            PartieJoueur ia = new PartieJoueur();
            ia.setPartie(partie);
            ia.setEstIa(true);
            ia.setNomIa("Bot Alpha " + (i + 1));
            ia.setPositionPlateau(0);
            ia.setOrdreTour(i + 2);
            ia.setEffetActif("AUCUN");
            ia.setDureeEffet(0);
            joueurRepository.save(ia);
        }
        return partie;
    }

    public PartieEtatDto recupererEtatComplet(Long partieId) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        List<PartieJoueur> joueurs = joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId);

        return PartieEtatDto.builder()
                .partieId(partie.getId())
                .codeRejoindre(partie.getCodeRejoindre())
                .statut(partie.getStatut())
                .tourActuel(partie.getTourActuel())
                .joueurs(joueurs.stream().map(j -> PartieEtatDto.JoueurDto.builder()
                        .id(j.getId())
                        .nom(j.isEstIa() ? j.getNomIa() : j.getUtilisateur().getPseudo())
                        .position(j.getPositionPlateau())
                        .ordreTour(j.getOrdreTour())
                        .effetActif(j.getEffetActif() != null ? j.getEffetActif() : "AUCUN") 
                        .dureeEffet(j.getDureeEffet())
                        .estIa(j.isEstIa())
                        .build()).toList())
                .build();
    }

    // ==========================================
    // MÉTHODES CRUD STANDARD
    // ==========================================

    // CREATE (POST - Manuel)
    public Partie creer(Partie partie) {
        return partieRepository.save(partie);
    }

    // READ (GET)
    public List<Partie> recupererToutes() {
        return partieRepository.findAll();
    }

    public Partie recupererParId(Long id) {
        return partieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partie introuvable"));
    }

    // UPDATE (PUT)
    public Partie modifier(Long id, Partie donnees) {
        Partie existante = recupererParId(id);
        existante.setStatut(donnees.getStatut());
        existante.setTourActuel(donnees.getTourActuel());
        if (donnees.getVainqueur() != null) {
            existante.setVainqueur(donnees.getVainqueur());
        }
        return partieRepository.save(existante);
    }

    // DELETE
    @Transactional
    public void supprimer(Long id) {
        if (!partieRepository.existsById(id)) {
            throw new RuntimeException("Partie introuvable");
        }
        // Supprime d'abord les joueurs liés pour éviter les erreurs de clés étrangères
        joueurRepository.deleteByPartieId(id);
        partieRepository.deleteById(id);
    }
}