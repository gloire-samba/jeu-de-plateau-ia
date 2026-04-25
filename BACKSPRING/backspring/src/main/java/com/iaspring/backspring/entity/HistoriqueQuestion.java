package com.iaspring.backspring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HISTORIQUE_QUESTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisé pour garder la mémoire du joueur humain à travers toutes ses parties
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur; 

    // Utilisé pour garder la mémoire des bots uniquement pendant la partie en cours
    @ManyToOne
    @JoinColumn(name = "partie_joueur_id")
    private PartieJoueur partieJoueur; 

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 50)
    private String categorie;
}