package com.iaspring.backspring.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iaspring.backspring.entity.Partie;
import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.Question;
import com.iaspring.backspring.entity.TypeEffet;
import com.iaspring.backspring.repository.PartieJoueurRepository;
import com.iaspring.backspring.repository.PartieRepository;
import com.iaspring.backspring.repository.QuestionRepository;
import com.iaspring.backspring.service.CasePlateauService;
import com.iaspring.backspring.service.HistoriqueQuestionService;
import com.iaspring.backspring.service.IaJugeService;
import com.iaspring.backspring.service.MoteurJeuService;
import com.iaspring.backspring.service.PartieService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jeu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JeuRestController {

    private final MoteurJeuService moteurJeuService;
    private final PartieJoueurRepository joueurRepository;
    private final IaJugeService iaJugeService;
    private final PartieService partieService;
    private final CasePlateauService casePlateauService;
    private final HistoriqueQuestionService historiqueQuestionService;
    private final QuestionRepository questionRepository;
    private final PartieRepository partieRepository;

    private final List<String> TOUTES_CATEGORIES = Arrays.asList(
        "HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"
    );

    @Data
    public static class DemarrerJeuDto {
        private Long utilisateurId;
        private int nbBots;
        private int typePlateau; 
        private int taillePlateau; 
        private Map<Integer, MoteurJeuService.CasePlateau> plateauCustom;
    }

    @Data
    public static class QuestionDto {
        private Long questionId;
        private String texteQuestion;
        private String typeQuestion;
        private List<String> propositions;
        private String indiceTexte; 
    }

    @Data
    public static class ReponseDto {
        private Long partieId;
        private Long joueurId;
        private Long questionId; 
        private String reponseJoueur;
    }

    @Data
    public static class ReponseTourDto {
        private MoteurJeuService.ResultatTour resultatTour;
        private boolean etaitBonneReponse;
        private int valeurDe;
        private int nouvellePosition;
        private Integer positionAvantBots; 
        private List<String> logsIA; 
        private boolean partieTerminee;
        private String nomVainqueur;
        private String bonneReponse; 
    }

    @Data
    public static class JoueurEtatDto {
        private Long id;
        private String nom;
        private int position;
        private boolean estIa;
        private String statut;
        private int dureeEffet;
    }

    @Data
    public static class EtatPartieDto {
        private Long partieId;
        private List<JoueurEtatDto> joueurs;
    }

    @Data
    public static class AppliquerEffetDto {
        private Long lanceurId;
        private Long cible1Id; 
        private Long cible2Id; 
    }

    @Data
    public static class PreparerSuperBonusDto {
        private Long partieId;
        private Long joueurId;
        private String categorie;
    }

    @Data
    public static class RepondreSuperBonusDto {
        private Long partieId;
        private Long joueurId;
        private Long questionId;
        private String reponseJoueur;
        private int pointsChoisis;
        private String effetChoisi;
    }

    private String genererIndice(String reponse) {
        if (reponse == null || reponse.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reponse.length(); i++) {
            char c = reponse.charAt(i);
            if (c == ' ') {
                sb.append("   "); 
            } else if (i < 2 || i == reponse.length() - 1) {
                sb.append(c).append(" ");
            } else {
                sb.append("_ ");
            }
        }
        return sb.toString().trim();
    }

    private void faireJouerBots(Long partieId, ReponseTourDto response, int taillePlateau, Map<Integer, MoteurJeuService.CasePlateau> plateauActuel) {
        List<String> logsIA = new ArrayList<>();
        List<PartieJoueur> tousLesJoueurs = joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId);
        
        logsIA.add("--------------------------------------------------");
        logsIA.add("📊 RÉSUMÉ DU PLATEAU :");
        for (PartieJoueur pj : tousLesJoueurs) {
            String nom = pj.isEstIa() ? pj.getNomIa() : pj.getUtilisateur().getPseudo();
            String eff = (pj.getEffetActif() != null && !pj.getEffetActif().equals("AUCUN")) ? " (" + pj.getEffetActif() + ")" : "";
            logsIA.add("   - " + nom + " : Case " + pj.getPositionPlateau() + eff);
        }

        Random rand = new Random();
        
        for(PartieJoueur ia : tousLesJoueurs) {
            if(ia.isEstIa()) {
                
                if ("MALUS_PASSE_TOUR".equals(ia.getEffetActif())) {
                    logsIA.add("--------------------------------------------------");
                    logsIA.add("🤖 TOUR DE : " + ia.getNomIa());
                    logsIA.add("🛑 Passe son tour à cause du Malus !");
                    ia.setEffetActif("AUCUN");
                    joueurRepository.save(ia);
                    continue;
                }

                int posAvant = ia.getPositionPlateau();
                
                Question q = historiqueQuestionService.tirerQuestionPourJoueur(ia);
                String qTexte = (q != null) ? q.getTexteQuestion() : "Question par défaut";
                String qBonne = (q != null) ? q.getBonneReponse() : "";

                int prob = rand.nextInt(100);
                boolean iaReussit = false;
                String statut = ia.getEffetActif() != null ? ia.getEffetActif() : "NORMAL";

                if ("INDICE".equals(statut)) {
                    if (prob >= 5 && prob < 95) iaReussit = true;
                } else if ("MALUS_PRESSION".equals(statut)) {
                    if (prob >= 80) iaReussit = (rand.nextInt(100) < 40); 
                } else {
                    if (prob >= 20 && prob < 80) iaReussit = true; 
                }

                TypeEffet effetDepartIa = moteurJeuService.getEffetForPosition(ia.getPositionPlateau(), plateauActuel);
                int deIa = 0;
                if (effetDepartIa == TypeEffet.PARI_MULTIPLICATEUR || "PARI_MULTIPLICATEUR".equals(ia.getEffetActif())) {
                    deIa = rand.nextInt(6) + 1;
                } else if (iaReussit) {
                    deIa = rand.nextInt(6) + 1;
                }
                
                MoteurJeuService.ResultatTour resultatIa = moteurJeuService.traiterReponse(
                    partieId, ia.getId(), iaReussit, deIa, plateauActuel, taillePlateau
                );
                
                String actionSupp = "";
                if (resultatIa.effetEnAttente() != null && resultatIa.effetEnAttente().name().startsWith("CIBLE_")) {
                    List<PartieJoueur> ciblesNormales = new ArrayList<>();
                    List<PartieJoueur> ciblesEchange = new ArrayList<>();
                    
                    for (PartieJoueur p : joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId)) {
                        if (!"BOUCLIER".equals(p.getEffetActif())) {
                            ciblesEchange.add(p);
                            if (!p.getId().equals(ia.getId())) ciblesNormales.add(p);
                        }
                    }
                    
                    if (resultatIa.effetEnAttente().name().equals("CIBLE_ECHANGE") && ciblesEchange.size() >= 2) {
                        Collections.shuffle(ciblesEchange);
                        moteurJeuService.appliquerEffetInteractif(ia.getId(), ciblesEchange.get(0).getId(), ciblesEchange.get(1).getId());
                        String n1 = ciblesEchange.get(0).isEstIa() ? ciblesEchange.get(0).getNomIa() : "Vous";
                        String n2 = ciblesEchange.get(1).isEstIa() ? ciblesEchange.get(1).getNomIa() : "Vous";
                        actionSupp = " 🔄 Échange " + n1 + " avec " + n2;
                    } else if (!resultatIa.effetEnAttente().name().equals("CIBLE_ECHANGE") && !ciblesNormales.isEmpty()) {
                        PartieJoueur c = ciblesNormales.get(rand.nextInt(ciblesNormales.size()));
                        moteurJeuService.appliquerEffetInteractif(ia.getId(), c.getId(), null);
                        actionSupp = " 🎯 Cible " + (c.isEstIa() ? c.getNomIa() : "Vous") + " avec " + resultatIa.effetEnAttente().name();
                    } else {
                        PartieJoueur iaComp = joueurRepository.findById(ia.getId()).get();
                        iaComp.setPositionPlateau(Math.min(taillePlateau, iaComp.getPositionPlateau() + deIa));
                        iaComp.setEffetActif("AUCUN");
                        joueurRepository.save(iaComp);
                        actionSupp = " 🎁 +" + deIa + " cases bonus (Tous immunisés)";
                    }
                }

                PartieJoueur iaApresEffet = joueurRepository.findById(ia.getId()).get();
                int posApres = iaApresEffet.getPositionPlateau();
                
                logsIA.add("--------------------------------------------------");
                logsIA.add("🤖 TOUR DE : " + ia.getNomIa());
                logsIA.add("❓ Q : " + qTexte);
                
                if (effetDepartIa == TypeEffet.PARI_MULTIPLICATEUR || "PARI_MULTIPLICATEUR".equals(ia.getEffetActif())) {
                    logsIA.add("💬 R : Pari de " + deIa + " -> " + (iaReussit ? "✅ Gagné" : "❌ Perdu (" + qBonne + ")"));
                } else {
                    logsIA.add("💬 R : " + (iaReussit ? "✅ Juste" : "❌ Faux (" + qBonne + ")"));
                }

                String logMouvement = "🎲 Dé: " + deIa + " | 📍 Case " + posAvant + " ➡️ " + posApres;
                if (resultatIa.messageEffet() != null && !resultatIa.messageEffet().isEmpty()) {
                    logMouvement += " | ✨ " + resultatIa.messageEffet();
                }
                if (!actionSupp.isEmpty()) {
                    logMouvement += " | " + actionSupp;
                }
                logsIA.add(logMouvement);
            }
        }
        
        response.setLogsIA(logsIA);

        boolean partieTerminee = false;
        String nomVainqueur = "";
        tousLesJoueurs = joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId); 
        for (PartieJoueur p : tousLesJoueurs) {
            if (p.getPositionPlateau() >= taillePlateau) {
                partieTerminee = true;
                nomVainqueur = p.isEstIa() ? p.getNomIa() : "Vous";
                Partie partie = p.getPartie();
                if (!"TERMINEE".equals(partie.getStatut())) {
                    partie.setStatut("TERMINEE");
                    if (!p.isEstIa()) partie.setVainqueur(p.getUtilisateur());
                    partieRepository.save(partie);
                }
                break;
            }
        }
        response.setPartieTerminee(partieTerminee);
        response.setNomVainqueur(nomVainqueur);
    }

    @PostMapping("/demarrer")
    public ResponseEntity<Map<String, Object>> demarrerPartie(@RequestBody DemarrerJeuDto request) {
        Partie partie = partieService.creerPartieSolo(request.getUtilisateurId(), request.getNbBots());
        int tailleFinale = request.getTaillePlateau() > 0 ? request.getTaillePlateau() + 1 : 50;
        int taille = request.getTaillePlateau() > 0 ? request.getTaillePlateau() : 50;

        if (request.getTypePlateau() == 0 || request.getTypePlateau() == 1) {
            Map<Integer, MoteurJeuService.CasePlateau> plateau = new HashMap<>();
            List<TypeEffet> tousLesEffets = new ArrayList<>(Arrays.asList(TypeEffet.values()));
            tousLesEffets.remove(TypeEffet.AUCUN);

            int nbEffets = taille / 2;
            List<TypeEffet> pool = new ArrayList<>(tousLesEffets);
            Random rand = new Random();
            
            while (pool.size() < nbEffets) {
                pool.add(tousLesEffets.get(rand.nextInt(tousLesEffets.size())));
            }
            Collections.shuffle(pool);
            if (pool.size() > nbEffets) pool = pool.subList(0, nbEffets);

            List<Integer> positions = new ArrayList<>();
            for (int i = 1; i <= taille; i++) positions.add(i); 
            Collections.shuffle(positions);
            List<Integer> posEffets = positions.subList(0, nbEffets);

            int effetIdx = 0;
            for (int i = 1; i <= taille; i++) {
                String cat = TOUTES_CATEGORIES.get(rand.nextInt(TOUTES_CATEGORIES.size()));
                int pts = rand.nextInt(6) + 1;
                if (posEffets.contains(i)) {
                    plateau.put(i, new MoteurJeuService.CasePlateau(pool.get(effetIdx++), cat, pts));
                } else {
                    plateau.put(i, new MoteurJeuService.CasePlateau(TypeEffet.AUCUN, cat, pts));
                }
            }
            casePlateauService.sauvegarderPlateauComplet(partie.getId(), plateau);
        } else if (request.getTypePlateau() == 2 && request.getPlateauCustom() != null) {
            casePlateauService.sauvegarderPlateauComplet(partie.getId(), request.getPlateauCustom());
        }

        Long vraiJoueurId = request.getUtilisateurId(); 
        for (PartieJoueur pj : joueurRepository.findAll()) {
            if (pj.getPartie() != null && pj.getPartie().getId().equals(partie.getId()) && !pj.isEstIa()) {
                vraiJoueurId = pj.getId();
                break;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("partieId", partie.getId());
        response.put("joueurId", vraiJoueurId); 
        response.put("taillePlateau", tailleFinale);
        response.put("message", "Partie initialisée avec succès !");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{partieId}/{joueurId}")
    public ResponseEntity<QuestionDto> obtenirQuestion(@PathVariable Long partieId, @PathVariable Long joueurId) {
        PartieJoueur joueur = joueurRepository.findById(joueurId).orElseThrow();
        
        Map<Integer, MoteurJeuService.CasePlateau> plateau = casePlateauService.chargerPlateauPourMoteur(partieId);
        MoteurJeuService.CasePlateau caseActuelle = plateau.isEmpty() ? null : plateau.get(joueur.getPositionPlateau());
        
        Question q;
        if (caseActuelle != null && caseActuelle.getCategorie() != null) {
            q = historiqueQuestionService.tirerQuestionParCategorie(joueur, caseActuelle.getCategorie());
        } else {
            q = historiqueQuestionService.tirerQuestionPourJoueur(joueur);
        }

        if (q == null) return ResponseEntity.notFound().build();

        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(q.getId());
        dto.setTexteQuestion(q.getTexteQuestion());

        if ("INDICE".equals(joueur.getEffetActif())) {
            dto.setIndiceTexte(genererIndice(q.getBonneReponse()));
        }
        
        String type = (q.getTypeQuestion() != null) ? q.getTypeQuestion().toUpperCase() : "TEXTE";
        
        List<String> mauvaisesProps = new ArrayList<>(Arrays.asList(q.getMauvaiseProp1(), q.getMauvaiseProp2(), q.getMauvaiseProp3()));
        mauvaisesProps.removeIf(p -> p == null || p.trim().isEmpty());

        if (mauvaisesProps.size() > 0 || type.contains("QCM") || type.contains("VRAI_FAUX")) {
            List<String> props = new ArrayList<>();
            props.add(q.getBonneReponse());
            props.addAll(mauvaisesProps);
            Collections.shuffle(props);
            dto.setPropositions(props);
            dto.setTypeQuestion("QCM");
        } else {
            dto.setPropositions(new ArrayList<>());
            dto.setTypeQuestion("TEXTE");
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/repondre")
    public ResponseEntity<ReponseTourDto> repondreQuestion(@RequestBody ReponseDto request) {
        PartieJoueur jActuel = joueurRepository.findById(request.getJoueurId()).orElseThrow();
        
        if ("MALUS_PASSE_TOUR".equals(jActuel.getEffetActif())) {
            jActuel.setEffetActif("AUCUN");
            joueurRepository.save(jActuel);
            
            ReponseTourDto response = new ReponseTourDto();
            response.setEtaitBonneReponse(false);
            response.setValeurDe(0);
            response.setNouvellePosition(jActuel.getPositionPlateau());
            response.setPositionAvantBots(jActuel.getPositionPlateau());
            response.setResultatTour(new MoteurJeuService.ResultatTour(false, null, false, "🛑 Tour passé (Malus)."));
            response.setBonneReponse("");
            
            Map<Integer, MoteurJeuService.CasePlateau> plateauActuel = casePlateauService.chargerPlateauPourMoteur(request.getPartieId());
            int taillePlateau = plateauActuel.isEmpty() ? 50 : Collections.max(plateauActuel.keySet()) + 1;
            faireJouerBots(request.getPartieId(), response, taillePlateau, plateauActuel);
            
            return ResponseEntity.ok(response);
        }

        Question vraieQuestion = questionRepository.findById(request.getQuestionId()).orElseThrow();
        String reponseBrute = (request.getReponseJoueur() != null) ? request.getReponseJoueur().trim() : "";
        
        int valeurDePari = 1;
        if (reponseBrute.contains("_PARI_")) {
            String[] parts = reponseBrute.split("_PARI_");
            try { valeurDePari = Integer.parseInt(parts[0]); } catch(Exception e) { valeurDePari = 1; }
            reponseBrute = (parts.length > 1) ? parts[1] : ""; 
        }

        boolean estBonneReponse;
        if (reponseBrute.equalsIgnoreCase("PASSER") || reponseBrute.isEmpty() || reponseBrute.equals("PASSER_MALUS")) {
            estBonneReponse = false;
        } else {
            estBonneReponse = iaJugeService.evaluerReponse(vraieQuestion.getBonneReponse(), vraieQuestion.getSynonymesAcceptes() != null ? vraieQuestion.getSynonymesAcceptes() : "", reponseBrute);
        }

        Map<Integer, MoteurJeuService.CasePlateau> plateauActuel = casePlateauService.chargerPlateauPourMoteur(request.getPartieId());
        int taillePlateau = plateauActuel.isEmpty() ? 50 : Collections.max(plateauActuel.keySet()) + 1;

        MoteurJeuService.CasePlateau caseActuelle = plateauActuel.get(jActuel.getPositionPlateau());
        int valeurDe = 0;
        TypeEffet effetDepartActuel = moteurJeuService.getEffetForPosition(jActuel.getPositionPlateau(), plateauActuel);
        
        if (effetDepartActuel == TypeEffet.PARI_MULTIPLICATEUR || "PARI_MULTIPLICATEUR".equals(jActuel.getEffetActif())) {
            valeurDe = valeurDePari; 
        } else if (estBonneReponse) {
            if (caseActuelle != null && caseActuelle.getPoints() > 0) valeurDe = caseActuelle.getPoints(); 
            else valeurDe = new Random().nextInt(6) + 1; 
        }

        MoteurJeuService.ResultatTour resultat = moteurJeuService.traiterReponse(request.getPartieId(), request.getJoueurId(), estBonneReponse, valeurDe, plateauActuel, taillePlateau);

        boolean doitAttendreCible = false;
        
        if (resultat.effetEnAttente() != null) {
            if (resultat.effetEnAttente().name().startsWith("CIBLE_")) {
                List<PartieJoueur> tousJ = joueurRepository.findByPartieIdOrderByOrdreTourAsc(request.getPartieId());
                boolean canTarget = false;
                if (resultat.effetEnAttente().name().equals("CIBLE_ECHANGE")) {
                    long dispo = tousJ.stream().filter(p -> !"BOUCLIER".equals(p.getEffetActif()) || p.getId().equals(request.getJoueurId())).count();
                    canTarget = dispo >= 2;
                } else {
                    long dispo = tousJ.stream().filter(p -> !p.getId().equals(request.getJoueurId()) && !"BOUCLIER".equals(p.getEffetActif())).count();
                    canTarget = dispo >= 1;
                }

                if (!canTarget) {
                    PartieJoueur jComp = joueurRepository.findById(request.getJoueurId()).get();
                    jComp.setPositionPlateau(Math.min(taillePlateau, jComp.getPositionPlateau() + valeurDe));
                    jComp.setEffetActif("AUCUN");
                    joueurRepository.save(jComp);
                    String msg = resultat.messageEffet() + " Mais vu qu'il n'y a pas de cible disponible, vous avancez de 2 fois le chiffre ! (+" + valeurDe + " cases bonus)";
                    resultat = new MoteurJeuService.ResultatTour(jComp.getPositionPlateau() >= taillePlateau, null, resultat.aDroitDeuxiemeChance(), msg);
                } else {
                    doitAttendreCible = true; 
                }
            } else if (resultat.effetEnAttente() == TypeEffet.SUPER_BONUS) {
                doitAttendreCible = true;
            }
        }

        PartieJoueur jPostReponse = joueurRepository.findById(request.getJoueurId()).orElseThrow();
        int posAvantBots = jPostReponse.getPositionPlateau();

        ReponseTourDto response = new ReponseTourDto();
        response.setResultatTour(resultat);
        response.setEtaitBonneReponse(estBonneReponse);
        response.setValeurDe(valeurDe);
        response.setBonneReponse(vraieQuestion.getBonneReponse());
        response.setPositionAvantBots(posAvantBots); 

        if (!resultat.aDroitDeuxiemeChance() && !resultat.victoire() && !doitAttendreCible) {
            faireJouerBots(request.getPartieId(), response, taillePlateau, plateauActuel);
        } else if (resultat.aDroitDeuxiemeChance()) {
            response.setLogsIA(new ArrayList<>(Collections.singletonList("✨ Deuxième chance activée ! Les bots attendent votre nouvelle tentative.")));
        } else {
            response.setLogsIA(new ArrayList<>());
        }

        PartieJoueur jActuelPostBots = joueurRepository.findById(request.getJoueurId()).orElseThrow();
        response.setNouvellePosition(jActuelPostBots.getPositionPlateau());

        if (resultat.victoire()) {
            response.setPartieTerminee(true);
            response.setNomVainqueur("Vous");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/appliquer-effet")
    public ResponseEntity<ReponseTourDto> appliquerEffetInteractif(@RequestBody AppliquerEffetDto req) {
        PartieJoueur lanceur = joueurRepository.findById(req.getLanceurId()).orElseThrow();
        Long partieId = lanceur.getPartie().getId();

        if ("CIBLE_ECHANGE".equals(lanceur.getEffetActif())) {
            moteurJeuService.appliquerEffetInteractif(req.getLanceurId(), req.getCible1Id(), req.getCible2Id());
        } else {
            moteurJeuService.appliquerEffetInteractif(req.getLanceurId(), req.getCible1Id(), null);
        }
        
        ReponseTourDto response = new ReponseTourDto();
        Map<Integer, MoteurJeuService.CasePlateau> plateauActuel = casePlateauService.chargerPlateauPourMoteur(partieId);
        int taillePlateau = plateauActuel.isEmpty() ? 50 : Collections.max(plateauActuel.keySet()) + 1;
        
        faireJouerBots(partieId, response, taillePlateau, plateauActuel);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/preparer-super-bonus")
    public ResponseEntity<QuestionDto> preparerSuperBonus(@RequestBody PreparerSuperBonusDto req) {
        PartieJoueur joueur = joueurRepository.findById(req.getJoueurId()).orElseThrow();
        Question q = historiqueQuestionService.tirerQuestionParCategorie(joueur, req.getCategorie());
        if (q == null) return ResponseEntity.notFound().build();

        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(q.getId());
        dto.setTexteQuestion(q.getTexteQuestion());

        String type = (q.getTypeQuestion() != null) ? q.getTypeQuestion().toUpperCase() : "TEXTE";
        List<String> mauvaisesProps = new ArrayList<>(Arrays.asList(q.getMauvaiseProp1(), q.getMauvaiseProp2(), q.getMauvaiseProp3()));
        mauvaisesProps.removeIf(p -> p == null || p.trim().isEmpty());

        if (mauvaisesProps.size() > 0 || type.contains("QCM") || type.contains("VRAI_FAUX")) {
            List<String> props = new ArrayList<>();
            props.add(q.getBonneReponse());
            props.addAll(mauvaisesProps);
            Collections.shuffle(props);
            dto.setPropositions(props);
            dto.setTypeQuestion("QCM");
        } else {
            dto.setPropositions(new ArrayList<>());
            dto.setTypeQuestion("TEXTE");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/repondre-super-bonus")
    public ResponseEntity<ReponseTourDto> repondreSuperBonus(@RequestBody RepondreSuperBonusDto req) {
        Question vraieQuestion = questionRepository.findById(req.getQuestionId()).orElseThrow();
        String reponseBrute = (req.getReponseJoueur() != null) ? req.getReponseJoueur().trim() : "";
        
        boolean estBonneReponse = iaJugeService.evaluerReponse(
            vraieQuestion.getBonneReponse(),
            vraieQuestion.getSynonymesAcceptes() != null ? vraieQuestion.getSynonymesAcceptes() : "",
            reponseBrute
        );

        PartieJoueur joueur = joueurRepository.findById(req.getJoueurId()).orElseThrow();
        Map<Integer, MoteurJeuService.CasePlateau> plateauActuel = casePlateauService.chargerPlateauPourMoteur(req.getPartieId());
        int taillePlateau = plateauActuel.isEmpty() ? 50 : Collections.max(plateauActuel.keySet()) + 1;

        String messageEffet = "";
        TypeEffet effetEnAttente = null;
        boolean doitAttendreCible = false;

        if (estBonneReponse) {
            boolean canTarget = true;
            if (req.getEffetChoisi() != null && req.getEffetChoisi().startsWith("CIBLE_")) {
                List<PartieJoueur> tousJ = joueurRepository.findByPartieIdOrderByOrdreTourAsc(req.getPartieId());
                if (req.getEffetChoisi().equals("CIBLE_ECHANGE")) {
                    long dispo = tousJ.stream().filter(p -> !"BOUCLIER".equals(p.getEffetActif()) || p.getId().equals(req.getJoueurId())).count();
                    canTarget = dispo >= 2;
                } else {
                    long dispo = tousJ.stream().filter(p -> !p.getId().equals(req.getJoueurId()) && !"BOUCLIER".equals(p.getEffetActif())).count();
                    canTarget = dispo >= 1;
                }
            }

            if (req.getEffetChoisi() != null && req.getEffetChoisi().startsWith("CIBLE_") && !canTarget) {
                joueur.setPositionPlateau(Math.min(joueur.getPositionPlateau() + (req.getPointsChoisis() * 2), taillePlateau));
                joueur.setEffetActif("BOUCLIER"); 
                joueur.setDureeEffet(2);
                messageEffet = "🌟 EXCELLENT ! Vu qu'il n'y a pas de cible disponible, vous avancez de 2 fois le chiffre (" + (req.getPointsChoisis() * 2) + " cases) !";
                effetEnAttente = null;
            } else {
                joueur.setPositionPlateau(Math.min(joueur.getPositionPlateau() + req.getPointsChoisis(), taillePlateau));
                joueur.setEffetActif(req.getEffetChoisi()); 
                joueur.setDureeEffet("BOUCLIER".equals(req.getEffetChoisi()) ? 2 : 1);
                messageEffet = "🌟 EXCELLENT ! Vous avancez de " + req.getPointsChoisis() + " cases et obtenez l'effet : " + req.getEffetChoisi() + " !";
                if (req.getEffetChoisi() != null && req.getEffetChoisi().startsWith("CIBLE_")) {
                    try { 
                        effetEnAttente = TypeEffet.valueOf(req.getEffetChoisi()); 
                        doitAttendreCible = true; 
                    } catch (Exception e) {}
                }
            }
        } else {
            joueur.setEffetActif("BOUCLIER");
            joueur.setDureeEffet(2);
            messageEffet = "❌ Mauvaise réponse ! Mais vous gardez le Bouclier intact.";
        }
        joueurRepository.save(joueur);

        int posAvantBots = joueur.getPositionPlateau(); 

        boolean partieTerminee = joueur.getPositionPlateau() >= taillePlateau;
        String nomVainqueur = partieTerminee ? "Vous" : "";
        List<String> logs_ia = new ArrayList<>();
        
        if (partieTerminee) {
            Partie partie = joueur.getPartie();
            partie.setStatut("TERMINEE");
            partie.setVainqueur(joueur.getUtilisateur());
            partieRepository.save(partie);
        }

        MoteurJeuService.ResultatTour res = new MoteurJeuService.ResultatTour(
            partieTerminee, effetEnAttente, false, messageEffet
        );

        ReponseTourDto response = new ReponseTourDto();
        response.setResultatTour(res);
        response.setEtaitBonneReponse(estBonneReponse);
        response.setValeurDe(estBonneReponse ? req.getPointsChoisis() : 0);
        response.setBonneReponse(vraieQuestion.getBonneReponse());
        response.setPositionAvantBots(posAvantBots); 

        if (!partieTerminee && !doitAttendreCible) {
            faireJouerBots(req.getPartieId(), response, taillePlateau, plateauActuel);
        } else {
            response.setLogsIA(logs_ia); 
        }

        response.setNouvellePosition(joueur.getPositionPlateau());
        if (partieTerminee) {
            response.setPartieTerminee(true);
            response.setNomVainqueur("Vous");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/etat/{partieId}")
    public ResponseEntity<EtatPartieDto> obtenirEtatPartie(@PathVariable Long partieId) {
        List<PartieJoueur> joueurs = joueurRepository.findAll();
        EtatPartieDto etat = new EtatPartieDto();
        etat.setPartieId(partieId);
        List<JoueurEtatDto> listeJoueurs = new ArrayList<>();

        for (PartieJoueur pj : joueurs) {
            if (pj.getPartie() != null && pj.getPartie().getId().equals(partieId)) {
                JoueurEtatDto dto = new JoueurEtatDto();
                dto.setId(pj.getId());
                dto.setNom(pj.isEstIa() ? pj.getNomIa() : "Vous (Joueur)"); 
                dto.setPosition(pj.getPositionPlateau());
                dto.setEstIa(pj.isEstIa());
                String effet = pj.getEffetActif();
                dto.setStatut((effet != null && !effet.equals("AUCUN")) ? effet : "NORMAL");
                dto.setDureeEffet(pj.getDureeEffet()); 
                listeJoueurs.add(dto);
            }
        }
        etat.setJoueurs(listeJoueurs);
        return ResponseEntity.ok(etat);
    }
}