import unicodedata
import re

class IaJugeService:

    @staticmethod
    def evaluer_reponse(reponse_attendue: str, synonymes: str, reponse_joueur: str) -> bool:
        if not reponse_joueur or not reponse_attendue or reponse_joueur.strip() == "":
            return False

        # 1. On teste la réponse officielle stricte
        if IaJugeService.tester_correspondance(reponse_attendue, reponse_joueur):
            return True

        # 2. On teste la liste des synonymes
        if synonymes and synonymes.strip():
            liste_synonymes = synonymes.split(',')
            for mot in liste_synonymes:
                if IaJugeService.tester_correspondance(mot.strip(), reponse_joueur):
                    return True

        return False

    @staticmethod
    def tester_correspondance(attendu: str, test: str) -> bool:
        cible = IaJugeService.nettoyer(attendu)
        essai = IaJugeService.nettoyer(test)

        if cible == essai:
            return True

        distance = IaJugeService.calculer_levenshtein(cible, essai)

        # 👉 TOLÉRANCE PROPORTIONNELLE (Jusqu'à 5 fautes)
        longueur = len(cible)
        if longueur <= 5:
            tolerance = 1
        elif longueur <= 10:
            tolerance = 2
        elif longueur <= 15:
            tolerance = 3
        elif longueur <= 20:
            tolerance = 4
        else:
            tolerance = 5

        return distance <= tolerance

    @staticmethod
    def nettoyer(texte: str) -> str:
        # Met en minuscule et retire les accents
        texte = texte.strip().lower()
        texte = ''.join(c for c in unicodedata.normalize('NFD', texte) if unicodedata.category(c) != 'Mn')
        
        # Remplace tirets et apostrophes par des espaces
        texte = re.sub(r"[-']", " ", texte)
        
        # 👉 SUPPRESSION DES ARTICLES au début du mot
        texte = re.sub(r"^(le |la |les |l |un |une |des )", "", texte)
        
        return texte.strip()

    @staticmethod
    def calculer_levenshtein(a: str, b: str) -> int:
        if len(a) < len(b):
            return IaJugeService.calculer_levenshtein(b, a)
        if len(b) == 0:
            return len(a)
        
        previous_row = range(len(b) + 1)
        for i, c1 in enumerate(a):
            current_row = [i + 1]
            for j, c2 in enumerate(b):
                insertions = previous_row[j + 1] + 1
                deletions = current_row[j] + 1
                substitutions = previous_row[j] + (c1 != c2)
                current_row.append(min(insertions, deletions, substitutions))
            previous_row = current_row
        
        return previous_row[-1]