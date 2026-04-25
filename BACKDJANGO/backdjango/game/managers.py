from django.db import models

class QuestionManager(models.Manager):
    """
    Équivalent de ton QuestionRepository.java
    """
    def tirage_aleatoire(self, categorie):
        # order_by('?') est la façon Django de faire un ORDER BY RAND()
        return self.filter(categorie=categorie).order_by('?').first()

    def tirage_aleatoire_sans_doublons(self, categorie, liste_ids_exclus):
        # exclude(id__in=...) est l'équivalent du NOT IN (SQL)
        return self.filter(categorie=categorie).exclude(id__in=liste_ids_exclus).order_by('?').first()


class PartieJoueurManager(models.Manager):
    """
    Équivalent de ton PartieJoueurRepository.java
    """
    def joueurs_tries_par_tour(self, partie_id):
        return self.filter(partie_id=partie_id).order_by('ordre_tour')