package com.iaspring.backspring.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.iaspring.backspring.entity.Utilisateur;
import com.iaspring.backspring.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.findByEmail("admin@demo.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setPseudo("Admin_Demo");
            admin.setEmail("admin@demo.com");
            admin.setMotDePasse("admin123");
            admin.setRole("ROLE_ADMIN");
            utilisateurRepository.save(admin);
        }
        if (utilisateurRepository.findByEmail("joueur@demo.com").isEmpty()) {
            Utilisateur joueur = new Utilisateur();
            joueur.setPseudo("Joueur_Demo");
            joueur.setEmail("joueur@demo.com");
            joueur.setMotDePasse("joueur123");
            joueur.setRole("ROLE_USER");
            utilisateurRepository.save(joueur);
        }
    }
}