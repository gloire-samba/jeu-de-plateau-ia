import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { routes } from '../../app.routes';

@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.css'
})
export class ProfilComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  // Les données du formulaire
  pseudo: string = '';
  email: string = '';
  nouveauMotDePasse: string = '';
  
  // Messages pour l'utilisateur
  messageSucces: string = '';
  messageErreur: string = '';

  ngOnInit() {
    this.chargerMonProfil();
  }

  chargerMonProfil() {
    const monId = this.authService.getUserId();
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/utilisateurs/${monId}${isDjango ? '/' : ''}`;

    this.http.get<any>(url).subscribe({
      next: (donnees) => {
        this.pseudo = donnees.pseudo;
        this.email = donnees.email;
        // On ne charge pas le mot de passe, il est secret !
      },
      error: () => this.messageErreur = "Impossible de charger votre profil."
    });
  }

  sauvegarderProfil() {
    this.messageSucces = '';
    this.messageErreur = '';
    const monId = this.authService.getUserId();
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/utilisateurs/${monId}${isDjango ? '/' : ''}`;

    // On prépare les données à envoyer
    const payload: any = {
      pseudo: this.pseudo,
      email: this.email
    };

    // Si l'utilisateur a tapé un nouveau mot de passe, on l'ajoute !
    if (this.nouveauMotDePasse && this.nouveauMotDePasse.trim() !== '') {
      payload.motDePasse = this.nouveauMotDePasse;
    }

    // On utilise PUT pour mettre à jour
    this.http.put(url, payload).subscribe({
      next: () => {
        this.messageSucces = "Votre profil a été mis à jour avec succès !";
        this.nouveauMotDePasse = ''; // On vide le champ mot de passe
      },
      error: (err) => {
        console.error(err);
        this.messageErreur = "Erreur lors de la sauvegarde (Ce pseudo ou email est peut-être déjà pris).";
      }
    });
  }
}