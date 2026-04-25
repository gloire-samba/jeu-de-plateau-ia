from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, viewsets
from rest_framework.permissions import AllowAny
from .security import JwtService, IsAdminRole
import random

from .serializers import (
    NouvellePartieSoloSerializer, PartieResponseSerializer, PartieEtatCompletSerializer, CasePlateauSerializer, 
    UtilisateurCrudSerializer, QuestionCrudSerializer, PartieCrudSerializer, PartieJoueurCrudSerializer, HistoriqueQuestionSerializer
)
from .services import PartieService, MoteurJeuService, HistoriqueQuestionService, PlateauService, TypeEffet
from .models import PartieJoueur, Partie, Utilisateur, Question, HistoriqueQuestion, CasePlateau
from .ia_juge_service import IaJugeService

# ==========================================
# ⚙️ VUES CRUD ADMIN / STANDARD
# ==========================================

class UtilisateurViewSet(viewsets.ModelViewSet):
    queryset = Utilisateur.objects.all().order_by('id')
    serializer_class = UtilisateurCrudSerializer
    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAdminRole()]

class QuestionViewSet(viewsets.ModelViewSet):
    queryset = Question.objects.all().order_by('id')
    serializer_class = QuestionCrudSerializer
    def get_permissions(self):
        return [IsAdminRole()]
    def get_queryset(self):
        queryset = super().get_queryset()
        categorie = self.request.query_params.get('categorie')
        if categorie:
            queryset = queryset.filter(categorie__iexact=categorie)
        return queryset

class PartieViewSet(viewsets.ModelViewSet):
    queryset = Partie.objects.all().order_by('-date_creation')
    serializer_class = PartieCrudSerializer
    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAdminRole()]

class PartieJoueurViewSet(viewsets.ModelViewSet):
    queryset = PartieJoueur.objects.all().order_by('id')
    serializer_class = PartieJoueurCrudSerializer
    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAdminRole()]
    
class HistoriqueQuestionViewSet(viewsets.ModelViewSet):
    queryset = HistoriqueQuestion.objects.all().order_by('id')
    serializer_class = HistoriqueQuestionSerializer
    def get_permissions(self):
        return [IsAdminRole()]

class CasePlateauViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = CasePlateauSerializer
    def get_permissions(self):
        if self.request.method == 'GET':
            return [AllowAny()]
        return [IsAdminRole()]
    def get_queryset(self):
        partie_id = self.request.query_params.get('partie_id')
        if partie_id:
            return CasePlateau.objects.filter(partie_id=partie_id).order_by('position_plateau')
        return CasePlateau.objects.all().order_by('position_plateau')


# ==========================================
# 🧩 FONCTIONS UTILITAIRES & BOTS
# ==========================================

def generer_indice(reponse):
    if not reponse:
        return ""
    sb = []
    longueur = len(reponse)
    for i, c in enumerate(reponse):
        if c == ' ':
            sb.append("   ") 
        elif i < 2 or i == longueur - 1:
            sb.append(c + " ")
        else:
            sb.append("_ ")
    return "".join(sb).strip()

def enregistrer_dans_historique(partie_id, message):
    try:
        partie = Partie.objects.get(id=partie_id)
        ancien_histo = partie.historique if partie.historique else ""
        partie.historique = ancien_histo + message + "\n"
        partie.save()
    except Partie.DoesNotExist:
        pass

