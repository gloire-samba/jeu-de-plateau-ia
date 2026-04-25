import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-utilisateur-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-utilisateur-form.component.html'
})
export class AdminUtilisateurFormComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  public router = inject(Router);
  private authService = inject(AuthService);

  isEditMode = false;
  userId: string | null = null;
  user: any = { pseudo: '', email: '', motDePasse: '', role: 'ROLE_USER' };

  ngOnInit() {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.isEditMode = true;
      this.chargerUser();
    }
  }

  chargerUser() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/utilisateurs/${this.userId}${isDjango ? '/' : ''}`;
    this.http.get<any>(url).subscribe(data => this.user = { ...data, motDePasse: '' });
  }

  enregistrer() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = this.isEditMode 
      ? `${this.authService.getBaseUrl()}/utilisateurs/${this.userId}${isDjango ? '/' : ''}`
      : `${this.authService.getBaseUrl()}/utilisateurs${isDjango ? '/' : ''}`;

    const payload = { ...this.user };
    if (this.isEditMode && !payload.motDePasse) delete payload.motDePasse;

    const request = this.isEditMode ? this.http.put(url, payload) : this.http.post(url, payload);
    request.subscribe(() => this.router.navigate(['/utilisateurs']));
  }
}