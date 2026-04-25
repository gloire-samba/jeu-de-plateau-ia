package com.iaspring.backspring.repository;

import com.iaspring.backspring.entity.Partie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartieRepository extends JpaRepository<Partie, Long> {

    // Pour retrouver une partie via son code unique
    Optional<Partie> findByCodeRejoindre(String codeRejoindre);
    
    // Pour le filtrage READ (GET) par statut (ex: lister les parties en cours)
    List<Partie> findByStatut(String statut);

    // Utile pour l'historique d'un utilisateur spécifique
    List<Partie> findByCreateurId(Long utilisateurId);
    List<Partie> findByVainqueurId(Long utilisateurId);


}