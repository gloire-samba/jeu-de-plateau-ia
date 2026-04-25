package com.iaspring.backspring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PARTIE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_rejoindre", length = 20)
    private String codeRejoindre;

    @Column(nullable = false, length = 20)
    private String statut;

    // CORRECTION : On autorise Faker à insérer des dates passées
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "tour_actuel")
    private int tourActuel = 1;

    @ManyToOne
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;

    @ManyToOne
    @JoinColumn(name = "vainqueur_id")
    private Utilisateur vainqueur;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String historique = ""; // Initialisé vide

    @ManyToMany
    @JoinTable(
        name = "PARTIE_QUESTION_HISTORIQUE",
        joinColumns = @JoinColumn(name = "partie_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questionsPosees = new HashSet<>();

    // Méthode automatique pour la date du jour si non précisée
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}