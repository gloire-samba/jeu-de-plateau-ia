package com.iaspring.backspring.service;

import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import com.iaspring.backspring.entity.*;
import com.iaspring.backspring.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CliJeuService implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PartieService partieService;
    private final PartieRepository partieRepository;
    private final PartieJoueurRepository joueurRepository;
    private final IaJugeService iaJugeService;
    private final MoteurJeuService moteurJeuService;
    private final HistoriqueQuestionService historiqueQuestionService;

    private final boolean ACTIVER_CLI = false;

    private final List<String> TOUTES_CATEGORIES = Arrays.asList(
        "HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"
    );

    @Override
    public void run(String... args) {
        if (!ACTIVER_CLI) return;
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n===========================================");
        System.out.println("🚀 BIENVENUE DANS LE JEU DE PLATEAU IA (CLI)");
        System.out.println("===========================================\n");

        Utilisateur humain = authentifierOuCreer(scanner);
        if (humain == null) return;

        System.out.println("\nCombien de Bots voulez-vous affronter ?");
        String nbIaStr = inputAvecValidation("Votre choix (1-3) : ", Arrays.asList("1", "2", "3"), "1, 2 ou 3", scanner);
        int nbIa = Integer.parseInt(nbIaStr);

        Partie partie = partieService.creerPartieSolo(humain.getId(), nbIa);

        List<TypeEffet> tousLesEffets = new ArrayList<>(Arrays.asList(TypeEffet.values()));
        tousLesEffets.remove(TypeEffet.AUCUN);

        System.out.println("\nChoisissez le type de plateau :");
        System.out.println("  0) Classique (49 cases intermédiaires, Arrivée à 50)");
        System.out.println("  1) Aléatoire (50% cases à effets réparties équitablement)");
        System.out.println("  2) Personnalisé (Construisez vous-même votre plateau)");
        String choixPlateau = inputAvecValidation("Votre choix (0-2) : ", Arrays.asList("0", "1", "2"), "0, 1 ou 2", scanner);

        Map<Integer, MoteurJeuService.CasePlateau> plateauActuel = null;
        int taillePlateau = 50; 
        Random rand = new Random();

        if (choixPlateau.equals("1")) {
            System.out.println();
            int nbCases = Integer.parseInt(inputAvecConditionMath("Entrez le nombre de cases INTERMÉDIAIRES (min 20) : ", 20, Integer.MAX_VALUE, scanner));
            taillePlateau = nbCases + 1; 
            plateauActuel = new HashMap<>();
            
            int nbEffets = nbCases / 2;
            List<TypeEffet> pool = new ArrayList<>(tousLesEffets);
            
            while (pool.size() < nbEffets) {
                pool.add(tousLesEffets.get(rand.nextInt(tousLesEffets.size())));
            }
            
            Collections.shuffle(pool);
            if (pool.size() > nbEffets) {
                pool = pool.subList(0, nbEffets);
            }

            List<Integer> positions = new ArrayList<>();
            for (int i = 1; i <= nbCases; i++) positions.add(i); 
            Collections.shuffle(positions);
            List<Integer> posEffets = positions.subList(0, nbEffets);

            int effetIdx = 0;
            for (int i = 1; i <= nbCases; i++) {
                String cat = TOUTES_CATEGORIES.get(rand.nextInt(TOUTES_CATEGORIES.size()));
                int pts = rand.nextInt(6) + 1;
                if (posEffets.contains(i)) {
                    plateauActuel.put(i, new MoteurJeuService.CasePlateau(pool.get(effetIdx++), cat, pts));
                } else {
                    plateauActuel.put(i, new MoteurJeuService.CasePlateau(TypeEffet.AUCUN, cat, pts));
                }
            }
            System.out.println("✅ Plateau aléatoire généré avec " + nbCases + " cases intermédiaires et " + nbEffets + " effets (Arrivée à la case " + taillePlateau + ").");

        } else if (choixPlateau.equals("2")) {
            System.out.println();
            int nbCases = Integer.parseInt(inputAvecConditionMath("Entrez le nombre de cases INTERMÉDIAIRES (minimum 1) : ", 1, Integer.MAX_VALUE, scanner));
            taillePlateau = nbCases + 1;
            plateauActuel = new HashMap<>();
            
            Set<Integer> dispos = new TreeSet<>();
            for (int i = 1; i <= nbCases; i++) dispos.add(i);

            int indexEffet = 0;
            while (!dispos.isEmpty()) {
                TypeEffet effetEnCours = tousLesEffets.get(indexEffet);
                System.out.println("\n--- CRÉATION DU PLATEAU ---");
                System.out.println("Effet sélectionné : [" + effetEnCours.name() + "]");
                System.out.println("Cases disponibles : " + dispos);
                
                List<String> commandesAutorisees = new ArrayList<>();
                commandesAutorisees.add("T");
                for (int d : dispos) commandesAutorisees.add(String.valueOf(d));
                
                StringBuilder prompt = new StringBuilder("Commandes : ");
                if (indexEffet > 0) { prompt.append("[P] Précédent | "); commandesAutorisees.add("P"); }
                if (indexEffet < tousLesEffets.size() - 1) { prompt.append("[S] Suivant | "); commandesAutorisees.add("S"); }
                
                if (!plateauActuel.isEmpty()) { prompt.append("[E] Effacer une case | "); commandesAutorisees.add("E"); }
                
                prompt.append("[T] Terminer (et remplir de cases normales) | [Numéro] Placer ici\nVotre choix : ");

                String reponse = inputAvecValidation(prompt.toString(), commandesAutorisees, "P, S, E, T ou un numéro libre", scanner);
                
                if (reponse.equals("P")) indexEffet--;
                else if (reponse.equals("S")) indexEffet++;
                else if (reponse.equals("T")) break;
                else if (reponse.equals("E")) {
                    System.out.println("\nCases déjà placées :");
                    List<String> validEffacer = new ArrayList<>();
                    validEffacer.add("A");
                    for (Map.Entry<Integer, MoteurJeuService.CasePlateau> entry : plateauActuel.entrySet()) {
                        System.out.println("  " + entry.getKey() + ") " + entry.getValue().getEffet().name());
                        validEffacer.add(String.valueOf(entry.getKey()));
                    }
                    String repEffacer = inputAvecValidation("Numéro de la case à effacer (ou [A] Annuler) : ", validEffacer, "un numéro placé ou A", scanner);
                    if (!repEffacer.equals("A")) {
                        int caseAEffacer = Integer.parseInt(repEffacer);
                        plateauActuel.remove(caseAEffacer);
                        dispos.add(caseAEffacer); 
                        System.out.println("✅ Case " + caseAEffacer + " effacée et de nouveau disponible !");
                    }
                }
                else {
                    int caseChoisie = Integer.parseInt(reponse);
                    String cat = TOUTES_CATEGORIES.get(rand.nextInt(TOUTES_CATEGORIES.size()));
                    int pts = rand.nextInt(6) + 1;
                    plateauActuel.put(caseChoisie, new MoteurJeuService.CasePlateau(effetEnCours, cat, pts));
                    dispos.remove(caseChoisie);
                    System.out.println("✅ " + effetEnCours.name() + " placé sur la case " + caseChoisie + " ! (Catégorie : " + cat + ", " + pts + " pts)");
                }
            }
            
            System.out.println("⏳ Remplissage des cases restantes avec des cases normales...");
            for (int d : dispos) {
                String cat = TOUTES_CATEGORIES.get(rand.nextInt(TOUTES_CATEGORIES.size()));
                int pts = rand.nextInt(6) + 1;
                plateauActuel.put(d, new MoteurJeuService.CasePlateau(TypeEffet.AUCUN, cat, pts));
            }
            System.out.println("✅ Plateau personnalisé terminé (Arrivée à la case " + taillePlateau + ") !");
        }

        boucleDeJeu(partie.getId(), scanner, plateauActuel, taillePlateau, tousLesEffets);
    }

    private Utilisateur authentifierOuCreer(Scanner scanner) {
        System.out.print("Email : "); String email = scanner.nextLine();
        System.out.print("Password : "); String mdp = scanner.nextLine();
        Optional<Utilisateur> opt = utilisateurRepository.findByEmail(email);
        if (opt.isPresent() && opt.get().getMotDePasse().equals(mdp)) {
            System.out.println("✅ Bienvenue " + opt.get().getPseudo());
            return opt.get();
        }
        System.out.println("⚠️ Nouveau compte.");
        System.out.print("Pseudo : "); String pseudo = scanner.nextLine();
        Utilisateur u = new Utilisateur(); u.setEmail(email); u.setMotDePasse(mdp); u.setPseudo(pseudo);
        return utilisateurRepository.save(u);
    }

    private String inputAvecConditionMath(String promptMsg, int min, int max, Scanner scanner) {
        System.out.println(); 
        System.out.print(promptMsg);
        while (true) {
            String ans = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(ans);
                if (val >= min && val <= max) return ans;
                else throw new Exception();
            } catch (Exception e) {
                String errorMsg = "⚠️ Valeur invalide ('" + ans + "'). Veuillez taper un nombre >= " + min + ".";
                System.out.print("\033[1A\033[K\033[1A\033[K\r" + errorMsg + "\n\r" + promptMsg);
                System.out.flush();
            }
        }
    }

    private String inputAvecValidation(String promptMsg, List<String> validChoices, String errorFormat, Scanner scanner) {
        System.out.println(); 
        System.out.print(promptMsg);
        while (true) {
            String ans = scanner.nextLine().trim();
            if (validChoices.contains(ans.toUpperCase())) return ans.toUpperCase();
            
            String errorMsg = "⚠️ Mauvaise commande ('" + ans + "'). Veuillez taper " + errorFormat + ".";
            System.out.print("\033[1A\033[K\033[1A\033[K\r" + errorMsg + "\n\r" + promptMsg);
            System.out.flush();
        }
    }

    private String lireEntreeAvecChrono(String prompt, int timeoutSeconds, Scanner scanner, List<String> validChoices, String errorFormat) {
        System.out.println("\n"); 
        System.out.print(prompt);
        String errorMsg = "";
        try {
            for (int i = timeoutSeconds * 10; i > 0; i--) {
                if (System.in.available() > 0) {
                    String ans = scanner.nextLine().trim();
                    if (validChoices == null || validChoices.isEmpty() || validChoices.contains(ans.toUpperCase())) return ans;
                    errorMsg = "⚠️ Mauvaise commande ('" + ans + "'). Veuillez taper " + errorFormat + ".";
                    System.out.print("\033[1A\033[K\r" + prompt);   
                    System.out.flush();
                }
                if (i % 10 == 0) {
                    System.out.print("\033[s\033[2A\r\033[K" + errorMsg + "\n\r\033[K⏳ " + (i / 10) + "s restantes...\033[u");   
                    System.out.flush();
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {}
        System.out.println("\n\n⏳ TEMPS ÉCOULÉ !");
        return "TEMPS_ECOULE";
    }

    private void boucleDeJeu(Long partieId, Scanner scanner, Map<Integer, MoteurJeuService.CasePlateau> plateau, int taillePlateau, List<TypeEffet> tousLesEffets) {
        boolean enCours = true; 
        Random rand = new Random();
        
        while (enCours) {
            List<PartieJoueur> tous = joueurRepository.findByPartieIdOrderByOrdreTourAsc(partieId);
            System.out.println("\n📊 RÉSUMÉ DU PLATEAU :");
            for (PartieJoueur pj : tous) {
                String nomP = pj.isEstIa() ? pj.getNomIa() : pj.getUtilisateur().getPseudo();
                String nomEffetP = pj.getEffetActif();
                if ("BONUS_PRESSION".equals(nomEffetP)) nomEffetP = "BONUS_PRESSION (Lanceur)";
                else if ("MALUS_PRESSION".equals(nomEffetP)) nomEffetP = "MALUS_PRESSION (Victime)";
                String eff = (nomEffetP != null && !nomEffetP.equals("AUCUN")) ? " (" + nomEffetP + ")" : "";
                System.out.println("   - " + nomP + " : Case " + pj.getPositionPlateau() + eff);
            }

            for (PartieJoueur j : tous) {
                if (j.getPositionPlateau() >= taillePlateau) { enCours = false; break; }
                String nom = j.isEstIa() ? j.getNomIa() : j.getUtilisateur().getPseudo();
                String nomEffetT = j.getEffetActif();
                if ("BONUS_PRESSION".equals(nomEffetT)) nomEffetT = "BONUS_PRESSION (Lanceur)";
                else if ("MALUS_PRESSION".equals(nomEffetT)) nomEffetT = "MALUS_PRESSION (Victime)";
                String effTour = (nomEffetT != null && !nomEffetT.equals("AUCUN")) ? " [Sous effet : " + nomEffetT + "]" : "";
                
                System.out.println("\n==================================================");
                System.out.println("👉 TOUR DE : " + nom + effTour + " | Case " + j.getPositionPlateau());

                if ("MALUS_PASSE_TOUR".equals(j.getEffetActif())) {
                    System.out.println("🛑 " + nom + " passe son tour à cause du Malus !");
                    j.setEffetActif("AUCUN"); joueurRepository.save(j);
                    try { Thread.sleep(1500); } catch (Exception e) {}
                    continue; 
                }

                int tempsLimite = 60; boolean aPression = false;
                if ("MALUS_PRESSION".equals(j.getEffetActif())) { tempsLimite = 30; aPression = true; j.setEffetActif("AUCUN"); joueurRepository.save(j); } 
                else if ("BONUS_PRESSION".equals(j.getEffetActif())) { tempsLimite = 90; j.setEffetActif("AUCUN"); joueurRepository.save(j); }

                boolean aIndice = "INDICE".equals(j.getEffetActif());
                if (aIndice) { j.setEffetActif("AUCUN"); joueurRepository.save(j); }
                
                MoteurJeuService.CasePlateau caseA = (plateau != null) ? plateau.get(j.getPositionPlateau()) : null;
                TypeEffet effetDepart = moteurJeuService.getEffetForPosition(j.getPositionPlateau(), plateau);
                
                int pariChoisi = 0;
                if (effetDepart == TypeEffet.PARI_MULTIPLICATEUR) {
                    System.out.println("\n🎰 EFFET QUITTE OU DOUBLE !");
                    System.out.println("Choisissez un multiplicateur (1 à 6). Si vous répondez juste, vous avancerez de : multiplicateur * points de la case.");
                    System.out.println("Si vous vous trompez, vous RECULEREZ de ce même montant !");
                    if (!j.isEstIa()) {
                        String inPari = inputAvecValidation("Votre multiplicateur (1-6) : ", Arrays.asList("1", "2", "3", "4", "5", "6"), "un chiffre entre 1 et 6", scanner);
                        pariChoisi = Integer.parseInt(inPari);
                    } else {
                        pariChoisi = rand.nextInt(6) + 1;
                        System.out.println("🤖 L'IA prend un risque et choisit le multiplicateur : " + pariChoisi);
                        try { Thread.sleep(1200); } catch (Exception e) {}
                    }
                }

                Question q;
                if (caseA != null && caseA.getCategorie() != null) {
                    System.out.println("🏷️ Catégorie imposée par la case : " + caseA.getCategorie());
                    q = historiqueQuestionService.tirerQuestionParCategorie(j, caseA.getCategorie());
                } else {
                    q = historiqueQuestionService.tirerQuestionPourJoueur(j);
                }

                if (q == null) { System.out.println("❌ Erreur : Aucune question !"); enCours = false; break; }
                System.out.println("❓ QUESTION : " + q.getTexteQuestion());

                boolean isQcm = q.getTypeQuestion() != null && q.getTypeQuestion().startsWith("QCM");
                Map<String, String> map = new HashMap<>();
                if (isQcm) {
                    List<String> p = new ArrayList<>(Arrays.asList(q.getBonneReponse(), q.getMauvaiseProp1(), q.getMauvaiseProp2(), q.getMauvaiseProp3()));
                    p.removeIf(Objects::isNull); Collections.shuffle(p);
                    String[] L = {"A", "B", "C", "D"};
                    System.out.println("Propositions :");
                    for (int i=0; i<p.size(); i++) { map.put(L[i], p.get(i)); System.out.println("  " + L[i] + ") " + p.get(i)); }
                }

                String aff = "", eval = "";
                
                if (!j.isEstIa()) {
                    if (aIndice) System.out.println("💡 INDICE : " + q.getBonneReponse().substring(0, Math.max(1, q.getBonneReponse().length() / 2)) + "...");
                    String promptMsg = isQcm ? "Votre réponse (A/B/C/D) : " : "Votre réponse : ";
                    aff = lireEntreeAvecChrono(promptMsg, tempsLimite, scanner, isQcm ? Arrays.asList("A", "B", "C", "D") : null, isQcm ? "A, B, C ou D" : "");
                    eval = (isQcm && map.containsKey(aff.toUpperCase())) ? map.get(aff.toUpperCase()) : aff;
                } else {
                    if (aIndice) System.out.println("💡 L'IA utilise son indice...");
                    try { Thread.sleep(1200); } catch (Exception e) {}
                    int chance = rand.nextInt(100);
                    if (chance < (aIndice ? 5 : (aPression ? 80 : 20))) { aff = eval = "PASSER"; } 
                    else {
                        if (rand.nextInt(100) < (aIndice ? 90 : (aPression ? 40 : 60))) {
                            eval = q.getBonneReponse();
                            if (isQcm) { for (Map.Entry<String, String> e : map.entrySet()) if (e.getValue().equals(eval)) aff = e.getKey(); }
                            else aff = eval;
                        } else {
                            if (isQcm) {
                                List<String> f = new ArrayList<>(map.keySet()); f.removeIf(k -> map.get(k).equals(q.getBonneReponse()));
                                aff = f.isEmpty() ? "PASSER" : f.get(rand.nextInt(f.size())); eval = map.getOrDefault(aff, "PASSER");
                            } else { aff = eval = "Je ne sais pas."; }
                        }
                    }
                    System.out.println("🗣️ L'IA répond : " + aff);
                }

                boolean ok = false;
                if (eval.equalsIgnoreCase("PASSER") || eval.equals("TEMPS_ECOULE")) { System.out.println("⏭️ Passe ou Temps écoulé. 💡 Réponse : " + q.getBonneReponse()); } 
                else {
                    ok = iaJugeService.evaluerReponse(q.getBonneReponse(), q.getSynonymesAcceptes(), eval);
                    System.out.println(ok ? "✅ BONNE RÉPONSE !" : "❌ MAUVAISE ! 💡 Réponse : " + q.getBonneReponse());
                }

                int de = 0;
                
                if (effetDepart == TypeEffet.PARI_MULTIPLICATEUR) {
                    de = pariChoisi; 
                } else if (ok) {
                    if (caseA != null && caseA.getPoints() > 0) {
                        de = caseA.getPoints();
                        System.out.println("🎲 La case rapporte une valeur fixe de : " + de);
                    } else {
                        de = rand.nextInt(6) + 1;
                        System.out.println("🎲 Résultat du dé : " + de);
                    }
                }
                
                MoteurJeuService.ResultatTour res = moteurJeuService.traiterReponse(partieId, j.getId(), ok, de, plateau, taillePlateau);
                PartieJoueur jMaj = joueurRepository.findById(j.getId()).get();

                while (jMaj.getEffetActif() != null && (jMaj.getEffetActif().startsWith("CIBLE_") || jMaj.getEffetActif().equals("SUPER_BONUS"))) {
                    String effetNom = jMaj.getEffetActif();
                    final Long currentJoueurId = jMaj.getId();
                    
                    if (effetNom.equals("SUPER_BONUS")) {
                        // 👉 LOGIQUE SUPER BONUS
                        System.out.println("\n🌟 Le super bonus est activé et vous avez un bouclier pour le reste de ce tour et le tour suivant !");
                        jMaj.setEffetActif("BOUCLIER"); 
                        jMaj.setDureeEffet(2); 
                        joueurRepository.save(jMaj);

                        List<String> valid1to6 = Arrays.asList("1", "2", "3", "4", "5", "6");
                        String cChoix = TOUTES_CATEGORIES.get(0); 
                        int ptsChoix = 3; 
                        String eChoix = "AUCUN";

                        // 👉 CORRECTION : Liste dynamique des effets (Sauf AUCUN, SUPER_BONUS ET BOUCLIER)
                        List<TypeEffet> effetsSuperBonus = new ArrayList<>(Arrays.asList(TypeEffet.values()));
                        effetsSuperBonus.remove(TypeEffet.AUCUN);
                        effetsSuperBonus.remove(TypeEffet.SUPER_BONUS);
                        effetsSuperBonus.remove(TypeEffet.BOUCLIER); // EXCLU !

                        if (!jMaj.isEstIa()) {
                            System.out.println("\nSélectionnez une Catégorie :");
                            List<String> validCat = new ArrayList<>();
                            for (int i=0; i<TOUTES_CATEGORIES.size(); i++) {
                                System.out.println("  " + (i+1) + ") " + TOUTES_CATEGORIES.get(i));
                                validCat.add(String.valueOf(i+1));
                            }
                            String inC = inputAvecValidation("Numéro de la catégorie : ", validCat, "un chiffre valide", scanner);
                            cChoix = TOUTES_CATEGORIES.get(Integer.parseInt(inC) - 1);
                            
                            System.out.println("\nSélectionnez un Effet :");
                            List<String> validEffets = new ArrayList<>();
                            for (int i=0; i<effetsSuperBonus.size(); i++) {
                                System.out.println("  " + (i+1) + ") " + effetsSuperBonus.get(i).name());
                                validEffets.add(String.valueOf(i+1));
                            }
                            String inE = inputAvecValidation("Numéro de l'effet : ", validEffets, "un chiffre valide", scanner);
                            eChoix = effetsSuperBonus.get(Integer.parseInt(inE) - 1).name();

                            String inP = inputAvecValidation("Entrez le chiffre de la case (1-6) : ", valid1to6, "un chiffre entre 1 et 6", scanner);
                            ptsChoix = Integer.parseInt(inP);
                            
                        } else {
                            cChoix = TOUTES_CATEGORIES.get(rand.nextInt(TOUTES_CATEGORIES.size()));
                            eChoix = effetsSuperBonus.get(rand.nextInt(effetsSuperBonus.size())).name();
                            ptsChoix = rand.nextInt(6) + 1;
                            System.out.println("🤖 L'IA choisit la catégorie " + cChoix + ", l'effet " + eChoix + " et la valeur " + ptsChoix);
                            try { Thread.sleep(1500); } catch (Exception e) {}
                        }

                        Question qBonus = historiqueQuestionService.tirerQuestionParCategorie(jMaj, cChoix);
                        System.out.println("\n❓ QUESTION BONUS : " + qBonus.getTexteQuestion());
                        
                        boolean isQcmB = qBonus.getTypeQuestion() != null && qBonus.getTypeQuestion().startsWith("QCM");
                        Map<String, String> mapB = new HashMap<>();
                        if (isQcmB) {
                            List<String> p = new ArrayList<>(Arrays.asList(qBonus.getBonneReponse(), qBonus.getMauvaiseProp1(), qBonus.getMauvaiseProp2(), qBonus.getMauvaiseProp3()));
                            p.removeIf(Objects::isNull); Collections.shuffle(p);
                            String[] L = {"A", "B", "C", "D"};
                            System.out.println("Propositions :");
                            for (int i=0; i<p.size(); i++) { mapB.put(L[i], p.get(i)); System.out.println("  " + L[i] + ") " + p.get(i)); }
                        }

                        String repBonusEvaluee = "";
                        if (!jMaj.isEstIa()) {
                            String promptMsgB = isQcmB ? "Votre réponse (A/B/C/D) : " : "Votre réponse : \n";
                            String repAfficheeB = lireEntreeAvecChrono(promptMsgB, 60, scanner, isQcmB ? Arrays.asList("A", "B", "C", "D") : null, isQcmB ? "A, B, C ou D" : "");
                            repBonusEvaluee = (isQcmB && mapB.containsKey(repAfficheeB.toUpperCase())) ? mapB.get(repAfficheeB.toUpperCase()) : repAfficheeB;
                        } else {
                            int chanceB = rand.nextInt(100);
                            String repAfficheeB = "";
                            if (chanceB < 20) {
                                repBonusEvaluee = "PASSER";
                                repAfficheeB = "PASSER";
                            } else if (chanceB < 80) {
                                repBonusEvaluee = qBonus.getBonneReponse();
                                if (isQcmB) {
                                    for (Map.Entry<String, String> e : mapB.entrySet()) if (e.getValue().equals(repBonusEvaluee)) repAfficheeB = e.getKey();
                                } else {
                                    repAfficheeB = repBonusEvaluee;
                                }
                            } else {
                                repBonusEvaluee = "Je ne sais pas";
                                if (isQcmB) {
                                    List<String> fausses = new ArrayList<>(mapB.keySet());
                                    fausses.removeIf(k -> mapB.get(k).equals(qBonus.getBonneReponse()));
                                    repAfficheeB = fausses.isEmpty() ? "PASSER" : fausses.get(rand.nextInt(fausses.size()));
                                    repBonusEvaluee = mapB.getOrDefault(repAfficheeB, "PASSER");
                                } else {
                                    repAfficheeB = repBonusEvaluee;
                                }
                            }
                            System.out.println("🗣️ L'IA répond : " + repAfficheeB);
                            try { Thread.sleep(1200); } catch (Exception e) {}
                        }

                        if (!repBonusEvaluee.equals("TEMPS_ECOULE") && iaJugeService.evaluerReponse(qBonus.getBonneReponse(), qBonus.getSynonymesAcceptes(), repBonusEvaluee)) {
                            System.out.println("✅ Bonne réponse ! Vous avancez de " + ptsChoix + " cases et déclenchez l'effet : " + eChoix);
                            jMaj.setPositionPlateau(Math.min(taillePlateau, jMaj.getPositionPlateau() + ptsChoix));
                            jMaj.setEffetActif(eChoix);
                            jMaj.setDureeEffet(eChoix.equals("BOUCLIER") ? 2 : 1);
                            joueurRepository.save(jMaj);
                        } else {
                            System.out.println("❌ Mauvaise réponse mais vous gardez le bouclier. 💡 Réponse : " + qBonus.getBonneReponse());
                            break; 
                        }
                    } else {
                        List<PartieJoueur> tousValides = new ArrayList<>(tous);
                        tousValides.removeIf(p -> "BOUCLIER".equals(p.getEffetActif()) && !p.getId().equals(currentJoueurId));
                        List<PartieJoueur> advValides = new ArrayList<>(tousValides);
                        advValides.removeIf(p -> p.getId().equals(currentJoueurId));

                        if ((effetNom.equals("CIBLE_ECHANGE") && tousValides.size() < 2) || (!effetNom.equals("CIBLE_ECHANGE") && advValides.isEmpty())) {
                            System.out.println("\n⚠️ Impossible d'appliquer l'effet : tout le monde est immunisé !");
                            System.out.println("🎁 COMPENSATION : Vous avancez du double de votre dé initial (+"+de+" cases) !");
                            jMaj.setPositionPlateau(Math.min(taillePlateau, jMaj.getPositionPlateau() + de));
                            jMaj.setEffetActif("AUCUN"); joueurRepository.save(jMaj);
                        } else {
                            Long idC1 = null, idC2 = null;
                            if (effetNom.equals("CIBLE_ECHANGE")) {
                                System.out.println("\n⚠️ ACTION REQUISE : 🔄 ÉCHANGE DE PLACES !");
                                if (!jMaj.isEstIa()) {
                                    for (int i=0; i<tousValides.size(); i++) System.out.println("  "+i+") "+(tousValides.get(i).isEstIa() ? tousValides.get(i).getNomIa() : tousValides.get(i).getUtilisateur().getPseudo()) + " case " + tousValides.get(i).getPositionPlateau());
                                    List<String> validC1 = new ArrayList<>(); for(int i=0; i<tousValides.size(); i++) validC1.add(String.valueOf(i));
                                    String c1Str = inputAvecValidation("Numéro 1ère cible : ", validC1, "un chiffre valide", scanner);
                                    idC1 = tousValides.get(Integer.parseInt(c1Str)).getId();

                                    final Long fIdC1 = idC1;
                                    List<PartieJoueur> cRestantes = new ArrayList<>(tousValides); cRestantes.removeIf(p -> p.getId().equals(fIdC1));
                                    System.out.println("Cibles restantes :");
                                    for (int i=0; i<cRestantes.size(); i++) System.out.println("  "+i+") "+(cRestantes.get(i).isEstIa() ? cRestantes.get(i).getNomIa() : cRestantes.get(i).getUtilisateur().getPseudo()) + " case " + cRestantes.get(i).getPositionPlateau());
                                    
                                    List<String> validC2 = new ArrayList<>(); for(int i=0; i<cRestantes.size(); i++) validC2.add(String.valueOf(i));
                                    String c2Str = inputAvecValidation("Numéro 2ème cible : ", validC2, "un chiffre valide", scanner);
                                    idC2 = cRestantes.get(Integer.parseInt(c2Str)).getId();
                                } else {
                                    Collections.shuffle(tousValides); idC1 = tousValides.get(0).getId(); idC2 = tousValides.get(1).getId();
                                    System.out.println("🤖 L'IA a choisi ses cibles !");
                                }
                                moteurJeuService.appliquerEffetInteractif(currentJoueurId, idC1, idC2);
                            } else {
                                if (effetNom.equals("CIBLE_RECUL")) System.out.println("\n⚠️ ACTION REQUISE : 💥 FAIRE RECULER UN ADVERSAIRE !");
                                else System.out.println("\n⚠️ ACTION REQUISE : 🎯 APPLIQUER UN MALUS [" + effetNom + "] !");

                                if (!jMaj.isEstIa()) {
                                    for (int i=0; i<advValides.size(); i++) System.out.println("  "+i+") "+(advValides.get(i).isEstIa() ? advValides.get(i).getNomIa() : advValides.get(i).getUtilisateur().getPseudo()) + " case " + advValides.get(i).getPositionPlateau());
                                    List<String> validV = new ArrayList<>(); for(int i=0; i<advValides.size(); i++) validV.add(String.valueOf(i));
                                    String vStr = inputAvecValidation("Victime : ", validV, "un chiffre valide", scanner);
                                    idC1 = advValides.get(Integer.parseInt(vStr)).getId();
                                } else {
                                    idC1 = advValides.get(rand.nextInt(advValides.size())).getId();
                                    System.out.println("🤖 L'IA attaque !");
                                }
                                moteurJeuService.appliquerEffetInteractif(currentJoueurId, idC1, null);
                            }
                        }
                    }
                    jMaj = joueurRepository.findById(currentJoueurId).get();
                }

                if (res.victoire() || jMaj.getPositionPlateau() >= taillePlateau) {
                    System.out.println("\n🎉 VICTOIRE DE : " + nom + " !");
                    Partie p = partieRepository.findById(partieId).get(); p.setStatut("TERMINEE"); p.setVainqueur(jMaj.getUtilisateur());
                    partieRepository.save(p); enCours = false; break;
                }
                try { Thread.sleep(1500); } catch (Exception e) {}
            }
        }
    }
}