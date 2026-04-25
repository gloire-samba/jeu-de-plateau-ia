import os
from django.core.management.base import BaseCommand
from django.db import connection
from django.conf import settings

class Command(BaseCommand):
    help = 'Exécute le fichier questions_bdd.sql pour remplir la base de données SQLite'

    def handle(self, *args, **kwargs):
        # On pointe vers le fichier SQL situé à côté de manage.py
        chemin_fichier = os.path.join(settings.BASE_DIR, 'questions_bdd.sql')
        
        if not os.path.exists(chemin_fichier):
            self.stdout.write(self.style.ERROR(f"❌ Le fichier est introuvable à cet emplacement : {chemin_fichier}"))
            return

        self.stdout.write(f"⏳ Lecture du fichier {chemin_fichier}...")
        
        with open(chemin_fichier, 'r', encoding='utf-8') as f:
            sql_content = f.read()

        # SQLite ne comprend pas la commande "USE nom_de_la_base;", on doit la retirer
        sql_content = sql_content.replace("USE jeu_de_plateau_bdd;", "")
        
        # AJOUTE CETTE LIGNE : On adapte le nom de la table au standard Django
        sql_content = sql_content.replace("INSERT INTO QUESTION", "INSERT INTO game_question")

        self.stdout.write("⏳ Exécution des requêtes SQL...")
        
        try:
            with connection.cursor() as cursor:
                # executescript est une fonction native de SQLite idéale pour les longs fichiers SQL
                cursor.connection.executescript(sql_content)
            self.stdout.write(self.style.SUCCESS("✅ Les 120 questions ont été importées avec succès dans SQLite !"))
        except Exception as e:
            self.stdout.write(self.style.ERROR(f"❌ Erreur lors de l'importation : {str(e)}"))