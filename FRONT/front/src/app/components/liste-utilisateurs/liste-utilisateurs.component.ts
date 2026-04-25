import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router'; //

@Component({
  selector: 'app-liste-utilisateurs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './liste-utilisateurs.component.html'
  // Plus besoin de styleUrl si on utilise le CSS global !
})
export class ListeUtilisateursComponent implements OnInit {
  utilisateurs: any[] = [];
  
  private http = inject(HttpClient);
  public authService = inject(AuthService); // Public pour le HTML
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  ngOnInit() {
    this.chargerUtilisateurs();
  }

  chargerUtilisateurs() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/utilisateurs${isDjango ? '/' : ''}`;
    
    this.http.get<any>(url).subscribe({
      next: (res) => this.utilisateurs = res.results ? res.results : res,
      error: (err) => console.error("Erreur de chargement", err)
    });
  }

  supprimer(id: number) {
    if (confirm('Bannir définitivement cet utilisateur ?')) {
      const isDjango = this.authService.getBaseUrl().includes('8000');
      const url = `${this.authService.getBaseUrl()}/utilisateurs/${id}${isDjango ? '/' : ''}`;
      this.http.delete(url).subscribe(() => this.chargerUtilisateurs());
    }
  }
}