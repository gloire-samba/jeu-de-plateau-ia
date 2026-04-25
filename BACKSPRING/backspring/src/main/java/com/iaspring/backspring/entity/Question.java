package com.iaspring.backspring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "QUESTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Column(name = "type_question", nullable = false, length = 20)
    private String typeQuestion;

    @Column(name = "type_reponse", length = 20)
    private String typeReponse;

    @Column(name = "texte_question", nullable = false, columnDefinition = "TEXT")
    private String texteQuestion;

    @Column(name = "bonne_reponse", nullable = false, columnDefinition = "TEXT")
    private String bonneReponse;

    @Column(name = "mauvaise_prop_1", columnDefinition = "TEXT")
    private String mauvaiseProp1;

    @Column(name = "mauvaise_prop_2", columnDefinition = "TEXT")
    private String mauvaiseProp2;

    @Column(name = "mauvaise_prop_3", columnDefinition = "TEXT")
    private String mauvaiseProp3;

    @Column(name = "synonymes_acceptes")
    private String synonymesAcceptes;

}
