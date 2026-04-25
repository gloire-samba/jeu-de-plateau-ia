import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-historique',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-historique.component.html'
})
export class AdminHistoriqueComponent implements OnInit {
  historiques: any[] = [];
  private http = inject(HttpClient);
  public authService = inject(AuthService);
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  ngOnInit() {
    this.chargerHistorique();
  }

  chargerHistorique() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/historique${isDjango ? '/' : ''}`;
    
    this.http.get<any>(url).subscribe({
      next: (res) => this.historiques = res.results ? res.results : res,
      error: (err) => console.error("Accès refusé ou erreur", err)
    });
  }

  supprimer(id: number) {
    if (confirm('Supprimer cette entrée de l\'historique ?')) {
      const isDjango = this.authService.getBaseUrl().includes('8000');
      const url = `${this.authService.getBaseUrl()}/historique/${id}${isDjango ? '/' : ''}`;
      this.http.delete(url).subscribe(() => this.chargerHistorique());
    }
  }
}