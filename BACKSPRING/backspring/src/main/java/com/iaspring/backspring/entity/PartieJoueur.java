package com.iaspring.backspring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PARTIE_JOUEUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartieJoueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "est_ia", nullable = false)
    private boolean estIa;

    @Column(name = "nom_ia", length = 50)
    private String nomIa;

    @Column(name = "position_plateau", nullable = false)
    private int positionPlateau;

    @Column(name = "ordre_tour", nullable = false)
    private int ordreTour;

    // CORRECTION : On lie le champ à ton Enum TypeEffet
    @Column(name = "effet_actif", length = 50)
    private String effetActif;

    @Column(name = "duree_effet")
    private int dureeEffet;
}