def faire_jouer_bots(partie_id, taille_plateau, plateau_actuel):
    logs_ia = []
    tous = PartieJoueur.objects.filter(partie_id=partie_id).order_by('ordre_tour')
    
    logs_ia.append("--------------------------------------------------")
    logs_ia.append("📊 RÉSUMÉ DU PLATEAU :")
    for pj in tous:
        nom = pj.nom_ia if pj.est_ia else pj.utilisateur.pseudo
        eff = f" ({pj.effet_actif})" if pj.effet_actif and pj.effet_actif != 'AUCUN' else ""
        logs_ia.append(f"   - {nom} : Case {pj.position_plateau}{eff}")

    ias = [p for p in tous if p.est_ia]
    
    for ia in ias:
        if getattr(ia, 'effet_actif', '') == "MALUS_PASSE_TOUR":
            logs_ia.append("--------------------------------------------------")
            logs_ia.append(f"🤖 TOUR DE : {ia.nom_ia}")
            logs_ia.append("🛑 Passe son tour à cause du Malus !")
            ia.effet_actif = "AUCUN"
            ia.save()
            continue

        pos_avant = ia.position_plateau
        
        q = HistoriqueQuestionService.tirer_question_pour_joueur(ia)
        q_texte = q.texte_question if q else "Question par défaut"
        q_bonne = q.bonne_reponse if q else ""

        prob = random.randint(0, 99)
        ia_reussit = False
        statut = getattr(ia, 'effet_actif', 'NORMAL')
        if not statut or statut == "AUCUN":
            statut = "NORMAL"

        if statut == "INDICE":
            if 5 <= prob < 95: ia_reussit = True
        elif statut == "MALUS_PRESSION":
            if prob >= 80: ia_reussit = (random.randint(0, 99) < 40)
        else:
            if 20 <= prob < 80: ia_reussit = True

        effet_depart_ia = MoteurJeuService.get_effet_for_position(ia.position_plateau, plateau_actuel)
        de_ia = 0
        if effet_depart_ia == TypeEffet.PARI_MULTIPLICATEUR or getattr(ia, 'effet_actif', '') == "PARI_MULTIPLICATEUR":
            de_ia = random.randint(1, 6)
        elif ia_reussit:
            de_ia = random.randint(1, 6)
        
        resultat_ia = MoteurJeuService.traiter_reponse(
            partie_id, ia.id, ia_reussit, de_ia, plateau_actuel, taille_plateau
        )
        
        action_supp = ""
        effet_attente_ia = resultat_ia.get("effet_en_attente")
        
        if effet_attente_ia:
            if effet_attente_ia.startswith("CIBLE_"):
                cibles_normales = []
                cibles_echange = []
                
                tous_pour_ia = PartieJoueur.objects.filter(partie_id=partie_id).order_by('ordre_tour')
                for p in tous_pour_ia:
                    if getattr(p, 'effet_actif', '') != "BOUCLIER": 
                        cibles_echange.append(p)
                        if p.id != ia.id:
                            cibles_normales.append(p)
                            
                if effet_attente_ia == "CIBLE_ECHANGE" and len(cibles_echange) >= 2:
                    cibles = random.sample(cibles_echange, 2)
                    MoteurJeuService.appliquer_effet_interactif(ia.id, cibles[0].id, cibles[1].id)
                    nom1 = cibles[0].nom_ia if cibles[0].est_ia else "Vous"
                    nom2 = cibles[1].nom_ia if cibles[1].est_ia else "Vous"
                    action_supp = f" 🔄 Échange {nom1} avec {nom2}"
                elif effet_attente_ia != "CIBLE_ECHANGE" and cibles_normales:
                    cible_choisie = random.choice(cibles_normales)
                    MoteurJeuService.appliquer_effet_interactif(ia.id, cible_choisie.id, None)
                    nom_cible = cible_choisie.nom_ia if cible_choisie.est_ia else "Vous"
                    action_supp = f" 🎯 Cible {nom_cible} avec {effet_attente_ia}"
                else:
                    ia_comp = PartieJoueur.objects.get(id=ia.id)
                    ia_comp.position_plateau = min(taille_plateau, ia_comp.position_plateau + de_ia)
                    ia_comp.effet_actif = "AUCUN"
                    ia_comp.save()
                    action_supp = f" 🎁 +{de_ia} cases bonus (Tous immunisés)"
            
            # 👉 LE BOT JOUE LE SUPER BONUS SOUS DJANGO SANS RESTRICTION
            elif effet_attente_ia == "SUPER_BONUS":
                reussite_sb = (random.randint(0, 99) >= 30) # 70% de chance
                de_sb = random.randint(1, 6) # Pari de 1 à 6
                
                effets_dispos = ["AUCUN", "SPRINT", "CIBLE_RECUL", "CIBLE_PASSE_TOUR", "CIBLE_PRESSION", "CIBLE_ECHANGE"]
                effet_choisi = random.choice(effets_dispos)
                
                ia_sb = PartieJoueur.objects.get(id=ia.id)
                if reussite_sb:
                    ia_sb.position_plateau = min(taille_plateau, ia_sb.position_plateau + de_sb)
                    
                    if effet_choisi.startswith("CIBLE_"):
                        cibles_normales = []
                        cibles_echange = []
                        tous_pour_sb = PartieJoueur.objects.filter(partie_id=partie_id).order_by('ordre_tour')
                        for p in tous_pour_sb:
                            if getattr(p, 'effet_actif', '') != "BOUCLIER":
                                cibles_echange.append(p)
                                if p.id != ia_sb.id:
                                    cibles_normales.append(p)
                                    
                        if effet_choisi == "CIBLE_ECHANGE" and len(cibles_echange) >= 2:
                            cibles = random.sample(cibles_echange, 2)
                            MoteurJeuService.appliquer_effet_interactif(ia_sb.id, cibles[0].id, cibles[1].id)
                            nom1 = cibles[0].nom_ia if cibles[0].est_ia else "Vous"
                            nom2 = cibles[1].nom_ia if cibles[1].est_ia else "Vous"
                            action_supp = f" 🌟 SUPER BONUS ! Avance de {de_sb} et 🔄 Échange {nom1} avec {nom2}"
                            ia_sb.effet_actif = "AUCUN"
                        elif effet_choisi != "CIBLE_ECHANGE" and cibles_normales:
                            cible_choisie = random.choice(cibles_normales)
                            MoteurJeuService.appliquer_effet_interactif(ia_sb.id, cible_choisie.id, None)
                            nom_cible = cible_choisie.nom_ia if cible_choisie.est_ia else "Vous"
                            action_supp = f" 🌟 SUPER BONUS ! Avance de {de_sb} et 🎯 Cible {nom_cible} avec {effet_choisi}"
                            ia_sb.effet_actif = "AUCUN"
                        else:
                            ia_sb.effet_actif = "BOUCLIER"
                            ia_sb.duree_effet = 2
                            action_supp = f" 🌟 SUPER BONUS ! Avance de {de_sb} (Pas de cible dispo, gagne un Bouclier)"
                    else:
                        ia_sb.effet_actif = effet_choisi
                        ia_sb.duree_effet = 2 if effet_choisi == "BOUCLIER" else 1
                        action_supp = f" 🌟 SUPER BONUS ! Avance de {de_sb} et gagne l'effet {effet_choisi}"
                else:
                    ia_sb.effet_actif = "BOUCLIER"
                    ia_sb.duree_effet = 2
                    action_supp = " 🌟 SUPER BONUS Raté ! Gagne un bouclier."
                ia_sb.save()

        ia.refresh_from_db()
        pos_apres = ia.position_plateau
        
        logs_ia.append("--------------------------------------------------")
        logs_ia.append(f"🤖 TOUR DE : {ia.nom_ia}")
        logs_ia.append(f"❓ Q : {q_texte}")
        
        if effet_depart_ia == TypeEffet.PARI_MULTIPLICATEUR or getattr(ia, 'effet_actif', '') == "PARI_MULTIPLICATEUR":
            logs_ia.append(f"💬 R : Pari de {de_ia} -> {'✅ Gagné' if ia_reussit else f'❌ Perdu ({q_bonne})'}")
        else:
            logs_ia.append(f"💬 R : {'✅ Juste' if ia_reussit else f'❌ Faux ({q_bonne})'}")

        log_mouv = f"🎲 Dé: {de_ia} | 📍 Case {pos_avant} ➡️ {pos_apres}"
        msg_effet = resultat_ia.get("messageEffet")
        if msg_effet:
            log_mouv += f" | ✨ {msg_effet}"
        if action_supp:
            log_mouv += f" | {action_supp}"
            
        logs_ia.append(log_mouv)

    partie_terminee = False
    nom_vainqueur = ""
    tous_les_joueurs_fin = PartieJoueur.objects.filter(partie_id=partie_id).order_by('ordre_tour')
    for p in tous_les_joueurs_fin:
        if p.position_plateau >= taille_plateau:
            partie_terminee = True
            nom_vainqueur = p.nom_ia if p.est_ia else "Vous"
            partie_db = Partie.objects.get(id=partie_id)
            if partie_db.statut != "TERMINEE":
                partie_db.statut = "TERMINEE"
                if not p.est_ia:
                    partie_db.vainqueur = p.utilisateur
                partie_db.save()
            break

    return logs_ia, partie_terminee, nom_vainqueur

