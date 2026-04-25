import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-liste-parties',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './liste-parties.component.html',
  styleUrl: './liste-parties.component.css'
})
export class ListePartiesComponent implements OnInit {
  parties: any[] = [];
  
  private http = inject(HttpClient);
  public authService = inject(AuthService); // Public pour le HTML
  public router = inject(Router);

  ngOnInit() {
    this.chargerParties();
  }

  chargerParties() {
    // Si Django, on ajoute un slash à la fin
    const url = `${this.authService.getBaseUrl()}/parties${this.authService.getBaseUrl().includes('8000') ? '/' : ''}`;
    this.http.get<any>(url).subscribe(res => {
      // Django renvoie parfois dans 'results' à cause de la pagination, Spring renvoie directement la liste
      this.parties = res.results ? res.results : res;
    });
  }

  supprimer(id: number) {
    if (confirm('Voulez-vous vraiment supprimer cette partie ?')) {
      const url = `${this.authService.getBaseUrl()}/parties/${id}${this.authService.getBaseUrl().includes('8000') ? '/' : ''}`;
      this.http.delete(url).subscribe(() => {
        this.chargerParties(); // On recharge la liste après suppression
      });
    }
  }

  retour() {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/salon']);
    }
  }
}