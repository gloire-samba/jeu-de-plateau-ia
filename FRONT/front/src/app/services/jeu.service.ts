import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ServeurService } from './serveur.service';
import { EtatPartieDto, QuestionDto, ReponseTourDto } from '../models/model-dto';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class JeuService {
  private http = inject(HttpClient);
  private serveurService = inject(ServeurService);

  private get baseUrl(): string {
    const backend = this.serveurService.getBackend();
    return `${environment.urls[backend]}/jeu`;
  }

  private get plateauUrl(): string {
    const backend = this.serveurService.getBackend();
    return `${environment.urls[backend]}/plateau`;
  }

  demarrerPartie(payload: any): Observable<any> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/demarrer`
      : `${this.baseUrl}/demarrer/`;
    return this.http.post(url, payload);
  }

  obtenirQuestion(partieId: number, joueurId: number): Observable<QuestionDto> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/question/${partieId}/${joueurId}`
      : `${this.baseUrl}/question/${partieId}/${joueurId}/`;
    return this.http.get<QuestionDto>(url);
  }

  repondreQuestion(partieId: number, joueurId: number, questionId: number, reponseJoueur: string): Observable<ReponseTourDto> {
    const payload = { partieId, joueurId, questionId, reponseJoueur };
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/repondre`
      : `${this.baseUrl}/repondre/`;
    return this.http.post<ReponseTourDto>(url, payload);
  }

  getPlateau(partieId: number): Observable<any> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.plateauUrl}/partie/${partieId}` 
      : `${this.plateauUrl}/?partie_id=${partieId}`;
    return this.http.get(url);
  }

  // 👉 CORRECTION : On utilise this.baseUrl et on gère la différence Spring/Django
  getEtatPartie(partieId: number): Observable<EtatPartieDto> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/etat/${partieId}`
      : `${this.baseUrl}/etat/${partieId}/`;
    return this.http.get<EtatPartieDto>(url);
  }

  appliquerEffet(lanceurId: number, cible1Id: number, cible2Id?: number): Observable<any> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/appliquer-effet`
      : `${this.baseUrl}/appliquer-effet/`;
      
    // Si cible2Id n'est pas fourni (malus normal), on ne l'envoie pas
    const payload = cible2Id ? { lanceurId, cible1Id, cible2Id } : { lanceurId, cible1Id };

    return this.http.post(url, payload);
  }

  // --- ROUTES SUPER BONUS ---
  preparerSuperBonus(partieId: number, joueurId: number, categorie: string): Observable<QuestionDto> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/preparer-super-bonus`
      : `${this.baseUrl}/preparer-super-bonus/`;
    return this.http.post<QuestionDto>(url, { partieId, joueurId, categorie });
  }

  repondreSuperBonus(payload: any): Observable<ReponseTourDto> {
    const url = this.serveurService.getBackend() === 'spring'
      ? `${this.baseUrl}/repondre-super-bonus`
      : `${this.baseUrl}/repondre-super-bonus/`;
    return this.http.post<ReponseTourDto>(url, payload);
  }
}