import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-liste-partie-joueurs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './liste-partie-joueurs.component.html'
})
export class ListePartieJoueursComponent implements OnInit {
  partieJoueurs: any[] = [];
  private http = inject(HttpClient);
  public authService = inject(AuthService);
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  ngOnInit() {
    this.chargerJoueurs();
  }

  chargerJoueurs() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/partie-joueurs${isDjango ? '/' : ''}`;
    
    this.http.get<any>(url).subscribe({
      next: (res) => this.partieJoueurs = res.results ? res.results : res,
      error: (err) => console.error("Erreur de chargement", err)
    });
  }

  supprimer(id: number) {
    if (confirm('Voulez-vous vraiment retirer ce joueur de la partie ?')) {
      const isDjango = this.authService.getBaseUrl().includes('8000');
      const url = `${this.authService.getBaseUrl()}/partie-joueurs/${id}${isDjango ? '/' : ''}`;
      this.http.delete(url).subscribe(() => this.chargerJoueurs());
    }
  }
}