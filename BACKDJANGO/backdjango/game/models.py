from django.db import models

# Create your models here.
from .managers import QuestionManager
from .managers import PartieJoueurManager


class Utilisateur(models.Model):
    pseudo = models.CharField(max_length=50, unique=True)
    email = models.CharField(max_length=100, unique=True)
    mot_de_passe = models.CharField(max_length=255) 
    role = models.CharField(max_length=20, default='ROLE_USER') # 👉 NOUVEAU
    date_inscription = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.pseudo


class Question(models.Model):
    categorie = models.CharField(max_length=50)
    type_question = models.CharField(max_length=20)
    type_reponse = models.CharField(max_length=20, null=True, blank=True)
    texte_question = models.TextField()
    bonne_reponse = models.TextField()
    mauvaise_prop_1 = models.TextField(null=True, blank=True)
    mauvaise_prop_2 = models.TextField(null=True, blank=True)
    mauvaise_prop_3 = models.TextField(null=True, blank=True)
    synonymes_acceptes = models.TextField(null=True, blank=True)

    objects = QuestionManager()

    def __str__(self):
        return self.texte_question[:50]


class Partie(models.Model):
    code_rejoindre = models.CharField(max_length=20, null=True, blank=True)
    statut = models.CharField(max_length=20)
    date_creation = models.DateTimeField(auto_now_add=True)
    tour_actuel = models.IntegerField(default=1)
    
    createur = models.ForeignKey(Utilisateur, on_delete=models.CASCADE, related_name='parties_creees')
    vainqueur = models.ForeignKey(Utilisateur, on_delete=models.SET_NULL, null=True, blank=True, related_name='parties_gagnees')
    questions_posees = models.ManyToManyField(Question, blank=True)

    def __str__(self):
        return f"Partie {self.id} - {self.code_rejoindre}"


class PartieJoueur(models.Model):
    partie = models.ForeignKey(Partie, on_delete=models.CASCADE, related_name='joueurs')
    utilisateur = models.ForeignKey(Utilisateur, on_delete=models.CASCADE, null=True, blank=True)
    est_ia = models.BooleanField(default=False)
    nom_ia = models.CharField(max_length=50, null=True, blank=True)
    position_plateau = models.IntegerField(default=0)
    ordre_tour = models.IntegerField(default=0)
    effet_actif = models.CharField(max_length=50, default='AUCUN')
    duree_effet = models.IntegerField(default=0)

    objects = PartieJoueurManager()

    def __str__(self):
        nom = self.nom_ia if self.est_ia else self.utilisateur.pseudo
        return f"{nom} (Partie {self.partie.id})"
    
class HistoriqueQuestion(models.Model):
    # Pour garder la mémoire du joueur humain sur le long terme
    utilisateur = models.ForeignKey(Utilisateur, on_delete=models.CASCADE, null=True, blank=True, related_name='historiques')
    
    # Pour garder la mémoire des bots uniquement pendant la partie
    partie_joueur = models.ForeignKey(PartieJoueur, on_delete=models.CASCADE, null=True, blank=True, related_name='historiques_bot')
    
    question = models.ForeignKey(Question, on_delete=models.CASCADE)
    categorie = models.CharField(max_length=50)

    def __str__(self):
        return f"Historique: Question {self.question.id} - {self.categorie}"
    
class CasePlateau(models.Model):
    partie = models.ForeignKey('Partie', on_delete=models.CASCADE, related_name='cases_plateau')
    position_plateau = models.IntegerField()
    effet = models.CharField(max_length=50, default="AUCUN")
    categorie = models.CharField(max_length=50, null=True, blank=True)
    points = models.IntegerField(default=0)

    class Meta:
        unique_together = ('partie', 'position_plateau')
        ordering = ['position_plateau']