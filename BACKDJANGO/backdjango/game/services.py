import random
import time
from enum import Enum
from django.db import transaction
from .models import Partie, PartieJoueur, Utilisateur, CasePlateau
from game.models import Question, HistoriqueQuestion

class TypeEffet(Enum):
    AUCUN = "AUCUN"
    BOUCLIER = "BOUCLIER"
    SPRINT = "SPRINT"
    RISQUE_EXPLOSIF = "RISQUE_EXPLOSIF"
    DEUXIEME_CHANCE = "DEUXIEME_CHANCE"
    SUPER_BONUS = "SUPER_BONUS"
    INDICE = "INDICE"
    CIBLE_RECUL = "CIBLE_RECUL"
    CIBLE_PASSE_TOUR = "CIBLE_PASSE_TOUR"
    CIBLE_ECHANGE = "CIBLE_ECHANGE"
    CIBLE_PRESSION = "CIBLE_PRESSION"
    PARI_MULTIPLICATEUR = "PARI_MULTIPLICATEUR"

def determiner_effet_case_fixe(position):
    match position:
        case 5 | 15 | 35: return TypeEffet.BOUCLIER
        case 10 | 40: return TypeEffet.SUPER_BONUS
        case 25: return TypeEffet.INDICE
        case 8 | 22 | 45: return TypeEffet.DEUXIEME_CHANCE
        case 12 | 32: return TypeEffet.SPRINT
        case 7 | 27 | 47: return TypeEffet.CIBLE_RECUL
        case 18 | 38: return TypeEffet.CIBLE_PASSE_TOUR
        case 14 | 34: return TypeEffet.CIBLE_ECHANGE
        case 20 | 42: return TypeEffet.CIBLE_PRESSION
        case 13 | 26 | 39: return TypeEffet.RISQUE_EXPLOSIF
        case 17 | 37: return TypeEffet.PARI_MULTIPLICATEUR
        case _: return TypeEffet.AUCUN

class PartieService:
    @staticmethod
    def creer_partie_solo(utilisateur_id, nb_ia=3):
        utilisateur = Utilisateur.objects.get(id=utilisateur_id)
        
        partie = Partie.objects.create(
            statut="EN_COURS",
            createur=utilisateur,
            tour_actuel=1
        )
        
        PartieJoueur.objects.create(
            partie=partie,
            utilisateur=utilisateur,
            est_ia=False,
            position_plateau=0,
            ordre_tour=1
        )
        
        for i in range(1, nb_ia + 1):
            PartieJoueur.objects.create(
                partie=partie,
                est_ia=True,
                nom_ia=f"Bot Alpha {i}",
                position_plateau=0,
                ordre_tour=i + 1
            )
        return partie

