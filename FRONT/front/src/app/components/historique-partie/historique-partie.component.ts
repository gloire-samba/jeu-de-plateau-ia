import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ServeurService } from '../../services/serveur.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-historique-partie',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './historique-partie.component.html',
  styleUrl: './historique-partie.component.css'
})
export class HistoriquePartieComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  http = inject(HttpClient);
  serveurService = inject(ServeurService);
  authService = inject(AuthService);

  partie: any = null;

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    const baseUrl = this.authService.getBaseUrl();
    
    // On appelle l'API standard pour récupérer les infos de la partie
    this.http.get(`${baseUrl}/parties/${id}`).subscribe({
      next: (res) => this.partie = res,
      error: (err) => console.error("Erreur lors du chargement de la partie", err)
    });
  }

  retour() {
    // Retour à la liste précédente (ajuste si besoin selon la provenance)
    this.router.navigate(['/parties']);
  }
}