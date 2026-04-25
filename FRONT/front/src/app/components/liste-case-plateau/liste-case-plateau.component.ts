import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-liste-case-plateau',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './liste-case-plateau.component.html'
})
export class ListeCasePlateauComponent implements OnInit {
  cases: any[] = [];
  private http = inject(HttpClient);
  public authService = inject(AuthService);
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  ngOnInit() {
    this.chargerCases();
  }

  chargerCases() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    // Note : On récupère toutes les cases existantes en base
    const url = `${this.authService.getBaseUrl()}/plateau${isDjango ? '/' : ''}`;
    
    this.http.get<any>(url).subscribe({
      next: (res) => this.cases = res.results ? res.results : res,
      error: (err) => console.error("Erreur de chargement des cases", err)
    });
  }

  supprimer(id: number) {
    if (confirm('Supprimer cette configuration de case ?')) {
      const isDjango = this.authService.getBaseUrl().includes('8000');
      const url = `${this.authService.getBaseUrl()}/plateau/${id}${isDjango ? '/' : ''}`;
      this.http.delete(url).subscribe(() => this.chargerCases());
    }
  }
}