class MoteurJeuService:
    @staticmethod
    def get_effet_for_position(pos, plateau_dict=None):
        if plateau_dict and str(pos) in plateau_dict:
            nom_effet = plateau_dict[str(pos)].get('effet', 'AUCUN')
            try:
                return TypeEffet[nom_effet]
            except KeyError:
                return TypeEffet.AUCUN
        return determiner_effet_case_fixe(pos)

    @staticmethod
    @transaction.atomic
    def appliquer_effet_interactif(lanceur_id, cible1_id=None, cible2_id=None):
        lanceur = PartieJoueur.objects.get(id=lanceur_id)
        effet = lanceur.effet_actif

        if effet == "CIBLE_ECHANGE" and cible1_id and cible2_id:
            c1 = PartieJoueur.objects.get(id=cible1_id)
            c2 = PartieJoueur.objects.get(id=cible2_id)
            pos1 = c1.position_plateau
            c1.position_plateau = c2.position_plateau
            c2.position_plateau = pos1
            c1.save()
            c2.save()

        elif effet and effet.startswith("CIBLE_") and cible1_id:
            cible = PartieJoueur.objects.get(id=cible1_id)
            
            if effet == "CIBLE_RECUL":
                cible.position_plateau = max(cible.position_plateau - 3, 0)
            elif effet == "CIBLE_PASSE_TOUR":
                cible.effet_actif = "MALUS_PASSE_TOUR"
                cible.duree_effet = 1
            elif effet == "CIBLE_PRESSION":
                cible.effet_actif = "MALUS_PRESSION"
                cible.duree_effet = 1
                lanceur.effet_actif = "BONUS_PRESSION"
                lanceur.duree_effet = 1
            cible.save()

        if lanceur.effet_actif != "BONUS_PRESSION":
            lanceur.effet_actif = "AUCUN"
        lanceur.save()

    @staticmethod
    @transaction.atomic
    def traiter_reponse(partie_id, joueur_id, est_bonne_reponse, valeur_de, plateau_dict=None, taille_plateau=50):
        joueur_actuel = PartieJoueur.objects.get(id=joueur_id)
        tous_les_joueurs = PartieJoueur.objects.filter(partie_id=partie_id)
        
        effet_depart = MoteurJeuService.get_effet_for_position(joueur_actuel.position_plateau, plateau_dict)
        
        if joueur_actuel.effet_actif == "PARI_MULTIPLICATEUR":
            effet_depart = TypeEffet.PARI_MULTIPLICATEUR
            joueur_actuel.effet_actif = "AUCUN"

        effet_en_attente = None
        a_droit_deuxieme_chance = False
        message_effet = ""

        case_actuelle = plateau_dict.get(str(joueur_actuel.position_plateau)) if plateau_dict else None
        pts_case = case_actuelle.get('points', 1) if case_actuelle and case_actuelle.get('points', 0) > 0 else 1

        if not est_bonne_reponse:
            if effet_depart == TypeEffet.DEUXIEME_CHANCE:
                a_droit_deuxieme_chance = True
                message_effet = "✨ Sauvé par la 2ème chance !"
            elif effet_depart == TypeEffet.RISQUE_EXPLOSIF:
                joueur_actuel.position_plateau = max(joueur_actuel.position_plateau - 3, 0)
                message_effet = "💣 Boum ! Mauvaise réponse, recule de 3 cases."
            elif effet_depart == TypeEffet.PARI_MULTIPLICATEUR:
                recul = valeur_de * pts_case
                joueur_actuel.position_plateau = max(joueur_actuel.position_plateau - recul, 0)
                message_effet = f"📉 Pari perdu ! Vous reculez de {recul} cases."
        else:
            deplacement_final = valeur_de
            
            if effet_depart == TypeEffet.PARI_MULTIPLICATEUR:
                deplacement_final = valeur_de * pts_case
                message_effet = f"📈 Pari gagné ! Vous avancez de {deplacement_final} cases."
            
            has_sprint_bonus = joueur_actuel.effet_actif == "SPRINT"
            if effet_depart == TypeEffet.SPRINT or has_sprint_bonus:
                deplacement_final *= 2
                message_effet += (" " if message_effet else "") + "⚡ SPRINT ! Déplacement doublé."
                if has_sprint_bonus:
                    joueur_actuel.effet_actif = "AUCUN"
            
            joueur_actuel.position_plateau = min(joueur_actuel.position_plateau + deplacement_final, taille_plateau)

            if joueur_actuel.position_plateau >= taille_plateau:
                joueur_actuel.save()
                return {"victoire": True, "effet_en_attente": None, "deuxieme_chance": False, "messageEffet": "🏆 Victoire !"}

            if effet_depart == TypeEffet.RISQUE_EXPLOSIF:
                recul_zone = valeur_de * 2
                message_effet = f"💣 EXPLOSION ! Les autres joueurs reculent de {recul_zone} cases."
                for j in tous_les_joueurs:
                    if j.id != joueur_actuel.id and j.effet_actif != "BOUCLIER":
                        j.position_plateau = max(j.position_plateau - recul_zone, 0)
                        j.save()

            effet_arrivee = MoteurJeuService.get_effet_for_position(joueur_actuel.position_plateau, plateau_dict)

            if effet_arrivee == TypeEffet.BOUCLIER:
                joueur_actuel.effet_actif = "BOUCLIER"
                joueur_actuel.duree_effet = 2
                message_effet += (" | " if message_effet else "") + "🛡️ Bouclier activé !"
            elif effet_arrivee == TypeEffet.INDICE:
                joueur_actuel.effet_actif = "INDICE"
                joueur_actuel.duree_effet = 2
                message_effet += (" | " if message_effet else "") + "💡 Indice gagné !"
            elif effet_arrivee == TypeEffet.SUPER_BONUS:
                effet_en_attente = effet_arrivee.name
                joueur_actuel.effet_actif = effet_arrivee.name
                message_effet += (" | " if message_effet else "") + "🌟 Super Bonus atteint !"
            elif effet_arrivee in [TypeEffet.CIBLE_ECHANGE, TypeEffet.CIBLE_RECUL, TypeEffet.CIBLE_PASSE_TOUR, TypeEffet.CIBLE_PRESSION]:
                effet_en_attente = effet_arrivee.name
                joueur_actuel.effet_actif = effet_arrivee.name
                message_effet += (" | " if message_effet else "") + "🎯 Préparez votre attaque !"

        if not a_droit_deuxieme_chance and not effet_en_attente:
            if joueur_actuel.duree_effet > 0:
                joueur_actuel.duree_effet -= 1
                if joueur_actuel.duree_effet == 0 and joueur_actuel.effet_actif != "BONUS_PRESSION":
                    joueur_actuel.effet_actif = "AUCUN"

        joueur_actuel.save()
        return {
            "victoire": False,
            "effet_en_attente": effet_en_attente,
            "deuxieme_chance": a_droit_deuxieme_chance,
            "messageEffet": message_effet
        }

