package com.iaspring.backspring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iaspring.backspring.entity.PartieJoueur;

@Repository
public interface PartieJoueurRepository extends JpaRepository<PartieJoueur, Long> {

    // Récupère tous les joueurs d'une partie, triés par leur ordre de tour (1, 2, 3, 4)
    List<PartieJoueur> findByPartieIdOrderByOrdreTourAsc(Long partieId);
    
    // Trouve un joueur spécifique dans une partie (pour appliquer un effet par exemple)
    Optional<PartieJoueur> findByPartieIdAndOrdreTour(Long partieId, int ordreTour);

    void deleteByPartieId(Long id);
}