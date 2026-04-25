import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { JeuService } from '../../services/jeu.service'; // Ton nouveau service REST
import { AuthService } from '../../services/auth.service'; // Le faux service créé juste au-dessus


@Component({
  selector: 'app-lobby',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './lobby.component.html',
  styleUrl: './lobby.component.css'
})
export class LobbyComponent {
  private jeuService = inject(JeuService);
  public authService = inject(AuthService);
  public router = inject(Router);

  messageErreur: string = '';
  etape: number = 1; // 1 = Config générale, 2 = Builder Custom

  // --- Paramètres de la partie ---
  nbBots: number = 3;
  typePlateau: number = 0; // 0: Classique, 1: Aléatoire, 2: Custom
  taillePlateau: number = 49; // Cases intermédiaires
  
  // --- Constructeur de Plateau Personnalisé ---
  effetsDispos = [
    'BOUCLIER', 'SUPER_BONUS', 'INDICE', 'DEUXIEME_CHANCE', 'SPRINT', 'PARI_MULTIPLICATEUR',
    'CIBLE_RECUL', 'CIBLE_PASSE_TOUR', 'CIBLE_ECHANGE', 'CIBLE_PRESSION', 'RISQUE_EXPLOSIF'
  ];
  effetSelectionne: string = 'BOUCLIER';
  plateauCustom: Record<number, any> = {};
  casesDisponibles: number[] = [];

  categories = ["HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"];

  // Passe de l'étape 1 à l'étape 2 (ou démarre direct si pas custom)
  validerConfig() {
    this.messageErreur = '';
    if (this.typePlateau === 1 && this.taillePlateau < 20) {
      this.messageErreur = "Pour un plateau aléatoire, il faut au moins 20 cases intermédiaires.";
      return;
    }
    if (this.typePlateau === 2 && this.taillePlateau < 1) {
      this.messageErreur = "Pour un plateau personnalisé, il faut au moins 1 case intermédiaire.";
      return;
    }

    if (this.typePlateau === 2) {
      // Prépare le constructeur visuel
      this.casesDisponibles = Array.from({length: this.taillePlateau}, (_, i) => i + 1);
      this.plateauCustom = {};
      this.etape = 2;
    } else {
      this.lancerPartie();
    }
  }

  // Clic sur une case vide pour placer l'effet
  placerEffet(numCase: number) {
    const catAlea = this.categories[Math.floor(Math.random() * this.categories.length)];
    const ptsAlea = Math.floor(Math.random() * 6) + 1;
    
    this.plateauCustom[numCase] = {
      effet: this.effetSelectionne,
      categorie: catAlea,
      points: ptsAlea
    };
    
    // Retire des dispos
    this.casesDisponibles = this.casesDisponibles.filter(c => c !== numCase);
  }

  // Clic sur une case déjà placée pour l'effacer
  effacerCase(numCase: number) {
    delete this.plateauCustom[numCase];
    this.casesDisponibles.push(numCase);
    this.casesDisponibles.sort((a, b) => a - b);
  }

  lancerPartie() {
    const payload = {
      utilisateurId: this.authService.getUserId(),
      nbBots: this.nbBots,
      typePlateau: this.typePlateau,
      taillePlateau: this.taillePlateau,
      plateauCustom: this.typePlateau === 2 ? this.plateauCustom : null
    };

    this.jeuService.demarrerPartie(payload).subscribe({
      next: (res) => {
        // 👉 NOUVEAU : On redirige en ajoutant '?jId=...' à la fin de l'URL
        this.router.navigate(['/plateau', res.partieId], { queryParams: { jId: res.joueurId } });
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = "Erreur lors du démarrage du serveur.";
      }
    });
  }
}