# ==========================================
# 🎮 VUES SPÉCIFIQUES JEU
# ==========================================
    
class JeuAPIView(APIView):
    
    def generer_plateau_aleatoire(self, nb_cases):
        plateau = {}
        effets = [e.name for e in TypeEffet if e.name != "AUCUN"]
        categories = ["HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"]
        
        nb_effets = nb_cases // 2
        pos_effets = random.sample(range(1, nb_cases + 1), min(nb_effets, nb_cases))
        
        for i in range(1, nb_cases + 1):
            cat = random.choice(categories)
            pts = random.randint(1, 6)
            if i in pos_effets:
                plateau[str(i)] = {"effet": random.choice(effets), "categorie": cat, "points": pts}
            else:
                plateau[str(i)] = {"effet": "AUCUN", "categorie": cat, "points": pts}
        return plateau

    def post(self, request):
        data = request.data
        user_id = data.get('utilisateurId')
        nb_bots = data.get('nbBots', 1)
        type_plateau = data.get('typePlateau', 0)
        taille = data.get('taillePlateau', 50)

        partie = PartieService.creer_partie_solo(user_id, nb_bots)
        taille_finale = taille + 1 if taille > 0 else 51

        if type_plateau in [0, 1]:
            taille_calc = taille if taille > 0 else 50
            plateau_aleatoire = self.generer_plateau_aleatoire(taille_calc)
            PlateauService.sauvegarder_plateau_complet(partie.id, plateau_aleatoire)
        elif type_plateau == 2 and data.get('plateauCustom'):
            PlateauService.sauvegarder_plateau_complet(partie.id, data.get('plateauCustom'))

        joueur_humain = PartieJoueur.objects.filter(partie=partie, est_ia=False).first()

        enregistrer_dans_historique(partie.id, "🎮 DÉBUT DE LA PARTIE\n")

        return Response({
            "partieId": partie.id,
            "joueurId": joueur_humain.id if joueur_humain else None, 
            "taillePlateau": taille_finale,
            "message": "Partie prête !"
        }, status=status.HTTP_201_CREATED)
        
        
    def get(self, request, partie_id, joueur_id):
        try:
            joueur = PartieJoueur.objects.get(id=joueur_id)
            q = HistoriqueQuestionService.tirer_question_pour_joueur(joueur)

            if not q:
                return Response({"error": "Pas de question"}, status=404)

            indice_texte = None
            if getattr(joueur, 'effet_actif', None) == "INDICE":
                indice_texte = generer_indice(str(q.bonne_reponse))

            mauvaises_props = [
                getattr(q, 'mauvaise_prop_1', None),
                getattr(q, 'mauvaise_prop_2', None),
                getattr(q, 'mauvaise_prop_3', None)
            ]
            mauvaises_valides = [p for p in mauvaises_props if p and str(p).strip()]

            type_final = str(q.type_question).upper() if q.type_question else "TEXTE"

            if len(mauvaises_valides) > 0 or "QCM" in type_final or "FAUX" in type_final:
                propositions = [str(q.bonne_reponse)] + [str(p) for p in mauvaises_valides]
                random.shuffle(propositions)
                type_final = "QCM" 
            else:
                propositions = []
                type_final = "TEXTE"

            return Response({
                "questionId": q.id,
                "texteQuestion": q.texte_question,
                "typeQuestion": type_final,
                "propositions": propositions,
                "indiceTexte": indice_texte 
            })

        except Exception as e:
            return Response({"error": str(e)}, status=500)        


