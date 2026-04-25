import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ServeurService } from '../../services/serveur.service'; // 👉 NOUVEL IMPORT

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  public authService = inject(AuthService);
  public serveurService = inject(ServeurService); // 👉 INJECTION POUR LE HTML
  public router = inject(Router);
  public route = inject(ActivatedRoute); 
    // À l'intérieur de ta classe LoginComponent, ajoute :
  hidePassword: boolean = true;

  isLoginMode = true; 
  
  formData: any = {
    pseudo: '',
    email: '',
    motDePasse: ''
  };

  messageErreur: string = '';
  messageSucces: string = '';

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        // 👉 CORRECTION : On lit les vraies valeurs envoyées par Spring/Django
        const token = params['token'];
        const id = params['id'] || '0';
        const pseudo = params['pseudo'] ? decodeURIComponent(params['pseudo']) : 'Joueur_Social';
        const role = params['role'] || 'ROLE_USER';

        this.authService.sauvegarderSession(token, pseudo, role, id);
        this.router.navigate(['/salon']);
      }
      if (params['error']) {
        this.messageErreur = "Erreur lors de la connexion via le réseau social.";
      }
    });
  }

  basculerMode() {
    this.isLoginMode = !this.isLoginMode;
    this.messageErreur = '';
    this.messageSucces = '';
  }

  onSubmit() {
    this.messageErreur = '';
    this.messageSucces = '';

    if (this.isLoginMode) {
      // --- CONNEXION ---
      this.authService.login(this.formData.email, this.formData.motDePasse).subscribe({
        next: (res) => {
          // 👉 LA MAGIE EST ICI : On redirige selon le rôle !
          if (res.role === 'ROLE_ADMIN') {
            this.router.navigate(['/admin']); // L'admin va sur son Dashboard
          } else {
            this.router.navigate(['/salon']); // Les joueurs vont dans le Lobby
          }
        },
        error: (err) => {
          this.messageErreur = "Email ou mot de passe incorrect.";
        }
      });
    } else {
      // --- INSCRIPTION ---
      this.authService.register(this.formData).subscribe({
        next: () => {
          this.messageSucces = "Inscription réussie ! Vous pouvez maintenant vous connecter.";
          this.isLoginMode = true; 
          this.formData.motDePasse = ''; 
        },
        error: (err) => {
          this.messageErreur = err.error?.error || "Erreur lors de l'inscription.";
        }
      });
    }
  }

  loginAsAdmin() {
    // 1. Pré-remplit le formulaire (Mets ici les VRAIS identifiants de ton admin)
    this.formData.email = 'admin@demo.com'; // 👈 À MODIFIER avec l'email de ton admin
    this.formData.motDePasse = 'admin123'; // 👈 À MODIFIER avec le mot de passe de ton admin
    
    // 2. Lance la connexion comme si tu avais cliqué
    this.onSubmit();
  }

  connexionSociale(fournisseur: 'google' | 'github') {
    const isDjango = this.authService.getBaseUrl().includes('8000');
    if (isDjango) {
      window.location.href = `http://localhost:8000/api/auth/${fournisseur}/login/`;
    } else {
      window.location.href = `http://localhost:8080/oauth2/authorization/${fournisseur}`;
    }
  }

  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  
}