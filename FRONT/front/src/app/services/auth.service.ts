import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, tap } from 'rxjs';
import { ServeurService } from './serveur.service';
import { environment } from '../../environments/environment.development';

export interface LoginResponse {
  token: string;
  role: string;
  pseudo: string;
  utilisateurId: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private serveurService = inject(ServeurService); // 👉 INJECTION DU GESTIONNAIRE DE SERVEUR

  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private getUserFromStorage(): LoginResponse | null {
    const data = localStorage.getItem('userSession');
    return data ? JSON.parse(data) : null;
  }

  // 👉 L'URL S'ADAPTE TOUTE SEULE AU SERVEUR ACTIF
  getBaseUrl(): string {
    const backend = this.serveurService.getBackend();
    return environment.urls[backend];
  }

  login(email: string, motDePasse: string) {
    const baseUrl = this.getBaseUrl();
    const backend = this.serveurService.getBackend();

    // 👉 GESTION INTELLIGENTE DU SLASH DE FIN POUR DJANGO
    const loginUrl = backend === 'django' 
      ? `${baseUrl}/auth/login/` 
      : `${baseUrl}/auth/login`;

    return this.http.post<LoginResponse>(loginUrl, { email, motDePasse }).pipe(
      tap(res => {
        localStorage.setItem('userSession', JSON.stringify(res));
        this.currentUserSubject.next(res);
      })
    );
  }

  logout() {
    localStorage.removeItem('userSession');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  getUserId(): number {
    return Number(this.currentUserSubject.value?.utilisateurId) || 0;
  }

  isAdmin(): boolean {
    return this.currentUserSubject.value?.role === 'ROLE_ADMIN';
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  // --- INSCRIPTION CLASSIQUE ---
  register(data: any) {
    const isDjango = this.getBaseUrl().includes('8000');
    // Django aime bien les slashs à la fin, Spring s'en fiche
    const url = `${this.getBaseUrl()}/auth/register${isDjango ? '/' : ''}`;
    return this.http.post(url, data);
  }

  // Méthode utilitaire pour sauvegarder la session après OAuth2 (Google/GitHub)
  sauvegarderSession(token: string, pseudo: string, role: string, utilisateurId: string) {
    const session = { token, role, pseudo, utilisateurId };
    localStorage.setItem('userSession', JSON.stringify(session));
    this.currentUserSubject.next(session); // Met à jour l'état global instantanément !
  }
}