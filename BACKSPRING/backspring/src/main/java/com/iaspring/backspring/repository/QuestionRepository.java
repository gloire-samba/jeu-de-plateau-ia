package com.iaspring.backspring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iaspring.backspring.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Tirage aléatoire simple (utilisé au début de partie ou si l'historique est vide).
     */
    @Query(value = "SELECT * FROM QUESTION WHERE categorie = :categorie ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomByCategorie(@Param("categorie") String categorie);
    
    /**
     * Tirage aléatoire excluant les questions déjà posées.
     * ATTENTION : En SQL natif, 'NOT IN' avec une liste vide provoque une erreur. 
     * Ce repository doit être appelé par un service qui vérifie si la liste est vide.
     */
    @Query(value = "SELECT * FROM QUESTION q WHERE q.categorie = :categorie AND q.id NOT IN :questionsPoseesIds ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomByCategorieExcluding(
            @Param("categorie") String categorie, 
            @Param("questionsPoseesIds") List<Long> questionsPoseesIds
    );

    List<Question> findByCategorie(String categorie);

    List<Question> findByCategorieAndTypeReponse(String categorie, String typeReponse);
}