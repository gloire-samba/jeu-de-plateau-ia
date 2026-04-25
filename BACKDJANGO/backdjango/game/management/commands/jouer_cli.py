import random
import time
import sys
import threading
from django.core.management.base import BaseCommand
from game.models import Utilisateur, Partie, PartieJoueur, Question
from game.services import PartieService, MoteurJeuService, HistoriqueQuestionService, TypeEffet
from game.ia_juge_service import IaJugeService

TOUTES_CATEGORIES = ["HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"]

def input_avec_condition_math(prompt_msg, min_val):
    print()
    print(prompt_msg, end="", flush=True)
    while True:
        ans = input().strip()
        try:
            val = int(ans)
            if val >= min_val: return val
            raise ValueError()
        except ValueError:
            error_msg = f"⚠️ Valeur invalide ('{ans}'). Veuillez taper un nombre >= {min_val}."
            sys.stdout.write("\033[1A\033[K\033[1A\033[K")
            sys.stdout.write(f"\r{error_msg}\n")
            sys.stdout.write(f"\r{prompt_msg}")
            sys.stdout.flush()

def input_avec_validation(prompt_msg, valid_choices, error_format):
    print() 
    print(prompt_msg, end="", flush=True)
    while True:
        ans = input().strip()
        if ans.upper() in valid_choices:
            return ans.upper()
        
        error_msg = f"⚠️ Mauvaise commande ('{ans}'). Veuillez taper {error_format}."
        sys.stdout.write("\033[1A\033[K\033[1A\033[K") 
        sys.stdout.write(f"\r{error_msg}\n") 
        sys.stdout.write(f"\r{prompt_msg}")  
        sys.stdout.flush()

def lire_entree_avec_chrono(prompt_msg, timeout_seconds, valid_choices=None, error_format=None):
    print("\n") 
    print(prompt_msg, end="", flush=True) 
    reponse = [None]
    error_msg = ""
    
    def get_input():
        try: reponse[0] = input()
        except: pass

    t = threading.Thread(target=get_input)
    t.daemon = True
    t.start()

    for i in range(timeout_seconds * 10, 0, -1):
        if reponse[0] is not None:
            ans = reponse[0].strip()
            if valid_choices and ans.upper() not in valid_choices:
                error_msg = f"⚠️ Mauvaise commande ('{ans}'). Veuillez taper {error_format}."
                reponse[0] = None 
                sys.stdout.write("\033[1A\033[K") 
                sys.stdout.write(f"\r{prompt_msg}") 
                sys.stdout.flush()
                t = threading.Thread(target=get_input)
                t.daemon = True
                t.start()
            else:
                return ans

        if i % 10 == 0:
            sys.stdout.write("\033[s\033[2A\r\033[K") 
            if error_msg: sys.stdout.write(error_msg)
            sys.stdout.write(f"\n\r\033[K⏳ {i // 10}s restantes...\033[u") 
            sys.stdout.flush()
        time.sleep(0.1)

    print("\n\n⏳ TEMPS ÉCOULÉ !")
    return "TEMPS_ECOULE"

