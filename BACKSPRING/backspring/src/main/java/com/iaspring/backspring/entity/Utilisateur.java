package com.iaspring.backspring.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UTILISATEUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String pseudo;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Builder.Default
    @Column(nullable = false)
    private String role = "ROLE_USER";

    // CORRECTION : On autorise Faker à insérer des dates passées
    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    // Méthode automatique : Si la date est vide à la création, on met la date du jour
    @PrePersist
    protected void onCreate() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime.now();
        }
    }
}