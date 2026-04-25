import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ServeurService, BackendType } from './services/serveur.service';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private serveurService = inject(ServeurService);
  
  // On récupère le serveur actuel pour l'interface
  serveurActif: BackendType = this.serveurService.getBackend();

  changerServeur(backend: BackendType) {
    this.serveurService.setBackend(backend);
  }
}