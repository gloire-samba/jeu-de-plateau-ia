import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent {
  public authService = inject(AuthService); // Mis en public pour le HTML
  public router = inject(Router);

  logout() {
    this.authService.logout();
  }

  goTo(route: string) {
    this.router.navigate([route]);
  }
}