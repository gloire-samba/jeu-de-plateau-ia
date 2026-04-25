from rest_framework import serializers
from .models import Partie, Utilisateur, Question, PartieJoueur, HistoriqueQuestion, CasePlateau


# --- SERIALIZERS SPÉCIFIQUES JEU (Déjà présents) ---
class NouvellePartieSoloSerializer(serializers.Serializer):
    utilisateur_id = serializers.IntegerField()
    nb_ia = serializers.IntegerField(min_value=1, max_value=3, default=3)

class PartieResponseSerializer(serializers.ModelSerializer):
    nombre_joueurs = serializers.SerializerMethodField()
    class Meta:
        model = Partie
        fields = ['id', 'statut', 'nombre_joueurs']
    def get_nombre_joueurs(self, obj):
        return obj.joueurs.count()

class JoueurEtatSerializer(serializers.ModelSerializer):
    nom = serializers.SerializerMethodField()
    class Meta:
        model = PartieJoueur
        fields = ['id', 'nom', 'position_plateau', 'ordre_tour', 'effet_actif', 'duree_effet', 'est_ia']
    def get_nom(self, obj):
        return obj.nom_ia if obj.est_ia else obj.utilisateur.pseudo

class PartieEtatCompletSerializer(serializers.ModelSerializer):
    joueurs = JoueurEtatSerializer(many=True)
    class Meta:
        model = Partie
        fields = ['id', 'code_rejoindre', 'statut', 'tour_actuel', 'joueurs']

# ==========================================
# SERIALIZERS CRUD ADMIN / STANDARD
# ==========================================

class UtilisateurCrudSerializer(serializers.ModelSerializer):
    class Meta:
        model = Utilisateur
        fields = ['id', 'pseudo', 'email', 'role', 'date_inscription'] # 👉 NOUVEAU : 'role' ajouté

class QuestionCrudSerializer(serializers.ModelSerializer):
    class Meta:
        model = Question
        fields = '__all__' # Prend tous les champs

class PartieCrudSerializer(serializers.ModelSerializer):
    # On ajoute ces champs pour qu'Angular puisse afficher les noms au lieu des IDs
    pseudoCreateur = serializers.ReadOnlyField(source='createur.pseudo')
    pseudoVainqueur = serializers.ReadOnlyField(source='vainqueur.pseudo')

    class Meta:
        model = Partie
        fields = [
            'id', 'code_rejoindre', 'statut', 'date_creation', 
            'tour_actuel', 'createur', 'pseudoCreateur', 
            'vainqueur', 'pseudoVainqueur', 'historique'
        ]

class PartieJoueurCrudSerializer(serializers.ModelSerializer):
    class Meta:
        model = PartieJoueur
        fields = '__all__'
        
class HistoriqueQuestionSerializer(serializers.ModelSerializer):
    class Meta:
        model = HistoriqueQuestion
        fields = '__all__'
        
class CasePlateauSerializer(serializers.ModelSerializer):
    class Meta:
        model = CasePlateau
        fields = ['id', 'partie', 'position_plateau', 'effet', 'categorie', 'points']