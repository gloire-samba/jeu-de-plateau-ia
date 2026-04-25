package com.iaspring.backspring.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.TypeEffet;
import com.iaspring.backspring.repository.PartieJoueurRepository;
import com.iaspring.backspring.repository.PartieRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoteurJeuService {

    private final PartieRepository partieRepository;
    private final PartieJoueurRepository joueurRepository;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CasePlateau {
        private TypeEffet effet;
        private String categorie;
        private int points;
    }

    public record ResultatTour(boolean victoire, TypeEffet effetEnAttente, boolean aDroitDeuxiemeChance, String messageEffet) {}

    @Transactional
    public void appliquerEffetInteractif(Long lanceurId, Long cible1Id, Long cible2Id) {
        PartieJoueur lanceur = joueurRepository.findById(lanceurId).orElseThrow();
        String effet = lanceur.getEffetActif();

        if ("CIBLE_ECHANGE".equals(effet) && cible1Id != null && cible2Id != null) {
            PartieJoueur c1 = joueurRepository.findById(cible1Id).orElseThrow();
            PartieJoueur c2 = joueurRepository.findById(cible2Id).orElseThrow();
            int pos1 = c1.getPositionPlateau();
            c1.setPositionPlateau(c2.getPositionPlateau());
            c2.setPositionPlateau(pos1);
            joueurRepository.save(c1);
            joueurRepository.save(c2);
        } else if (effet != null && effet.startsWith("CIBLE_") && cible1Id != null) {
            PartieJoueur cible = joueurRepository.findById(cible1Id).orElseThrow();
            switch (effet) {
                case "CIBLE_RECUL":
                    reculerJoueur(cible, 3);
                    break;
                case "CIBLE_PASSE_TOUR":
                    cible.setEffetActif("MALUS_PASSE_TOUR");
                    cible.setDureeEffet(1);
                    break;
                case "CIBLE_PRESSION":
                    cible.setEffetActif("MALUS_PRESSION");
                    cible.setDureeEffet(1);
                    lanceur.setEffetActif("BONUS_PRESSION");
                    lanceur.setDureeEffet(1);
                    break;
            }
            joueurRepository.save(cible);
        }

        if (!"BONUS_PRESSION".equals(lanceur.getEffetActif())) {
            lanceur.setEffetActif("AUCUN");
        }
        joueurRepository.save(lanceur);
    }

    @Transactional
    public ResultatTour traiterReponse(Long partieId, Long joueurId, boolean estBonneReponse, int valeurDe, Map<Integer, CasePlateau> plateau, int taillePlateau) {
        PartieJoueur joueurActuel = joueurRepository.findById(joueurId).orElseThrow();
        List<PartieJoueur> tousLesJoueurs = joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId);
        
        TypeEffet effetDepart = getEffetForPosition(joueurActuel.getPositionPlateau(), plateau);
        
        if ("PARI_MULTIPLICATEUR".equals(joueurActuel.getEffetActif())) {
            effetDepart = TypeEffet.PARI_MULTIPLICATEUR;
            joueurActuel.setEffetActif("AUCUN");
        }

        TypeEffet effetEnAttente = null;
        boolean aDroitDeuxiemeChance = false;
        String messageEffet = "";

        CasePlateau caseActuelle = (plateau != null) ? plateau.get(joueurActuel.getPositionPlateau()) : null;
        int ptsCase = (caseActuelle != null && caseActuelle.getPoints() > 0) ? caseActuelle.getPoints() : 1;

        if (!estBonneReponse) {
            if (effetDepart == TypeEffet.DEUXIEME_CHANCE) {
                aDroitDeuxiemeChance = true;
                messageEffet = "✨ Sauvé par la 2ème chance !";
            } else if (effetDepart == TypeEffet.RISQUE_EXPLOSIF) {
                reculerJoueur(joueurActuel, 3);
                messageEffet = "💣 Boum ! Mauvaise réponse, recule de 3 cases.";
            } else if (effetDepart == TypeEffet.PARI_MULTIPLICATEUR) {
                int recul = valeurDe * ptsCase;
                reculerJoueur(joueurActuel, recul);
                messageEffet = "📉 Pari perdu ! Vous reculez de " + recul + " cases.";
            }
        } 
        else {
            int deplacementFinal = valeurDe;
            
            if (effetDepart == TypeEffet.PARI_MULTIPLICATEUR) {
                deplacementFinal = valeurDe * ptsCase;
                messageEffet = "📈 Pari gagné ! Vous avancez de " + deplacementFinal + " cases.";
            }
            
            boolean hasSprintBonus = "SPRINT".equals(joueurActuel.getEffetActif());
            if (effetDepart == TypeEffet.SPRINT || hasSprintBonus) {
                deplacementFinal *= 2;
                messageEffet += (messageEffet.isEmpty() ? "" : " ") + "⚡ SPRINT ! Déplacement doublé.";
                if (hasSprintBonus) joueurActuel.setEffetActif("AUCUN"); 
            }
            
            avancerJoueur(joueurActuel, deplacementFinal, taillePlateau);

            if (joueurActuel.getPositionPlateau() >= taillePlateau) {
                return new ResultatTour(true, null, false, "🏆 Victoire !");
            }

            if (effetDepart == TypeEffet.RISQUE_EXPLOSIF) {
                int reculZone = valeurDe * 2; 
                messageEffet = "💣 EXPLOSION ! Les autres joueurs reculent de " + reculZone + " cases.";
                for (PartieJoueur j : tousLesJoueurs) {
                    if (!j.getId().equals(joueurActuel.getId())) {
                        if (!"BOUCLIER".equals(j.getEffetActif())) {
                            reculerJoueur(j, reculZone);
                            joueurRepository.save(j);
                        }
                    }
                }
            }

            TypeEffet effetArrivee = getEffetForPosition(joueurActuel.getPositionPlateau(), plateau);
            
            switch (effetArrivee) {
                case BOUCLIER:
                    joueurActuel.setEffetActif("BOUCLIER");
                    joueurActuel.setDureeEffet(2);
                    messageEffet += (messageEffet.isEmpty() ? "" : " | ") + "🛡️ Bouclier activé !";
                    break;
                case INDICE:
                    joueurActuel.setEffetActif("INDICE");
                    joueurActuel.setDureeEffet(2);
                    messageEffet += (messageEffet.isEmpty() ? "" : " | ") + "💡 Indice gagné !";
                    break;
                case SUPER_BONUS:
                    effetEnAttente = effetArrivee;
                    joueurActuel.setEffetActif(effetArrivee.name());
                    messageEffet += (messageEffet.isEmpty() ? "" : " | ") + "🌟 Super Bonus atteint !";
                    break;
                case CIBLE_ECHANGE:
                case CIBLE_RECUL:
                case CIBLE_PASSE_TOUR:
                case CIBLE_PRESSION:
                    effetEnAttente = effetArrivee;
                    joueurActuel.setEffetActif(effetArrivee.name());
                    messageEffet += (messageEffet.isEmpty() ? "" : " | ") + "🎯 Préparez votre attaque !";
                    break;
                default: break;
            }
        }

        if (!aDroitDeuxiemeChance && effetEnAttente == null) reduireDureeEffet(joueurActuel);
        joueurRepository.save(joueurActuel);
        
        return new ResultatTour(false, effetEnAttente, aDroitDeuxiemeChance, messageEffet);
    }

    private void avancerJoueur(PartieJoueur joueur, int cases, int taillePlateau) {
        joueur.setPositionPlateau(Math.min(joueur.getPositionPlateau() + cases, taillePlateau));
    }
    private void reculerJoueur(PartieJoueur joueur, int cases) {
        joueur.setPositionPlateau(Math.max(joueur.getPositionPlateau() - cases, 0));
    }
    private void reduireDureeEffet(PartieJoueur joueur) {
        if (joueur.getDureeEffet() > 0) {
            joueur.setDureeEffet(joueur.getDureeEffet() - 1);
            if (joueur.getDureeEffet() == 0 && !joueur.getEffetActif().equals("BONUS_PRESSION")) {
                joueur.setEffetActif("AUCUN");
            }
        }
    }

    public TypeEffet getEffetForPosition(int pos, Map<Integer, CasePlateau> plateau) {
        if (plateau != null && plateau.containsKey(pos)) {
            return plateau.get(pos).getEffet();
        }
        return determinerEffetCaseFixe(pos);
    }

    public TypeEffet determinerEffetCaseFixe(int position) {
        return switch (position) {
            case 5, 15, 35 -> TypeEffet.BOUCLIER;
            case 10, 40 -> TypeEffet.SUPER_BONUS;
            case 25 -> TypeEffet.INDICE;
            case 8, 22, 45 -> TypeEffet.DEUXIEME_CHANCE;
            case 12, 32 -> TypeEffet.SPRINT;
            case 7, 27, 47 -> TypeEffet.CIBLE_RECUL;
            case 18, 38 -> TypeEffet.CIBLE_PASSE_TOUR;
            case 14, 34 -> TypeEffet.CIBLE_ECHANGE;
            case 20, 42 -> TypeEffet.CIBLE_PRESSION;
            case 13, 26, 39 -> TypeEffet.RISQUE_EXPLOSIF;
            case 17, 37 -> TypeEffet.PARI_MULTIPLICATEUR;
            default -> TypeEffet.AUCUN;
        };
    }
}