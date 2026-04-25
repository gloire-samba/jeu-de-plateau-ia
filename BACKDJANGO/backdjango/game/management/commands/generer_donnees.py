import random
from datetime import timedelta
from django.core.management.base import BaseCommand
from django.utils import timezone
from faker import Faker
from game.models import Utilisateur, Partie, PartieJoueur

class Command(BaseCommand):
    help = 'Génère 10 utilisateurs, 10 parties en cours et 10 parties terminées'

    def handle(self, *args, **kwargs):
        if Utilisateur.objects.exists():
            self.stdout.write(self.style.WARNING("⚠️ La base SQLite contient déjà des utilisateurs. Script ignoré."))
            return

        self.stdout.write("⏳ Génération des fausses données en cours...")
        fake = Faker('fr_FR')
        utilisateurs = []

        # 1. 10 Utilisateurs
        for i in range(10):
            u = Utilisateur.objects.create(
                pseudo=f"{fake.user_name()}_{i}",
                email=fake.email(),
                mot_de_passe="password123"
            )
            # Remplacement de la date d'inscription automatique
            u.date_inscription = timezone.now() - timedelta(days=random.randint(1, 30))
            u.save()
            utilisateurs.append(u)

        def creer_joueurs(partie, createur, est_terminee):
            PartieJoueur.objects.create(
                partie=partie, 
                utilisateur=createur, 
                est_ia=False, 
                ordre_tour=1,
                position_plateau=50 if est_terminee else random.randint(0, 30)
            )
            for j in range(3):
                PartieJoueur.objects.create(
                    partie=partie, 
                    est_ia=True, 
                    nom_ia=f"Bot {fake.first_name()}", 
                    ordre_tour=j+2,
                    position_plateau=random.randint(10, 49) if est_terminee else random.randint(0, 30)
                )

        # 2. 10 Parties Terminées
        for _ in range(10):
            createur = random.choice(utilisateurs)
            p = Partie.objects.create(
                statut="TERMINEE", 
                createur=createur, 
                vainqueur=createur, 
                tour_actuel=random.randint(15, 40)
            )
            p.date_creation = timezone.now() - timedelta(days=random.randint(1, 15))
            p.save()
            creer_joueurs(p, createur, True)

        # 3. 10 Parties En Cours
        for _ in range(10):
            createur = random.choice(utilisateurs)
            p = Partie.objects.create(
                statut="EN_COURS", 
                createur=createur, 
                tour_actuel=random.randint(1, 10)
            )
            creer_joueurs(p, createur, False)

        self.stdout.write(self.style.SUCCESS("✅ Génération terminée avec succès !"))