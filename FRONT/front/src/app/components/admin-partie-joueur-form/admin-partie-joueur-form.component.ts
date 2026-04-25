import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-partie-joueur-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-partie-joueur-form.component.html'
})
export class AdminPartieJoueurFormComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  public router = inject(Router);
  private authService = inject(AuthService);

  isEditMode = false;
  pjId: string | null = null;
  pj: any = { positionPlateau: 0, effetActif: 'AUCUN', dureeEffet: 0 };

  ngOnInit() {
    this.pjId = this.route.snapshot.paramMap.get('id');
    if (this.pjId) {
      this.isEditMode = true;
      this.chargerPJ();
    }
  }

  chargerPJ() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/partie-joueurs/${this.pjId}${isDjango ? '/' : ''}`;
    this.http.get<any>(url).subscribe(data => {
      this.pj = data;
      if (isDjango) {
        this.pj.positionPlateau = data.position_plateau;
        this.pj.effetActif = data.effet_actif;
        this.pj.dureeEffet = data.duree_effet;
      }
    });
  }

  enregistrer() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = this.isEditMode 
      ? `${this.authService.getBaseUrl()}/partie-joueurs/${this.pjId}${isDjango ? '/' : ''}`
      : `${this.authService.getBaseUrl()}/partie-joueurs${isDjango ? '/' : ''}`;

    const payload = { ...this.pj };
    if (isDjango) {
      payload.position_plateau = this.pj.positionPlateau;
      payload.effet_actif = this.pj.effetActif;
      payload.duree_effet = this.pj.dureeEffet;
    }

    const req = this.isEditMode ? this.http.patch(url, payload) : this.http.post(url, payload);
    req.subscribe(() => this.router.navigate(['/partie-joueurs']));
  }
}