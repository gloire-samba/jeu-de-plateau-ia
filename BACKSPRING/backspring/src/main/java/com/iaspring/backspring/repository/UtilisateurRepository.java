package com.iaspring.backspring.repository;

import com.iaspring.backspring.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByPseudo(String pseudo);
    
    Optional<Utilisateur> findByEmail(String email);
    
    boolean existsByPseudo(String pseudo);
    
    boolean existsByEmail(String email);
}