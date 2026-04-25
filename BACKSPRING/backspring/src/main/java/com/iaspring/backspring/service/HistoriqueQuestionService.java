package com.iaspring.backspring.service;

import com.iaspring.backspring.entity.HistoriqueQuestion;
import com.iaspring.backspring.entity.PartieJoueur;
import com.iaspring.backspring.entity.Question;
import com.iaspring.backspring.repository.HistoriqueQuestionRepository;
import com.iaspring.backspring.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriqueQuestionService {

    private final HistoriqueQuestionRepository historiqueRepository;
    private final QuestionRepository questionRepository;
    private final Random random = new Random();

    // --- PARTIE CRUD CLASSIQUE ---
    public HistoriqueQuestion creer(HistoriqueQuestion historique) {
        return historiqueRepository.save(historique);
    }

    public List<HistoriqueQuestion> recupererTout() {
        return historiqueRepository.findAll();
    }

    public void supprimer(Long id) {
        historiqueRepository.deleteById(id);
    }

    // --- PARTIE LOGIQUE MÉTIER (Tirage Inédit) ---
    
    public Question tirerQuestionPourJoueur(PartieJoueur joueur) {
        // 1. On choisit d'abord une catégorie au hasard
        List<String> categories = questionRepository.findAll().stream()
                .map(Question::getCategorie)
                .distinct()
                .collect(Collectors.toList());
        String categorieChoisie = categories.get(random.nextInt(categories.size()));

        return tirerQuestionParCategorie(joueur, categorieChoisie);
    }

    public Question tirerQuestionParCategorie(PartieJoueur joueur, String categorie) {
        List<Question> questionsDeLaCategorie = questionRepository.findByCategorie(categorie);
        
        // 2. On récupère l'historique (Humain ou IA)
        List<HistoriqueQuestion> historique;
        if (!joueur.isEstIa() && joueur.getUtilisateur() != null) {
            historique = historiqueRepository.findByUtilisateur(joueur.getUtilisateur());
        } else {
            historique = historiqueRepository.findByPartieJoueur(joueur);
        }

        // 3. On filtre pour trouver les questions que le joueur n'a pas encore eues
        List<Long> idsDejaPoses = historique.stream()
                .filter(h -> h.getCategorie().equals(categorie))
                .map(h -> h.getQuestion().getId())
                .collect(Collectors.toList());

        List<Question> questionsInedites = questionsDeLaCategorie.stream()
                .filter(q -> !idsDejaPoses.contains(q.getId()))
                .collect(Collectors.toList());

        // 4. LA RÈGLE D'OR : Si la catégorie est épuisée, on efface la mémoire de cette catégorie
        if (questionsInedites.isEmpty()) {
            if (!joueur.isEstIa() && joueur.getUtilisateur() != null) {
                historiqueRepository.deleteByUtilisateurAndCategorie(joueur.getUtilisateur(), categorie);
            } else {
                historiqueRepository.deleteByPartieJoueurAndCategorie(joueur, categorie);
            }
            questionsInedites = questionsDeLaCategorie; // On remet le paquet à neuf
        }

        // 5. Tirage au sort parmi les inédites
        Question questionTiree = questionsInedites.get(random.nextInt(questionsInedites.size()));

        // 6. On enregistre cette question dans l'historique pour le futur
        HistoriqueQuestion nouvelHistorique = HistoriqueQuestion.builder()
                .question(questionTiree)
                .categorie(categorie)
                .build();

        if (!joueur.isEstIa() && joueur.getUtilisateur() != null) {
            nouvelHistorique.setUtilisateur(joueur.getUtilisateur());
        } else {
            nouvelHistorique.setPartieJoueur(joueur);
        }
        
        historiqueRepository.save(nouvelHistorique);

        return questionTiree;
    }
}