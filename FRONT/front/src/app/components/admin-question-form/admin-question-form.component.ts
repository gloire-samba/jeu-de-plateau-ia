import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-question-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-question-form.component.html'
})
export class AdminQuestionFormComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  public router = inject(Router);
  private authService = inject(AuthService);

  isEditMode = false;
  questionId: string | null = null;

  // Modèle de données (on gère les deux types de noms de champs)
  question: any = {
    categorie: 'HISTOIRE',
    texteQuestion: '', // Spring
    texte_question: '', // Django
    bonneReponse: '',  // Spring
    bonne_reponse: ''   // Django
  };

  categories = ["HISTOIRE", "GEOGRAPHIE", "SCIENCES", "DIVERTISSEMENT", "SPORT", "ARTS_ET_LITTERATURE"];

  ngOnInit() {
    this.questionId = this.route.snapshot.paramMap.get('id');
    if (this.questionId) {
      this.isEditMode = true;
      this.chargerQuestion();
    }
  }

  chargerQuestion() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const url = `${this.authService.getBaseUrl()}/questions/${this.questionId}${isDjango ? '/' : ''}`;
    this.http.get<any>(url).subscribe(data => {
      this.question = data;
      // On synchronise les champs pour éviter les problèmes entre Spring/Django
      if (isDjango) {
        this.question.texteQuestion = data.texte_question;
        this.question.bonneReponse = data.bonne_reponse;
      }
    });
  }

  enregistrer() {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    const urlBase = `${this.authService.getBaseUrl()}/questions`;
    const url = this.isEditMode ? `${urlBase}/${this.questionId}${isDjango ? '/' : ''}` : `${urlBase}${isDjango ? '/' : ''}`;

    // On prépare l'objet pour le backend spécifique
    const payload = { ...this.question };
    if (isDjango) {
      payload.texte_question = this.question.texteQuestion;
      payload.bonne_reponse = this.question.bonneReponse;
    }

    const request = this.isEditMode ? this.http.put(url, payload) : this.http.post(url, payload);

    request.subscribe({
      next: () => this.router.navigate(['/admin/questions']),
      error: (err) => alert("Erreur lors de l'enregistrement : " + err.message)
    });
  }
}