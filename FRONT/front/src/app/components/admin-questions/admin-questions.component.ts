import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-questions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-questions.component.html'
})
export class AdminQuestionsComponent implements OnInit {
  questions: any[] = [];
  
  private http = inject(HttpClient);
  public authService = inject(AuthService);
  public router = inject(Router); // 👉 INJECTION AJOUTÉE ICI !

  ngOnInit() {
    this.chargerQuestions();
  }

  chargerQuestions() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/questions${isDjango ? '/' : ''}`;
    
    this.http.get<any>(url).subscribe({
      next: (res) => this.questions = res.results ? res.results : res,
      error: (err) => console.error("Accès refusé ou erreur serveur", err)
    });
  }

  supprimer(id: number) {
    if (confirm('Supprimer cette question de la base de données ?')) {
      const isDjango = this.authService.getBaseUrl().includes('8000');
      const url = `${this.authService.getBaseUrl()}/questions/${id}${isDjango ? '/' : ''}`;
      this.http.delete(url).subscribe(() => this.chargerQuestions());
    }
  }
}