class HistoriqueQuestionService:
    @staticmethod
    def tirer_question_pour_joueur(joueur):
        historiques = HistoriqueQuestion.objects.filter(utilisateur=joueur.utilisateur) if not joueur.est_ia else HistoriqueQuestion.objects.filter(partie_joueur=joueur)
        ids_deja_poses = historiques.values_list('question_id', flat=True)
        
        categories = ["HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"]
        categorie_aleatoire = random.choice(categories)
        
        questions_inedites = Question.objects.exclude(id__in=ids_deja_poses)

        if not questions_inedites.exists():
            historiques.delete()
            questions_inedites = Question.objects.all()

        question_tiree = random.choice(list(questions_inedites))
        
        nouvel_historique = HistoriqueQuestion(question=question_tiree, categorie=categorie_aleatoire)
        if not joueur.est_ia and joueur.utilisateur:
            nouvel_historique.utilisateur = joueur.utilisateur
        else:
            nouvel_historique.partie_joueur = joueur
        nouvel_historique.save()
        
        return question_tiree

    @staticmethod
    def tirer_question_par_categorie(joueur, categorie):
        historiques = HistoriqueQuestion.objects.filter(utilisateur=joueur.utilisateur) if not joueur.est_ia else HistoriqueQuestion.objects.filter(partie_joueur=joueur)
        ids_deja_poses = historiques.values_list('question_id', flat=True)
        
        questions_de_la_categorie = Question.objects.filter(categorie__iexact=categorie)
        if not questions_de_la_categorie.exists():
            return HistoriqueQuestionService.tirer_question_pour_joueur(joueur)

        questions_inedites = questions_de_la_categorie.exclude(id__in=ids_deja_poses)

        if not questions_inedites.exists():
            historiques.delete() 
            questions_inedites = questions_de_la_categorie

        question_tiree = random.choice(list(questions_inedites))
        nouvel_historique = HistoriqueQuestion(question=question_tiree, categorie=categorie)
        
        if not joueur.est_ia and joueur.utilisateur: nouvel_historique.utilisateur = joueur.utilisateur
        else: nouvel_historique.partie_joueur = joueur
        nouvel_historique.save()
        return question_tiree
    
class PlateauService:
    @staticmethod
    def sauvegarder_plateau_complet(partie_id, plateau_dict):
        partie = Partie.objects.get(id=partie_id)
        CasePlateau.objects.filter(partie=partie).delete()
        
        cases = []
        for pos, data in plateau_dict.items():
            cases.append(CasePlateau(
                partie=partie,
                position_plateau=pos,
                effet=data.get('effet', 'AUCUN'),
                categorie=data.get('categorie'),
                points=data.get('points', 0)
            ))
        CasePlateau.objects.bulk_create(cases)

    @staticmethod
    def charger_plateau_pour_moteur(partie_id):
        cases = CasePlateau.objects.filter(partie_id=partie_id)
        return {str(c.position_plateau): {
            'effet': c.effet, 
            'categorie': c.categorie, 
            'points': c.points
        } for c in cases}