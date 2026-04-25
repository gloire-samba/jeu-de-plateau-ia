package com.iaspring.backspring.service;

import org.springframework.stereotype.Service;

@Service
public class IaJugeService {

    public boolean evaluerReponse(String reponseAttendue, String synonymes, String reponseJoueur) {
        if (reponseJoueur == null || reponseAttendue == null || reponseJoueur.isEmpty()) return false;

        // 1. On teste d'abord la réponse officielle stricte
        if (testerCorrespondance(reponseAttendue, reponseJoueur)) {
            return true;
        }

        // 2. Si c'est faux, on découpe et on teste la liste des synonymes
        if (synonymes != null && !synonymes.trim().isEmpty()) {
            String[] listeSynonymes = synonymes.split(",");
            for (String mot : listeSynonymes) {
                if (testerCorrespondance(mot.trim(), reponseJoueur)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean testerCorrespondance(String attendu, String test) {
        String cible = nettoyer(attendu);
        String essai = nettoyer(test);

        if (cible.equals(essai)) return true;

        int distance = calculerLevenshtein(cible, essai);

        // 👉 TOLÉRANCE PROPORTIONNELLE (Jusqu'à 5 fautes)
        int tolerance;
        int longueur = cible.length();
        
        if (longueur <= 5) tolerance = 1;       // ex: "Paris" (1 faute max)
        else if (longueur <= 10) tolerance = 2; // ex: "Tennis" (2 fautes max)
        else if (longueur <= 15) tolerance = 3; // ex: "Royaume Uni" (3 fautes max)
        else if (longueur <= 20) tolerance = 4; // ex: "Napoléon Bonaparte" (4 fautes)
        else tolerance = 5;                     // Très longues phrases (5 fautes max)

        return distance <= tolerance;
    }

    private String nettoyer(String input) {
        String clean = input.trim().toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ûüù]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[-']", " "); // Remplace les apostrophes par des espaces ("l'eau" devient "l eau")

        // 👉 SUPPRESSION DES ARTICLES : Si le mot commence par le, la, les, un, une... on l'ignore
        clean = clean.replaceAll("^(le |la |les |l |un |une |des )", "");
        
        return clean.trim();
    }

    private int calculerLevenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + cost, 
                               Math.min(dp[i - 1][j] + 1, 
                                        dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}