class Command(BaseCommand):
    help = 'Lance le jeu en mode Terminal (CLI)'

    def handle(self, *args, **kwargs):
        self.stdout.write(self.style.SUCCESS("\n==========================================="))
        self.stdout.write(self.style.SUCCESS("🚀 BIENVENUE DANS LE JEU DE PLATEAU IA (CLI)"))
        self.stdout.write(self.style.SUCCESS("===========================================\n"))

        joueur_humain = self.authentifier_ou_creer()
        if not joueur_humain: return

        print("\nCombien de Bots voulez-vous affronter ?")
        nb_ia_str = input_avec_validation("Votre choix (1-3) : ", ["1", "2", "3"], "1, 2 ou 3")
        nb_ia = int(nb_ia_str)

        partie = PartieService.creer_partie_solo(joueur_humain.id, nb_ia)

        tous_les_effets = [e for e in TypeEffet if e != TypeEffet.AUCUN]

        print("\nChoisissez le type de plateau :")
        print("  0) Classique (49 cases intermédiaires, Arrivée à 50)")
        print("  1) Aléatoire (50% cases à effets réparties équitablement)")
        print("  2) Personnalisé (Construisez vous-même votre plateau)")
        choix_plateau = input_avec_validation("Votre choix (0-2) : ", ["0", "1", "2"], "0, 1 ou 2")

        plateau_actuel = None
        taille_plateau = 50

        if choix_plateau == "1":
            print()
            nb_cases = input_avec_condition_math("Entrez le nombre de cases INTERMÉDIAIRES (min 20) : ", 20)
            taille_plateau = nb_cases + 1
            plateau_actuel = {}
            
            nb_effets = nb_cases // 2
            pool = list(tous_les_effets)
            while len(pool) < nb_effets:
                pool.append(random.choice(tous_les_effets))
            random.shuffle(pool)
            pool = pool[:nb_effets]

            positions = list(range(1, nb_cases + 1))
            random.shuffle(positions)
            pos_effets = positions[:nb_effets]

            effet_idx = 0
            for i in range(1, nb_cases + 1):
                cat = random.choice(TOUTES_CATEGORIES)
                pts = random.randint(1, 6)
                if i in pos_effets:
                    plateau_actuel[i] = {"effet": pool[effet_idx], "categorie": cat, "points": pts}
                    effet_idx += 1
                else:
                    plateau_actuel[i] = {"effet": TypeEffet.AUCUN, "categorie": cat, "points": pts}
            print(f"✅ Plateau aléatoire généré avec {nb_cases} cases intermédiaires et {nb_effets} effets (Arrivée = Case {taille_plateau}).")

        elif choix_plateau == "2":
            print()
            nb_cases = input_avec_condition_math("Entrez le nombre de cases INTERMÉDIAIRES (minimum 1) : ", 1)
            taille_plateau = nb_cases + 1
            plateau_actuel = {}
            
            dispos = list(range(1, nb_cases + 1))
            index_effet = 0

            while dispos:
                effet_en_cours = tous_les_effets[index_effet]
                print(f"\n--- CRÉATION DU PLATEAU ---")
                print(f"Effet sélectionné : [{effet_en_cours.name}]")
                print(f"Cases disponibles : {dispos}")
                
                valid_choices = ["T"] + [str(d) for d in dispos]
                prompt = "Commandes : "
                if index_effet > 0: prompt += "[P] Précédent | "; valid_choices.append("P")
                if index_effet < len(tous_les_effets) - 1: prompt += "[S] Suivant | "; valid_choices.append("S")
                
                if plateau_actuel: prompt += "[E] Effacer une case | "; valid_choices.append("E")
                
                prompt += "[T] Terminer (et remplir cases normales) | [Numéro] Placer ici\nVotre choix : "

                reponse = input_avec_validation(prompt, valid_choices, "P, S, E, T ou un numéro libre")
                
                if reponse == "P": index_effet -= 1
                elif reponse == "S": index_effet += 1
                elif reponse == "T": break
                elif reponse == "E":
                    print("\nCases déjà placées :")
                    valid_effacer = ["A"]
                    for k, v in plateau_actuel.items():
                        print(f"  {k}) {v['effet'].name}")
                        valid_effacer.append(str(k))
                    rep_effacer = input_avec_validation("Numéro de la case à effacer (ou [A] Annuler) : ", valid_effacer, "un numéro placé ou A")
                    if rep_effacer != "A":
                        case_a_effacer = int(rep_effacer)
                        del plateau_actuel[case_a_effacer]
                        dispos.append(case_a_effacer)
                        dispos.sort() 
                        print(f"✅ Case {case_a_effacer} effacée et de nouveau disponible !")
                else:
                    case_choisie = int(reponse)
                    cat = random.choice(TOUTES_CATEGORIES)
                    pts = random.randint(1, 6)
                    plateau_actuel[case_choisie] = {"effet": effet_en_cours, "categorie": cat, "points": pts}
                    dispos.remove(case_choisie)
                    print(f"✅ {effet_en_cours.name} placé sur la case {case_choisie} ! (Catégorie : {cat}, {pts} pts)")
            
            print("⏳ Remplissage des cases restantes avec des cases normales...")
            for d in dispos:
                cat = random.choice(TOUTES_CATEGORIES)
                pts = random.randint(1, 6)
                plateau_actuel[d] = {"effet": TypeEffet.AUCUN, "categorie": cat, "points": pts}
            print(f"✅ Plateau personnalisé terminé (Arrivée = Case {taille_plateau}) !")

        self.boucle_de_jeu(partie, plateau_actuel, taille_plateau, tous_les_effets)

    def authentifier_ou_creer(self):
        email = input("Entrez votre email : ")
        mdp = input("Entrez votre mot de passe : ")
        try:
            user = Utilisateur.objects.get(email=email)
            if user.mot_de_passe == mdp:
                self.stdout.write(self.style.SUCCESS(f"✅ Connexion réussie ! Bienvenue {user.pseudo}"))
                return user
            else:
                self.stdout.write(self.style.ERROR("❌ Mot de passe incorrect."))
                return None
        except Utilisateur.DoesNotExist:
            self.stdout.write(self.style.WARNING("⚠️ Identifiants introuvables. Création d'un compte."))
            pseudo = input("Choisissez un pseudo : ")
            return Utilisateur.objects.create(email=email, mot_de_passe=mdp, pseudo=pseudo)

    def boucle_de_jeu(self, partie, plateau_actuel, taille_plateau, tous_les_effets):
        partie_en_cours = True
        while partie_en_cours:
            tous_joueurs = list(PartieJoueur.objects.joueurs_tries_par_tour(partie.id))
            print("\n📊 RÉSUMÉ DU PLATEAU :")
            for pj in tous_joueurs:
                nom_p = pj.nom_ia if pj.est_ia else pj.utilisateur.pseudo
                nom_effet_p = pj.effet_actif
                if nom_effet_p == 'BONUS_PRESSION': nom_effet_p = 'BONUS_PRESSION (Lanceur)'
                elif nom_effet_p == 'MALUS_PRESSION': nom_effet_p = 'MALUS_PRESSION (Victime)'
                eff = f" ({nom_effet_p})" if nom_effet_p and nom_effet_p != 'AUCUN' else ""
                print(f"   - {nom_p} : Case {pj.position_plateau}{eff}")

            for j in tous_joueurs:
                if j.position_plateau >= taille_plateau:
                    partie_en_cours = False; break

                nom = j.nom_ia if j.est_ia else j.utilisateur.pseudo
                nom_effet_t = j.effet_actif
                if nom_effet_t == 'BONUS_PRESSION': nom_effet_t = 'BONUS_PRESSION (Lanceur)'
                elif nom_effet_t == 'MALUS_PRESSION': nom_effet_t = 'MALUS_PRESSION (Victime)'
                eff_tour = f" [Sous effet : {nom_effet_t}]" if nom_effet_t and nom_effet_t != 'AUCUN' else ""
                
                print(f"\n==================================================")
                print(f"👉 TOUR DE : {nom}{eff_tour} | Position initiale : Case {j.position_plateau}")
                print(f"==================================================")
                
                if j.effet_actif == 'MALUS_PASSE_TOUR':
                    print(f"🛑 {nom} passe son tour à cause du Malus !")
                    j.effet_actif = 'AUCUN'
                    j.save()
                    time.sleep(1.5)
                    continue

                temps_limite = 60
                a_pression = False
                if j.effet_actif == 'MALUS_PRESSION':
                    temps_limite = 30; a_pression = True; j.effet_actif = 'AUCUN'; j.save()
                elif j.effet_actif == 'BONUS_PRESSION':
                    temps_limite = 90; j.effet_actif = 'AUCUN'; j.save()

                a_indice = (j.effet_actif == 'INDICE')
                if a_indice: j.effet_actif = 'AUCUN'; j.save()

                case_a = plateau_actuel.get(j.position_plateau) or plateau_actuel.get(str(j.position_plateau)) if plateau_actuel else None
                effet_depart = MoteurJeuService.get_effet_for_position(j.position_plateau, plateau_actuel)
                
                pari_choisi = 0
                if effet_depart == TypeEffet.PARI_MULTIPLICATEUR:
                    print("\n🎰 EFFET QUITTE OU DOUBLE !")
                    print("Choisissez un multiplicateur (1 à 6). Si vous répondez juste, vous avancerez de : multiplicateur * points de la case.")
                    print("Si vous vous trompez, vous RECULEREZ de ce même montant !")
                    if not j.est_ia:
                        in_pari = input_avec_validation("Votre multiplicateur (1-6) : ", ["1", "2", "3", "4", "5", "6"], "un chiffre entre 1 et 6")
                        pari_choisi = int(in_pari)
                    else:
                        pari_choisi = random.randint(1, 6)
                        print(f"🤖 L'IA prend un risque et choisit le multiplicateur : {pari_choisi}")
                        time.sleep(1.2)

                if case_a and case_a.get('categorie'):
                    print(f"🏷️ Catégorie imposée par la case : {case_a['categorie']}")
                    question = HistoriqueQuestionService.tirer_question_par_categorie(j, case_a['categorie'])
                else:
                    question = HistoriqueQuestionService.tirer_question_pour_joueur(j)

                if not question:
                    print("❌ Erreur : Aucune question !")
                    partie_en_cours = False; break

                print(f"❓ QUESTION : {question.texte_question}")
                
                is_qcm = question.type_question.startswith('QCM') if question.type_question else False
                qcm_mapping = {}
                if is_qcm:
                    props = [question.bonne_reponse, question.mauvaise_prop_1, question.mauvaise_prop_2, question.mauvaise_prop_3]
                    props = [p for p in props if p]
                    random.shuffle(props)
                    lettres = ['A', 'B', 'C', 'D']
                    print("Propositions :")
                    for i, p in enumerate(props):
                        lettre = lettres[i]
                        qcm_mapping[lettre] = p
                        print(f"  {lettre}) {p}")

                reponse_affichee, reponse_evaluee = "", ""  

                if not j.est_ia:
                    if a_indice: print(f"💡 INDICE : {question.bonne_reponse[:max(1, len(question.bonne_reponse)//2)]}...")
                    
                    prompt_msg = "Votre réponse (A/B/C/D) : " if is_qcm else "Votre réponse : "
                    valid_choices = ['A', 'B', 'C', 'D'] if is_qcm else None
                    error_format = "A, B, C ou D" if is_qcm else ""
                    
                    reponse_affichee = lire_entree_avec_chrono(prompt_msg, temps_limite, valid_choices, error_format)
                    if is_qcm and reponse_affichee.upper() in qcm_mapping:
                        reponse_evaluee = qcm_mapping[reponse_affichee.upper()]
                    else:
                        reponse_evaluee = reponse_affichee
                else:
                    if a_indice: print("💡 L'IA utilise son indice...")
                    time.sleep(1.2)
                    
                    chance_passer = random.randint(0, 99)
                    seuil_passer = 5 if a_indice else (80 if a_pression else 20)
                    
                    if chance_passer < seuil_passer: 
                        reponse_affichee = reponse_evaluee = "PASSER"
                    else:
                        chance_juste = random.randint(0, 99)
                        seuil_bon = 90 if a_indice else (40 if a_pression else 60)
                        
                        if chance_juste < seuil_bon:
                            if is_qcm:
                                reponse_affichee = [k for k,v in qcm_mapping.items() if v == question.bonne_reponse][0]
                                reponse_evaluee = question.bonne_reponse
                            else: reponse_affichee = reponse_evaluee = question.bonne_reponse
                        else:
                            if is_qcm:
                                fausses = [k for k,v in qcm_mapping.items() if v != question.bonne_reponse]
                                reponse_affichee = random.choice(fausses) if fausses else "PASSER"
                                reponse_evaluee = qcm_mapping.get(reponse_affichee, "PASSER")
                            else:
                                q_sim = Question.objects.filter(categorie=question.categorie, type_reponse=question.type_reponse).exclude(id=question.id)
                                if not q_sim.exists(): q_sim = Question.objects.filter(categorie=question.categorie).exclude(id=question.id)
                                f_q = q_sim.order_by('?').first()
                                if f_q:
                                    reponse_affichee = reponse_evaluee = f_q.bonne_reponse
                                    if reponse_evaluee == question.bonne_reponse: reponse_affichee = reponse_evaluee = "Euh... je ne sais plus."
                                else: reponse_affichee = reponse_evaluee = "Je ne sais pas"
                    print(f"🗣️ L'IA répond : {reponse_affichee}")

                est_bonne = False
                if reponse_evaluee.upper() == "PASSER" or reponse_evaluee == "TEMPS_ECOULE":
                    print(f"⏭️ Passe ou Temps écoulé. 💡 Réponse : {question.bonne_reponse}")
                else:
                    est_bonne = IaJugeService.evaluer_reponse(question.bonne_reponse, question.synonymes_acceptes, reponse_evaluee)
                    if est_bonne: print("✅ Résultat : BONNE RÉPONSE !")
                    else: print(f"❌ Résultat : MAUVAISE RÉPONSE ! 💡 Réponse : {question.bonne_reponse}")

                valeur_de = 0
                
                if effet_depart == TypeEffet.PARI_MULTIPLICATEUR:
                    valeur_de = pari_choisi 
                elif est_bonne:
                    if case_a and case_a.get('points', 0) > 0:
                        valeur_de = case_a['points']
                        print(f"🎲 La case rapporte une valeur fixe de : {valeur_de}")
                    else:
                        valeur_de = random.randint(1, 6)
                        print(f"🎲 Résultat du dé : {valeur_de}")
                
                res = MoteurJeuService.traiter_reponse(partie.id, j.id, est_bonne, valeur_de, plateau_actuel, taille_plateau)
                j.refresh_from_db()

                while j.effet_actif and (j.effet_actif.startswith('CIBLE_') or j.effet_actif == 'SUPER_BONUS'):
                    effet_nom = j.effet_actif
                    current_joueur_id = j.id
                    
                    if effet_nom == 'SUPER_BONUS':
                        print("\n🌟 Le super bonus est activé et vous avez un bouclier pour le reste de ce tour et le tour suivant !")
                        j.effet_actif = 'BOUCLIER'
                        j.duree_effet = 2
                        j.save()

                        # 👉 CORRECTION PYTHON : Liste dynamique des effets (Sauf AUCUN, SUPER_BONUS ET BOUCLIER)
                        effets_super_bonus = [e for e in tous_les_effets if e.name not in ["AUCUN", "SUPER_BONUS", "BOUCLIER"]]
                        
                        valid1to6 = [str(i) for i in range(1, 7)]
                        c_choisie = TOUTES_CATEGORIES[0]
                        pts_choix = 3
                        e_choix = "AUCUN"

                        if not j.est_ia:
                            print("\nSélectionnez une Catégorie :")
                            valid_cat = []
                            for i, c in enumerate(TOUTES_CATEGORIES): 
                                print(f"  {i+1}) {c}")
                                valid_cat.append(str(i+1))
                            c_in = input_avec_validation("Numéro de la catégorie : ", valid_cat, "un chiffre valide")
                            c_choisie = TOUTES_CATEGORIES[int(c_in)-1]
                            
                            print("\nSélectionnez un Effet :")
                            valid_effets = []
                            for i, e in enumerate(effets_super_bonus): 
                                print(f"  {i+1}) {e.name}")
                                valid_effets.append(str(i+1))
                            e_in = input_avec_validation("Numéro de l'effet : ", valid_effets, "un chiffre valide")
                            e_choix = effets_super_bonus[int(e_in)-1].name

                            pts_in = input_avec_validation("Entrez le chiffre de la case (1-6) : ", valid1to6, "un chiffre entre 1 et 6")
                            pts_choix = int(pts_in)
                            
                        else:
                            c_choisie = random.choice(TOUTES_CATEGORIES)
                            e_choix = random.choice(effets_super_bonus).name
                            pts_choix = random.randint(1, 6)
                            print(f"🤖 L'IA choisit la catégorie {c_choisie}, l'effet {e_choix} et la valeur {pts_choix}")
                            time.sleep(1.5)

                        qb = HistoriqueQuestionService.tirer_question_par_categorie(j, c_choisie)
                        print(f"\n❓ QUESTION BONUS : {qb.texte_question}")
                        
                        is_qcm_b = qb.type_question and qb.type_question.startswith('QCM')
                        qcm_mapping_b = {}
                        if is_qcm_b:
                            props_b = [qb.bonne_reponse, qb.mauvaise_prop_1, qb.mauvaise_prop_2, qb.mauvaise_prop_3]
                            props_b = [p for p in props_b if p]
                            random.shuffle(props_b)
                            lettres_b = ['A', 'B', 'C', 'D']
                            print("Propositions :")
                            for i, p in enumerate(props_b):
                                lettre = lettres_b[i]
                                qcm_mapping_b[lettre] = p
                                print(f"  {lettre}) {p}")

                        rep_bonus_evaluee = ""
                        if not j.est_ia:
                            prompt_msg_b = "Votre réponse (A/B/C/D) : " if is_qcm_b else "Votre réponse : \n"
                            valid_choices_b = ['A', 'B', 'C', 'D'] if is_qcm_b else None
                            error_format_b = "A, B, C ou D" if is_qcm_b else ""
                            rep_affichee_b = lire_entree_avec_chrono(prompt_msg_b, 60, valid_choices_b, error_format_b)
                            
                            if is_qcm_b and rep_affichee_b.upper() in qcm_mapping_b:
                                rep_bonus_evaluee = qcm_mapping_b[rep_affichee_b.upper()]
                            else:
                                rep_bonus_evaluee = rep_affichee_b
                        else:
                            chance_b = random.randint(0, 99)
                            rep_affichee_b = ""
                            if chance_b < 20:
                                rep_bonus_evaluee = "PASSER"
                                rep_affichee_b = "PASSER"
                            elif chance_b < 80:
                                rep_bonus_evaluee = qb.bonne_reponse
                                if is_qcm_b:
                                    rep_affichee_b = [k for k,v in qcm_mapping_b.items() if v == rep_bonus_evaluee][0]
                                else:
                                    rep_affichee_b = rep_bonus_evaluee
                            else:
                                rep_bonus_evaluee = "Je ne sais pas"
                                if is_qcm_b:
                                    fausses = [k for k,v in qcm_mapping_b.items() if v != qb.bonne_reponse]
                                    rep_affichee_b = random.choice(fausses) if fausses else "PASSER"
                                    rep_bonus_evaluee = qcm_mapping_b.get(rep_affichee_b, "PASSER")
                                else:
                                    rep_affichee_b = rep_bonus_evaluee
                            print(f"🗣️ L'IA répond : {rep_affichee_b}")
                            time.sleep(1.2)

                        if rep_bonus_evaluee != "TEMPS_ECOULE" and IaJugeService.evaluer_reponse(qb.bonne_reponse, qb.synonymes_acceptes, rep_bonus_evaluee):
                            print(f"✅ Bonne réponse ! Vous avancez de {pts_choix} cases et déclenchez l'effet : {e_choix}")
                            j.position_plateau = min(taille_plateau, j.position_plateau + pts_choix)
                            j.effet_actif = e_choix
                            j.duree_effet = 2 if e_choix == "BOUCLIER" else 1
                            j.save()
                        else:
                            print(f"❌ Mauvaise réponse mais vous gardez le bouclier. 💡 Réponse : {qb.bonne_reponse}")
                            break
                    else:
                        tous_valides = [p for p in tous_joueurs if p.effet_actif != 'BOUCLIER' or p.id == current_joueur_id]
                        adversaires_valides = [p for p in tous_valides if p.id != current_joueur_id]

                        if (effet_nom == 'CIBLE_ECHANGE' and len(tous_valides) < 2) or (effet_nom != 'CIBLE_ECHANGE' and not adversaires_valides):
                            print("\n⚠️ Impossible d'appliquer l'effet : tout le monde est immunisé !")
                            print(f"🎁 COMPENSATION : Vous avancez du double de votre dé initial (+{valeur_de} cases) !")
                            j.position_plateau = min(taille_plateau, j.position_plateau + valeur_de)
                            j.effet_actif = 'AUCUN'
                            j.save()
                        else:
                            id_cible1, id_cible2 = None, None

                            if effet_nom == 'CIBLE_ECHANGE':
                                print(f"\n⚠️ ACTION REQUISE : 🔄 ÉCHANGE DE PLACES !")
                                if not j.est_ia:
                                    for idx, adv in enumerate(tous_valides): print(f"  {idx}) {adv.nom_ia if adv.est_ia else adv.utilisateur.pseudo} case {adv.position_plateau}")
                                    valid_c1 = [str(i) for i in range(len(tous_valides))]
                                    c1_str = input_avec_validation("Numéro 1ère cible : ", valid_c1, f"un chiffre valide")
                                    id_cible1 = tous_valides[int(c1_str)].id

                                    tous_valides_restants = [p for p in tous_valides if p.id != id_cible1]
                                    print("Cibles restantes :")
                                    for idx, adv in enumerate(tous_valides_restants): print(f"  {idx}) {adv.nom_ia if adv.est_ia else adv.utilisateur.pseudo} case {adv.position_plateau}")
                                    valid_c2 = [str(i) for i in range(len(tous_valides_restants))]
                                    c2_str = input_avec_validation("Numéro 2ème cible : ", valid_c2, f"un chiffre valide")
                                    id_cible2 = tous_valides_restants[int(c2_str)].id
                                else:
                                    cibles = random.sample(tous_valides, 2)
                                    id_cible1, id_cible2 = cibles[0].id, cibles[1].id
                                    print("🤖 L'IA a choisi ses cibles !")
                                MoteurJeuService.appliquer_effet_interactif(current_joueur_id, id_cible1, id_cible2)

                            else:
                                if effet_nom == 'CIBLE_RECUL': print(f"\n⚠️ ACTION REQUISE : 💥 FAIRE RECULER UN ADVERSAIRE !")
                                else: print(f"\n⚠️ ACTION REQUISE : 🎯 APPLIQUER UN MALUS [{effet_nom}] !")

                                if not j.est_ia:
                                    for idx, adv in enumerate(adversaires_valides): print(f"  {idx}) {adv.nom_ia if adv.est_ia else adv.utilisateur.pseudo} case {adv.position_plateau}")
                                    valid_v = [str(i) for i in range(len(adversaires_valides))]
                                    choix_str = input_avec_validation("Victime : ", valid_v, f"un chiffre valide")
                                    id_cible1 = adversaires_valides[int(choix_str)].id
                                else:
                                    id_cible1 = random.choice(adversaires_valides).id
                                    print(f"🤖 L'IA attaque !")
                                MoteurJeuService.appliquer_effet_interactif(current_joueur_id, id_cible1)

                    j.refresh_from_db()

                if res.get("victoire") or j.position_plateau >= taille_plateau:
                    print(f"\n🎉 VICTOIRE DE : {nom} !")
                    partie.statut = "TERMINEE"
                    partie.vainqueur = j.utilisateur
                    partie.save()
                    partie_en_cours = False; break
                time.sleep(1.5)
        print("\n🎉 FIN DE LA PARTIE !")