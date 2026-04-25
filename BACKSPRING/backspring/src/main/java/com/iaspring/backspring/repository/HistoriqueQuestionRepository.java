package com.iaspring.backspring.repository;

import com.iaspring.backspring.entity.HistoriqueQuestion;
import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface HistoriqueQuestionRepository extends JpaRepository<HistoriqueQuestion, Long> {
    
    List<HistoriqueQuestion> findByUtilisateur(Utilisateur utilisateur);
    
    List<HistoriqueQuestion> findByPartieJoueur(PartieJoueur partieJoueur);
    
    @Transactional
    void deleteByUtilisateurAndCategorie(Utilisateur utilisateur, String categorie);
    
    @Transactional
    void deleteByPartieJoueurAndCategorie(PartieJoueur partieJoueur, String categorie);
}