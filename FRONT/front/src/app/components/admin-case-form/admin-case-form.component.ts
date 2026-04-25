import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-case-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-case-form.component.html'
})
export class AdminCaseFormComponent implements OnInit {
  private http = inject(HttpClient);
  public route = inject(ActivatedRoute);
  public router = inject(Router);
  public authService = inject(AuthService);

  isEditMode = false;
  caseId: string | null = null;
  c: any = { partieId: null, positionPlateau: 0, effet: 'AUCUN', categorie: 'HISTOIRE', points: 3 };

  ngOnInit() {
    this.caseId = this.route.snapshot.paramMap.get('id');
    if (this.caseId) {
      this.isEditMode = true;
      this.chargerCase();
    }
  }

  chargerCase() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/plateau/${this.caseId}${isDjango ? '/' : ''}`;
    this.http.get<any>(url).subscribe(data => {
      this.c = data;
      if (isDjango) {
        this.c.positionPlateau = data.position_plateau;
        this.c.partieId = data.partie;
      } else {
        this.c.partieId = data.partieId;
      }
    });
  }

  enregistrer() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = this.isEditMode 
      ? `${this.authService.getBaseUrl()}/plateau/${this.caseId}${isDjango ? '/' : ''}`
      : `${this.authService.getBaseUrl()}/plateau${isDjango ? '/' : ''}`;

    const payload = { ...this.c };
    if (isDjango) {
      payload.position_plateau = this.c.positionPlateau;
      payload.partie = this.c.partieId;
    } else {
      payload.partieId = this.c.partieId;
    }

    const req = this.isEditMode ? this.http.put(url, payload) : this.http.post(url, payload);
    req.subscribe(() => this.router.navigate(['/cases']));
  }
}