class RepondreAPIView(APIView):
    def post(self, request):
        partie_id = request.data.get('partieId')
        joueur_id = request.data.get('joueurId')
        question_id = request.data.get('questionId')
        reponse_joueur = request.data.get('reponseJoueur')

        try:
            joueur = PartieJoueur.objects.get(id=joueur_id)
            
            if joueur.effet_actif == "MALUS_PASSE_TOUR":
                joueur.effet_actif = "AUCUN"
                joueur.save()
                
                plateau_actuel = PlateauService.charger_plateau_pour_moteur(partie_id)
                taille_plateau = max([int(k) for k in plateau_actuel.keys()]) + 1 if plateau_actuel else 50
                logs_ia, pt, nv = faire_jouer_bots(partie_id, taille_plateau, plateau_actuel)
                
                log_tour = ["--------------------------------------------------", "👤 TOUR DE : VOUS", "🛑 Vous avez passé votre tour à cause du Malus."]
                if logs_ia: log_tour.extend(logs_ia)
                enregistrer_dans_historique(partie_id, "\n".join(log_tour) + "\n")
                
                # 👉 INCRÉMENTATION DU TOUR
                partie_db = Partie.objects.get(id=partie_id)
                if partie_db.statut != "TERMINEE":
                    partie_db.tour_actuel += 1
                    partie_db.save()

                return Response({
                    "etaitBonneReponse": False,
                    "valeurDe": 0,
                    "nouvellePosition": joueur.position_plateau,
                    "positionAvantBots": joueur.position_plateau,
                    "resultatTour": {
                        "victoire": False, 
                        "effetEnAttente": None,
                        "messageEffet": "🛑 Tour passé (Malus).",
                        "aDroitDeuxiemeChance": False 
                    },
                    "logsIA": logs_ia,
                    "partieTerminee": pt,
                    "nomVainqueur": nv,
                    "bonneReponse": ""
                }, status=status.HTTP_200_OK)

            question = Question.objects.get(id=question_id)
            
            brute = str(reponse_joueur).strip() if reponse_joueur else ""
            valeur_de_pari = 1
            
            if "_PARI_" in brute:
                parts = brute.split("_PARI_")
                try:
                    valeur_de_pari = int(parts[0])
                except Exception:
                    valeur_de_pari = 1
                brute = parts[1] if len(parts) > 1 else ""

            bonne = str(question.bonne_reponse).strip() if question.bonne_reponse else ""
            syns = str(question.synonymes_acceptes).strip() if getattr(question, 'synonymes_acceptes', None) else ""
            
            if brute.upper() == "PASSER" or not brute or brute == "PASSER_MALUS":
                est_correct = False
            else:
                est_correct = IaJugeService.evaluer_reponse(bonne, syns, brute)
            
            valeur_de = 0
            plateau_actuel = PlateauService.charger_plateau_pour_moteur(partie_id)
            taille_plateau = max([int(k) for k in plateau_actuel.keys()]) + 1 if plateau_actuel else 50
            
            effet_depart_actuel = MoteurJeuService.get_effet_for_position(joueur.position_plateau, plateau_actuel)
            
            if effet_depart_actuel == TypeEffet.PARI_MULTIPLICATEUR or joueur.effet_actif == "PARI_MULTIPLICATEUR":
                valeur_de = valeur_de_pari 
            elif est_correct:
                case_actuelle = plateau_actuel.get(str(joueur.position_plateau)) or plateau_actuel.get(joueur.position_plateau)
                if case_actuelle and case_actuelle.get('points', 0) > 0:
                    valeur_de = case_actuelle['points'] 
                else:
                    valeur_de = random.randint(1, 6)
                
            resultat_humain = MoteurJeuService.traiter_reponse(
                partie_id, joueur.id, est_correct, valeur_de, plateau_actuel, taille_plateau
            )
            
            doit_attendre_cible = False
            effet_attente = resultat_humain.get("effet_en_attente")
            
            if effet_attente:
                if effet_attente.startswith("CIBLE_"):
                    tous_j = PartieJoueur.objects.filter(partie_id=partie_id)
                    can_target = False
                    if effet_attente == "CIBLE_ECHANGE":
                        dispo = sum(1 for p in tous_j if p.effet_actif != "BOUCLIER" or p.id == joueur_id)
                        can_target = dispo >= 2
                    else:
                        dispo = sum(1 for p in tous_j if p.id != joueur_id and p.effet_actif != "BOUCLIER")
                        can_target = dispo >= 1
                    
                    if not can_target:
                        j_comp = PartieJoueur.objects.get(id=joueur_id)
                        j_comp.position_plateau = min(taille_plateau, j_comp.position_plateau + valeur_de)
                        j_comp.effet_actif = "AUCUN"
                        j_comp.save()
                        msg = resultat_humain.get("messageEffet", "") + f" Mais vu qu'il n'y a pas de cible disponible, vous avancez de 2 fois le chiffre ! (+{valeur_de} cases bonus)"
                        resultat_humain["effet_en_attente"] = None
                        resultat_humain["messageEffet"] = msg
                        resultat_humain["victoire"] = j_comp.position_plateau >= taille_plateau
                    else:
                        doit_attendre_cible = True 
                elif effet_attente == "SUPER_BONUS":
                    doit_attendre_cible = True 

            joueur.refresh_from_db()
            pos_avant_bots = joueur.position_plateau

            logs_ia = []
            a_droit_deuxieme_chance = resultat_humain.get("deuxieme_chance", False)
            victoire_humain = resultat_humain.get("victoire", False)
            partie_terminee = victoire_humain
            nom_vainqueur = "Vous" if victoire_humain else ""
            
            # 👉 CORRECTION 3 : GESTION DB DU TOUR ET DE LA VICTOIRE
            partie_db = Partie.objects.get(id=partie_id)
            if victoire_humain:
                partie_db.statut = "TERMINEE"
                partie_db.vainqueur = joueur.utilisateur
            elif not doit_attendre_cible and not a_droit_deuxieme_chance:
                partie_db.tour_actuel += 1
            partie_db.save()
            
            if not a_droit_deuxieme_chance and not victoire_humain and not doit_attendre_cible:
                logs_ia, pt, nv = faire_jouer_bots(partie_id, taille_plateau, plateau_actuel)
                if pt:
                    partie_terminee = True
                    nom_vainqueur = nv
            elif a_droit_deuxieme_chance:
                logs_ia.append("--------------------------------------------------")
                logs_ia.append("✨ Deuxième chance activée ! Les bots attendent votre nouvelle tentative.")

            joueur_post_bots = PartieJoueur.objects.get(id=joueur_id)

            log_tour = []
            log_tour.append("--------------------------------------------------")
            log_tour.append("👤 TOUR DE : VOUS")
            log_tour.append(f"❓ Q : {question.texte_question}")
            r_str = brute if brute else "Passé"
            log_tour.append(f"💬 R : {r_str} -> {'✅ Juste' if est_correct else '❌ Faux'}")
            log_tour.append(f"🎲 Dé: {valeur_de} | 📍 Case {pos_avant_bots}")
            
            if resultat_humain.get("messageEffet"):
                log_tour.append(f"✨ {resultat_humain.get('messageEffet')}")
            if logs_ia:
                log_tour.extend(logs_ia)
            enregistrer_dans_historique(partie_id, "\n".join(log_tour) + "\n")

            return Response({
                "etaitBonneReponse": est_correct,
                "valeurDe": valeur_de if est_correct else None,
                "nouvellePosition": joueur_post_bots.position_plateau,
                "positionAvantBots": pos_avant_bots,
                "resultatTour": {
                    "victoire": victoire_humain, 
                    "effetEnAttente": resultat_humain.get("effet_en_attente"),
                    "messageEffet": resultat_humain.get("messageEffet"),
                    "aDroitDeuxiemeChance": a_droit_deuxieme_chance 
                },
                "logsIA": logs_ia,
                "partieTerminee": partie_terminee,
                "nomVainqueur": nom_vainqueur,
                "bonneReponse": bonne 
            }, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        
class EtatPartieAPIView(APIView):
    def get(self, request, partie_id):
        try:
            joueurs = PartieJoueur.objects.filter(partie_id=partie_id)
            liste_joueurs = []

            for pj in joueurs:
                effet = getattr(pj, 'effet_actif', "AUCUN")
                statut_final = effet if effet and effet != "AUCUN" else "NORMAL"

                liste_joueurs.append({
                    "id": pj.id,
                    "nom": pj.nom_ia if pj.est_ia else "Vous (Joueur)",
                    "position": pj.position_plateau,
                    "estIa": pj.est_ia,
                    "statut": statut_final,
                    "dureeEffet": getattr(pj, 'duree_effet', 0)
                })

            return Response({
                "partieId": partie_id,
                "joueurs": liste_joueurs
            }, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        
class AppliquerEffetAPIView(APIView):
    def post(self, request):
        lanceur_id = request.data.get('lanceurId')
        cible1_id = request.data.get('cible1Id')
        cible2_id = request.data.get('cible2Id')

        try:
            lanceur = PartieJoueur.objects.get(id=lanceur_id)

            if lanceur.effet_actif == "CIBLE_ECHANGE":
                MoteurJeuService.appliquer_effet_interactif(lanceur_id, cible1_id, cible2_id)
            else:
                MoteurJeuService.appliquer_effet_interactif(lanceur_id, cible1_id, None)

            partie_id = lanceur.partie_id
            plateau_actuel = PlateauService.charger_plateau_pour_moteur(partie_id)
            taille_plateau = max([int(k) for k in plateau_actuel.keys()]) + 1 if plateau_actuel else 50
            
            logs_ia, pt, nv = faire_jouer_bots(partie_id, taille_plateau, plateau_actuel)

            log_tour = ["--------------------------------------------------", "👤 TOUR DE : VOUS", "🎯 ACTION : Vous avez appliqué votre effet interactif."]
            if logs_ia: log_tour.extend(logs_ia)
            enregistrer_dans_historique(partie_id, "\n".join(log_tour) + "\n")

            return Response({
                "logsIA": logs_ia,
                "partieTerminee": pt,
                "nomVainqueur": nv
            }, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        
class PreparerSuperBonusAPIView(APIView):
    def post(self, request):
        joueur_id = request.data.get('joueurId')
        categorie = request.data.get('categorie')

        try:
            joueur = PartieJoueur.objects.get(id=joueur_id)
            q = HistoriqueQuestionService.tirer_question_par_categorie(joueur, categorie)

            if not q:
                return Response({"error": "Aucune question trouvée"}, status=status.HTTP_404_NOT_FOUND)

            mauvaises_props = [
                getattr(q, 'mauvaise_prop_1', None),
                getattr(q, 'mauvaise_prop_2', None),
                getattr(q, 'mauvaise_prop_3', None)
            ]
            mauvaises_valides = [p for p in mauvaises_props if p and str(p).strip()]
            type_final = str(q.type_question).upper() if q.type_question else "TEXTE"

            if len(mauvaises_valides) > 0 or "QCM" in type_final or "FAUX" in type_final:
                propositions = [str(q.bonne_reponse)] + [str(p) for p in mauvaises_valides]
                import random
                random.shuffle(propositions)
                type_final = "QCM" 
            else:
                propositions = []
                type_final = "TEXTE"

            return Response({
                "questionId": q.id,
                "texteQuestion": q.texte_question,
                "typeQuestion": type_final,
                "propositions": propositions 
            })

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class RepondreSuperBonusAPIView(APIView):
    def post(self, request):
        partie_id = request.data.get('partieId')
        joueur_id = request.data.get('joueurId')
        question_id = request.data.get('questionId')
        reponse_joueur = request.data.get('reponseJoueur')
        points_choisis = int(request.data.get('pointsChoisis', 3))
        effet_choisi = request.data.get('effetChoisi')

        try:
            joueur = PartieJoueur.objects.get(id=joueur_id)
            question = Question.objects.get(id=question_id)
            
            brute = str(reponse_joueur).strip() if reponse_joueur else ""
            bonne = str(question.bonne_reponse).strip()
            syns = str(question.synonymes_acceptes) if getattr(question, 'synonymes_acceptes', None) else ""
            
            est_correct = IaJugeService.evaluer_reponse(bonne, syns, brute)
            
            plateau_actuel = PlateauService.charger_plateau_pour_moteur(partie_id)
            taille_plateau = max([int(k) for k in plateau_actuel.keys()]) + 1 if plateau_actuel else 50
            
            message_effet = ""
            effet_en_attente = None
            doit_attendre_cible = False

            if est_correct:
                can_target = True
                if effet_choisi and effet_choisi.startswith("CIBLE_"):
                    tous_j = PartieJoueur.objects.filter(partie_id=partie_id)
                    if effet_choisi == "CIBLE_ECHANGE":
                        dispo = sum(1 for p in tous_j if p.effet_actif != "BOUCLIER" or p.id == joueur_id)
                        can_target = dispo >= 2
                    else:
                        dispo = sum(1 for p in tous_j if p.id != joueur_id and p.effet_actif != "BOUCLIER")
                        can_target = dispo >= 1

                if effet_choisi and effet_choisi.startswith("CIBLE_") and not can_target:
                    joueur.position_plateau = min(joueur.position_plateau + (points_choisis * 2), taille_plateau)
                    joueur.effet_actif = "BOUCLIER"
                    joueur.duree_effet = 2
                    message_effet = f"🌟 EXCELLENT ! Vu qu'il n'y a pas de cible disponible, vous avancez de 2 fois le chiffre ({points_choisis * 2} cases) !"
                    effet_en_attente = None
                else:
                    joueur.position_plateau = min(joueur.position_plateau + points_choisis, taille_plateau)
                    joueur.effet_actif = effet_choisi
                    joueur.duree_effet = 2 if effet_choisi == "BOUCLIER" else 1
                    message_effet = f"🌟 EXCELLENT ! Vous avancez de {points_choisis} cases et obtenez l'effet : {effet_choisi} !"
                    if effet_choisi and effet_choisi.startswith("CIBLE_"):
                        try: 
                            effet_en_attente = effet_choisi
                            doit_attendre_cible = True 
                        except Exception as e: pass
            else:
                joueur.effet_actif = "BOUCLIER"
                joueur.duree_effet = 2
                message_effet = "❌ Mauvaise réponse ! Mais vous gardez le Bouclier intact."
            
            joueur.save()

            pos_avant_bots = joueur.position_plateau 

            partie_terminee = joueur.position_plateau >= taille_plateau
            nom_vainqueur = "Vous" if partie_terminee else ""
            logs_ia = []
            
            if partie_terminee:
                partie_db = Partie.objects.get(id=partie_id)
                partie_db.statut = "TERMINEE"
                partie_db.vainqueur = joueur.utilisateur
                partie_db.save()
            elif not doit_attendre_cible:
                logs_ia, pt, nv = faire_jouer_bots(partie_id, taille_plateau, plateau_actuel)
                if pt:
                    partie_terminee = True
                    nom_vainqueur = nv

            log_tour = []
            log_tour.append("--------------------------------------------------")
            log_tour.append("👤 TOUR DE : VOUS (SUPER BONUS)")
            log_tour.append(f"❓ Q : {question.texte_question}")
            r_str = brute if brute else "Passé"
            log_tour.append(f"💬 R : {r_str} -> {'✅ Juste' if est_correct else '❌ Faux'}")
            log_tour.append(f"🎲 Dé (Points choisis): {points_choisis} | 📍 Case {pos_avant_bots}")
            if message_effet:
                log_tour.append(f"✨ {message_effet}")
            if logs_ia:
                log_tour.extend(logs_ia)
            enregistrer_dans_historique(partie_id, "\n".join(log_tour) + "\n")

            return Response({
                "etaitBonneReponse": est_correct,
                "valeurDe": points_choisis if est_correct else 0,
                "nouvellePosition": joueur.position_plateau,
                "positionAvantBots": pos_avant_bots, 
                "resultatTour": {
                    "victoire": joueur.position_plateau >= taille_plateau,
                    "messageEffet": message_effet,
                    "effetEnAttente": effet_en_attente
                },
                "logsIA": logs_ia,
                "partieTerminee": partie_terminee,
                "nomVainqueur": nom_vainqueur,
                "bonneReponse": bonne 
            }, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# ==========================================
# 🔐 VUES D'AUTHENTIFICATION (Classique & OAuth2)
# ==========================================
import requests
import urllib.parse
import uuid
from django.shortcuts import redirect
from django.conf import settings

# 1. Inscription Classique
class RegisterAPIView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get('email')
        pseudo = request.data.get('pseudo')
        mot_de_passe = request.data.get('motDePasse')

        if Utilisateur.objects.filter(email=email).exists():
            return Response({"error": "Cet email est déjà utilisé."}, status=status.HTTP_400_BAD_REQUEST)

        Utilisateur.objects.create(
            email=email,
            pseudo=pseudo,
            mot_de_passe=mot_de_passe,
            role="ROLE_USER"
        )
        return Response({"message": "Inscription réussie !"}, status=status.HTTP_201_CREATED)

# 2. Connexion Classique
class AuthAPIView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []

    def post(self, request):
        email = request.data.get('email')
        mot_de_passe = request.data.get('motDePasse')

        try:
            user = Utilisateur.objects.get(email=email)
            if user.mot_de_passe == mot_de_passe:
                token = JwtService.generer_token(user)
                return Response({
                    "token": token,
                    "role": user.role,
                    "pseudo": user.pseudo,
                    "utilisateurId": str(user.id)
                }, status=status.HTTP_200_OK)
            return Response({"error": "Mot de passe incorrect"}, status=status.HTTP_401_UNAUTHORIZED)
        except Utilisateur.DoesNotExist:
            return Response({"error": "Utilisateur introuvable"}, status=status.HTTP_404_NOT_FOUND)

# 3. GOOGLE OAuth2
class GoogleLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []
    def get(self, request):
        params = {
            'client_id': settings.GOOGLE_CLIENT_ID,
            'response_type': 'code',
            'redirect_uri': 'http://localhost:8000/api/auth/google/callback/',
            'scope': 'openid email profile',
            'access_type': 'offline',
            'prompt': 'consent'
        }
        url = f"https://accounts.google.com/o/oauth2/v2/auth?{urllib.parse.urlencode(params)}"
        return redirect(url)

class GoogleCallbackView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []
    def get(self, request):
        code = request.GET.get('code')
        if not code:
            return redirect(f"{settings.ANGULAR_LOGIN_URL}?error=AccessDenied")

        token_data = {
            'code': code,
            'client_id': settings.GOOGLE_CLIENT_ID,
            'client_secret': settings.GOOGLE_CLIENT_SECRET,
            'redirect_uri': 'http://localhost:8000/api/auth/google/callback/',
            'grant_type': 'authorization_code'
        }
        token_res = requests.post('https://oauth2.googleapis.com/token', data=token_data)
        access_token = token_res.json().get('access_token')

        user_res = requests.get('https://www.googleapis.com/oauth2/v3/userinfo', headers={'Authorization': f'Bearer {access_token}'})
        user_info = user_res.json()

        email = user_info.get('email')
        pseudo = user_info.get('name', f"Joueur_{email.split('@')[0]}")

        user, created = Utilisateur.objects.get_or_create(
            email=email,
            defaults={'pseudo': pseudo, 'mot_de_passe': str(uuid.uuid4()), 'role': 'ROLE_USER'}
        )

        token = JwtService.generer_token(user)
        pseudo_encode = urllib.parse.quote(user.pseudo)
        return redirect(f"{settings.ANGULAR_LOGIN_URL}?token={token}&id={user.id}&pseudo={pseudo_encode}&role={user.role}")

# 4. GITHUB OAuth2
class GithubLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []
    def get(self, request):
        params = {
            'client_id': settings.GITHUB_CLIENT_ID,
            'redirect_uri': 'http://localhost:8000/api/auth/github/callback/',
            'scope': 'read:user user:email'
        }
        url = f"https://github.com/login/oauth/authorize?{urllib.parse.urlencode(params)}"
        return redirect(url)

class GithubCallbackView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []
    def get(self, request):
        code = request.GET.get('code')
        if not code:
            return redirect(f"{settings.ANGULAR_LOGIN_URL}?error=AccessDenied")

        token_data = {
            'code': code,
            'client_id': settings.GITHUB_CLIENT_ID,
            'client_secret': settings.GITHUB_CLIENT_SECRET,
            'redirect_uri': 'http://localhost:8000/api/auth/github/callback/',
        }
        headers = {'Accept': 'application/json'}
        token_res = requests.post('https://github.com/login/oauth/access_token', data=token_data, headers=headers)
        access_token = token_res.json().get('access_token')

        user_res = requests.get('https://api.github.com/user', headers={'Authorization': f'Bearer {access_token}'})
        user_info = user_res.json()

        pseudo = user_info.get('login')
        email = user_info.get('email')

        if not email:
            email_res = requests.get('https://api.github.com/user/emails', headers={'Authorization': f'Bearer {access_token}'})
            primary_email = next((e for e in email_res.json() if e.get('primary')), None)
            email = primary_email.get('email') if primary_email else f"{pseudo}@github.com"

        user, created = Utilisateur.objects.get_or_create(
            email=email,
            defaults={'pseudo': pseudo, 'mot_de_passe': str(uuid.uuid4()), 'role': 'ROLE_USER'}
        )

        token = JwtService.generer_token(user)
        pseudo_encode = urllib.parse.quote(user.pseudo)
        return redirect(f"{settings.ANGULAR_LOGIN_URL}?token={token}&id={user.id}&pseudo={pseudo_encode}&role={user.role}")