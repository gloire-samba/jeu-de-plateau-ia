import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-partie-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-partie-form.component.html'
})
export class AdminPartieFormComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  public router = inject(Router);
  private authService = inject(AuthService);

  isEditMode = false;
  partieId: string | null = null;
  partie: any = { statut: 'EN_COURS', tourActuel: 1 };

  ngOnInit() {
    this.partieId = this.route.snapshot.paramMap.get('id');
    if (this.partieId) {
      this.isEditMode = true;
      this.chargerPartie();
    }
  }

  chargerPartie() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/parties/${this.partieId}${isDjango ? '/' : ''}`;
    this.http.get<any>(url).subscribe(data => {
      this.partie = data;
      if (isDjango) this.partie.tourActuel = data.tour_actuel;
    });
  }

  enregistrer() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = this.isEditMode 
      ? `${this.authService.getBaseUrl()}/parties/${this.partieId}${isDjango ? '/' : ''}`
      : `${this.authService.getBaseUrl()}/parties${isDjango ? '/' : ''}`;

    const payload = { ...this.partie };
    if (isDjango) payload.tour_actuel = this.partie.tourActuel;

    const req = this.isEditMode ? this.http.patch(url, payload) : this.http.post(url, payload);
    req.subscribe(() => this.router.navigate(['/parties']));
  }
}