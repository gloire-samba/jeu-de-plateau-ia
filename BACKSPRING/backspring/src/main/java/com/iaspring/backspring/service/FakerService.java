package com.iaspring.backspring.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.iaspring.backspring.entity.Partie;
import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.TypeEffet;
import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.PartieJoueurRepository;
import com.iaspring.backspring.repository.PartieRepository;
import com.iaspring.backspring.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;

@Service
@RequiredArgsConstructor
public class FakerService implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PartieRepository partieRepository;
    private final PartieJoueurRepository partieJoueurRepository;

    @Override
    public void run(String... args) {
        // Sécurité : On ne génère que si la base est vide
        if (utilisateurRepository.count() > 0) {
            System.out.println("✅ Base H2 déjà remplie, on ignore le Faker.");
            return;
        }
        
        System.out.println("⏳ Génération de 10 utilisateurs et 20 parties avec Faker...");
        Faker faker = new Faker();
        List<Utilisateur> utilisateurs = new ArrayList<>();

        // 1. Création de 10 Utilisateurs
        for (int i = 0; i < 10; i++) {
            Utilisateur u = Utilisateur.builder()
                .pseudo(faker.name().lastName() + i)
                .email(faker.internet().emailAddress())
                .motDePasse("password123") // Pas besoin de hasher pour l'Étape 1
                .dateInscription(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)))
                .build();
            utilisateurs.add(utilisateurRepository.save(u));
        }

        // 2. Création de 10 Parties Terminées
        for (int i = 0; i < 10; i++) {
            Utilisateur createur = utilisateurs.get(faker.number().numberBetween(0, 10));
            Partie p = Partie.builder()
                .statut("TERMINEE")
                .createur(createur)
                .vainqueur(createur) // On fait gagner le créateur pour faire simple
                .dateCreation(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 15)))
                .tourActuel(faker.number().numberBetween(15, 40))
                .build();
            partieRepository.save(p);
            creerJoueurs(p, createur, faker, true);
        }

        // 3. Création de 10 Parties En Cours
        for (int i = 0; i < 10; i++) {
            Utilisateur createur = utilisateurs.get(faker.number().numberBetween(0, 10));
            Partie p = Partie.builder()
                .statut("EN_COURS")
                .createur(createur)
                .dateCreation(LocalDateTime.now().minusHours(faker.number().numberBetween(1, 24)))
                .tourActuel(faker.number().numberBetween(1, 10))
                .build();
            partieRepository.save(p);
            creerJoueurs(p, createur, faker, false);
        }
        
        System.out.println("✅ Données générées avec succès dans H2 !");
    }

    private void creerJoueurs(Partie p, Utilisateur createur, Faker faker, boolean estTerminee) {
        // Le joueur humain
        partieJoueurRepository.save(PartieJoueur.builder()
            .partie(p)
            .utilisateur(createur)
            .estIa(false)
            .ordreTour(1)
            .positionPlateau(estTerminee ? 50 : faker.number().numberBetween(0, 30))
            .effetActif("AUCUN")
            .dureeEffet(0)
            .build());

        // 3 Bots
        for (int j = 0; j < 3; j++) {
            partieJoueurRepository.save(PartieJoueur.builder()
                .partie(p)
                .estIa(true)
                .nomIa("Bot " + faker.name().firstName())
                .ordreTour(j + 2)
                .positionPlateau(estTerminee ? faker.number().numberBetween(10, 49) : faker.number().numberBetween(0, 30))
                .effetActif("AUCUN")
                .dureeEffet(0)
                .build());
        }
    }
}
