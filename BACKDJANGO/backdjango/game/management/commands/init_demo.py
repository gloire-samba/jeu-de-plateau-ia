from django.core.management.base import BaseCommand
from game.models import Utilisateur

class Command(BaseCommand):
    help = 'Initialise les comptes de démonstration'

    def handle(self, *args, **kwargs):
        Utilisateur.objects.get_or_create(
            email="admin@demo.com",
            defaults={'pseudo': 'Admin_Demo', 'mot_de_passe': 'admin123', 'role': 'ROLE_ADMIN'}
        )
        Utilisateur.objects.get_or_create(
            email="joueur@demo.com",
            defaults={'pseudo': 'Joueur_Demo', 'mot_de_passe': 'joueur123', 'role': 'ROLE_USER'}
        )
        self.stdout.write(self.style.SUCCESS("✅ Comptes Admin et Joueur créés !"))