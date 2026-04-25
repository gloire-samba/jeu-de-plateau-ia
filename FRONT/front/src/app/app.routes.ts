import { Routes } from '@angular/router';

// --- COMPOSANTS DE BASE ---
import { LoginComponent } from './components/login/login.component';
import { LobbyComponent } from './components/lobby/lobby.component';
import { PlateauComponent } from './components/plateau/plateau.component';
import { ProfilComponent } from './components/profil/profil.component';

// --- COMPOSANTS DE LISTE / GESTION ---
import { ListePartiesComponent } from './components/liste-parties/liste-parties.component';
import { ListeUtilisateursComponent } from './components/liste-utilisateurs/liste-utilisateurs.component';
import { ListeCasePlateauComponent } from './components/liste-case-plateau/liste-case-plateau.component';
import { ListePartieJoueursComponent } from './components/liste-partie-joueurs/liste-partie-joueurs.component';

// --- COMPOSANTS STRICTEMENT ADMIN ---
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AdminQuestionsComponent } from './components/admin-questions/admin-questions.component';
import { AdminHistoriqueComponent } from './components/admin-historique/admin-historique.component';

// --- FORMULAIRES ADMIN ---
import { AdminQuestionFormComponent } from './components/admin-question-form/admin-question-form.component';
import { AdminUtilisateurFormComponent } from './components/admin-utilisateur-form/admin-utilisateur-form.component';
import { AdminPartieFormComponent } from './components/admin-partie-form/admin-partie-form.component';
import { AdminPartieJoueurFormComponent } from './components/admin-partie-joueur-form/admin-partie-joueur-form.component';
import { AdminCaseFormComponent } from './components/admin-case-form/admin-case-form.component';

// --- LES GARDIENS (SÉCURITÉ) ---
import { authGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  // 🛡️ ROUTES "MIXTES" (Lecture pour les joueurs connectés)
  { path: 'salon', component: LobbyComponent, canActivate: [authGuard] },
  { path: 'plateau/:id', component: PlateauComponent, canActivate: [authGuard] },
  { path: 'profil', component: ProfilComponent, canActivate: [authGuard] },
  
  { path: 'parties', component: ListePartiesComponent, canActivate: [authGuard] },
  { path: 'utilisateurs', component: ListeUtilisateursComponent, canActivate: [authGuard] },
  { path: 'cases', component: ListeCasePlateauComponent, canActivate: [authGuard] },
  { path: 'partie-joueurs', component: ListePartieJoueursComponent, canActivate: [authGuard] },

  // 👑 ROUTES "STRICTES" (Dashboards réservés à l'Admin)
  { path: 'admin', component: AdminDashboardComponent, canActivate: [adminGuard] },
  { path: 'admin/questions', component: AdminQuestionsComponent, canActivate: [adminGuard] },
  { path: 'admin/historique', component: AdminHistoriqueComponent, canActivate: [adminGuard] },

  // ✏️ FORMULAIRES ADMIN (Création / Modification - Réservés à l'Admin)
  { path: 'admin/questions/ajouter', component: AdminQuestionFormComponent, canActivate: [adminGuard] },
  { path: 'admin/questions/modifier/:id', component: AdminQuestionFormComponent, canActivate: [adminGuard] },
  
  { path: 'admin/utilisateurs/ajouter', component: AdminUtilisateurFormComponent, canActivate: [adminGuard] },
  { path: 'admin/utilisateurs/modifier/:id', component: AdminUtilisateurFormComponent, canActivate: [adminGuard] },

  { path: 'admin/parties/ajouter', component: AdminPartieFormComponent, canActivate: [adminGuard] },
  { path: 'admin/parties/modifier/:id', component: AdminPartieFormComponent, canActivate: [adminGuard] },

  { path: 'admin/cases/ajouter', component: AdminCaseFormComponent, canActivate: [adminGuard] },
  { path: 'admin/cases/modifier/:id', component: AdminCaseFormComponent, canActivate: [adminGuard] },

  { path: 'admin/partie-joueurs/ajouter', component: AdminPartieJoueurFormComponent, canActivate: [adminGuard] },
  { path: 'admin/partie-joueurs/modifier/:id', component: AdminPartieJoueurFormComponent, canActivate: [adminGuard] },

  // 🔄 REDIRECTIONS
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];