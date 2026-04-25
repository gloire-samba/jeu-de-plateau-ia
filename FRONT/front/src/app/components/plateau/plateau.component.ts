import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { JeuService } from '../../services/jeu.service';
import { AuthService } from '../../services/auth.service';
import { CasePlateau, QuestionDto, JoueurEtatDto, ReponseTourDto } from '../../models/model-dto';

type JoueurVisuel = JoueurEtatDto & { positionVisuelle: number };

interface Alerte {
  id: number;
  message: string;
  type: 'success' | 'danger' | 'warning' | 'dark' | 'victoire-joueur' | 'victoire-bot';
}

@Component({
  selector: 'app-plateau',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './plateau.component.html',
  styleUrl: './plateau.component.css'
})
export class PlateauComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  public router = inject(Router);
  private jeuService = inject(JeuService);
  private authService = inject(AuthService);

  partieId!: number;
  monId!: number;

  modeSuperBonus: boolean = false;

  taillePlateau: number = 50; 
  casesGrille: number[] = []; 
  effetsMap: { [key: number]: CasePlateau } = {}; 

  joueursSurPlateau: JoueurVisuel[] = [];
  intervalRafraichissement: any; 
  intervalAnimation: any; 

  maPosition: number = 0;
  monAncienStatut: string = 'NORMAL';

  positionAvantRequete: number = 0;

  questionEnCours: QuestionDto | null = null;
  reponseSaisie: string = '';
  enAttenteServeur: boolean = false;
  
  derniereQuestionPosee: string = '';
  derniereReponseDonnee: string = '';
  
  effetEnAttente: string | null = null;
  adversaires: JoueurEtatDto[] = [];
  joueursEchangeables: JoueurEtatDto[] = []; 

  partieTerminee: boolean = false;
  vainqueur: string = '';

  alertes: Alerte[] = [];
  alerteIdCounter: number = 0;

  effetsSuperBonusDispos: string[] = [
    'SPRINT', 'RISQUE_EXPLOSIF', 'DEUXIEME_CHANCE', 'INDICE', 
    'CIBLE_RECUL', 'CIBLE_PASSE_TOUR', 'CIBLE_ECHANGE', 'CIBLE_PRESSION', 'PARI_MULTIPLICATEUR'
  ];

  sbCategorie: string = 'HISTOIRE';
  sbPoints: number = 3;
  sbEffet: string = 'SPRINT'; 
  
  pariChoisi: number = 1;

  valeurDe: number | null = null;
  deEnRotation: boolean = false;
  logs: string[] = [];

  tempsRestant: number = 0;
  timerInterval: any;
  aDroitDeuxiemeChance: boolean = false;

  ngOnInit(): void {
    this.partieId = Number(this.route.snapshot.paramMap.get('id'));
    const vraiId = this.route.snapshot.queryParamMap.get('jId');
    this.monId = vraiId ? Number(vraiId) : (this.authService.getUserId() || 1);

    if (!this.partieId) {
      this.router.navigate(['/salon']);
      return;
    }

    this.chargerPlateau();
    this.rafraichirEtatPartie(); 

    this.intervalRafraichissement = setInterval(() => {
      this.rafraichirEtatPartie();
    }, 2000);

    this.intervalAnimation = setInterval(() => {
      this.joueursSurPlateau.forEach(j => {
        if (j.positionVisuelle < j.position) {
          j.positionVisuelle++;
        } else if (j.positionVisuelle > j.position) {
          j.positionVisuelle--;
        }
      });
    }, 150);
  }

  ngOnDestroy(): void {
    if (this.intervalRafraichissement) clearInterval(this.intervalRafraichissement);
    if (this.intervalAnimation) clearInterval(this.intervalAnimation); 
    this.arreterChrono();
  }

  ajouterAlerte(message: string, type: 'success' | 'danger' | 'warning' | 'dark' | 'victoire-joueur' | 'victoire-bot', duree: number = 5000) {
    const id = this.alerteIdCounter++;
    this.alertes.push({ id, message, type });
    setTimeout(() => {
      this.alertes = this.alertes.filter(a => a.id !== id);
    }, duree);
  }

  demarrerChrono() {
    this.arreterChrono(); 
    const moi = this.joueursSurPlateau.find(j => j.id === this.monId);
    const statut = moi ? moi.statut : 'NORMAL';

    if (statut === 'BONUS_PRESSION') this.tempsRestant = 90;
    else if (statut === 'MALUS_PRESSION') this.tempsRestant = 30;
    else this.tempsRestant = 60;

    this.timerInterval = setInterval(() => {
      this.tempsRestant--;
      if (this.tempsRestant <= 0) {
        this.arreterChrono();
        this.ajouterLog('👤 Vous avez laissé le temps s\'écouler et passé votre tour.');
        this.passerTour(); 
      }
    }, 1000);
  }

  arreterChrono() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  chargerPlateau() {
    this.jeuService.getPlateau(this.partieId).subscribe({
      next: (data: any) => {
        const cases = data.results ? data.results : data;
        if (cases && cases.length > 0) {
          this.taillePlateau = Math.max(...cases.map((c: any) => c.positionPlateau || c.position_plateau)) + 1;
          cases.forEach((c: any) => {
            const pos = c.positionPlateau || c.position_plateau;
            this.effetsMap[pos] = {
              positionPlateau: pos,
              effet: c.typeEffet || c.effet || c.type_effet || 'AUCUN',
              categorie: c.categorie || null,
              points: c.points || 0
            } as CasePlateau;
          });
        }
        this.casesGrille = Array.from({ length: this.taillePlateau + 1 }, (_, i) => i);
      }
    });
  }

  rafraichirEtatPartie() {
    if (this.partieTerminee) return;

    this.jeuService.getEtatPartie(this.partieId).subscribe({
      next: (etat) => {
        const nouveauxJoueurs: JoueurVisuel[] = [];

        etat.joueurs.forEach(jServeur => {
          let jLocal = this.joueursSurPlateau.find(j => j.id === jServeur.id);
          if (jLocal) {
            jLocal.position = jServeur.position; 
            jLocal.statut = jServeur.statut;
            jLocal.dureeEffet = jServeur.dureeEffet;
            nouveauxJoueurs.push(jLocal);
          } else {
            nouveauxJoueurs.push({ ...jServeur, positionVisuelle: jServeur.position });
          }
        });

        this.joueursSurPlateau = nouveauxJoueurs;

        const moi = this.joueursSurPlateau.find(j => j.id === this.monId);
        if (moi) {
          if (this.monAncienStatut === 'BOUCLIER' && moi.statut !== 'BOUCLIER') {
            this.ajouterAlerte("🛡️ Votre bouclier a disparu ! Vous êtes de nouveau vulnérable.", 'dark');
          }
          if (moi.statut === 'MALUS_PASSE_TOUR' && this.monAncienStatut !== 'MALUS_PASSE_TOUR') {
            this.ajouterAlerte("🛑 Vous êtes sous l'effet Passe Tour ! Vous ne pouvez pas jouer ce tour.", 'dark', 6000);
          }
          this.monAncienStatut = moi.statut;
          this.maPosition = moi.position;
        }
      }
    });
  }

  getJoueursSurCase(position: number): JoueurVisuel[] {
    return this.joueursSurPlateau.filter(j => j.positionVisuelle === position);
  }

  tirerQuestion() {
    this.positionAvantRequete = this.maPosition; 
    this.aDroitDeuxiemeChance = false; 
    this.enAttenteServeur = true;
    this.questionEnCours = null;
    this.reponseSaisie = '';
    this.valeurDe = null;

    this.jeuService.obtenirQuestion(this.partieId, this.monId).subscribe({
      next: (q) => {
        this.questionEnCours = q;
        this.enAttenteServeur = false;
        this.demarrerChrono(); 
      },
      error: () => this.enAttenteServeur = false
    });
  }

  repondreQcm(proposition: string) {
    this.reponseSaisie = proposition;
    this.envoyerReponse();
  }

  passerTourMalus() {
    this.positionAvantRequete = this.maPosition;
    this.enAttenteServeur = true;
    this.jeuService.repondreQuestion(this.partieId, this.monId, -1, "PASSER_MALUS")
      .subscribe({
        next: (res) => this.gererRetourServeur(res),
        error: () => this.enAttenteServeur = false
      });
  }

  envoyerReponse() {
    if (!this.questionEnCours || (!this.reponseSaisie.trim() && this.reponseSaisie !== 'PASSER')) return;
    this.arreterChrono(); 
    
    this.positionAvantRequete = this.maPosition; 
    this.enAttenteServeur = true;

    this.derniereQuestionPosee = this.questionEnCours?.texteQuestion || '';
    this.derniereReponseDonnee = this.reponseSaisie === 'PASSER' ? 'Temps écoulé / Passé' : this.reponseSaisie;

    if (this.modeSuperBonus) {
      const payload = {
        partieId: this.partieId,
        joueurId: this.monId,
        questionId: this.questionEnCours.questionId,
        reponseJoueur: this.reponseSaisie,
        pointsChoisis: this.sbPoints,
        effetChoisi: this.sbEffet
      };

      this.jeuService.repondreSuperBonus(payload).subscribe({
        next: (res) => {
          // 👉 FIX SUPER BONUS : On force l'interface cible si le joueur a choisi une attaque
          const cibles = ['CIBLE_RECUL', 'CIBLE_PASSE_TOUR', 'CIBLE_ECHANGE', 'CIBLE_PRESSION'];
          if (cibles.includes(this.sbEffet)) {
            if (!res.resultatTour) res.resultatTour = {} as any;
            res.resultatTour.effetEnAttente = this.sbEffet;
          }
          this.gererRetourServeur(res);
        },
        error: () => this.enAttenteServeur = false
      });
      return; 
    }

    let reponseFinale = this.reponseSaisie;
    const effetActuel = this.effetsMap[this.maPosition] ? this.effetsMap[this.maPosition].effet : 'AUCUN';
    
    if (effetActuel === 'PARI_MULTIPLICATEUR' || this.monAncienStatut === 'PARI_MULTIPLICATEUR') {
      reponseFinale = this.pariChoisi + '_PARI_' + this.reponseSaisie;
    }

    this.jeuService.repondreQuestion(this.partieId, this.monId, this.questionEnCours.questionId, reponseFinale)
      .subscribe({
        next: (res) => this.gererRetourServeur(res),
        error: () => this.enAttenteServeur = false
      });
  }

  passerTour() {
    this.reponseSaisie = 'PASSER';
    this.envoyerReponse();
  }

  validerEffet(cible1Id: number, cible2Id?: number) {
    if (!cible1Id) return;

    if (this.effetEnAttente === 'CIBLE_ECHANGE') {
      if (!cible2Id) return;
      if (cible1Id === cible2Id) {
        this.ajouterAlerte('⚠️ Vous devez choisir deux joueurs différents !', 'warning');
        return;
      }
    }

    this.positionAvantRequete = this.maPosition;
    this.enAttenteServeur = true;

    this.jeuService.appliquerEffet(this.monId, cible1Id, cible2Id).subscribe({
      next: (res: ReponseTourDto) => {
        const cible1 = this.joueursSurPlateau.find(j => j.id === cible1Id)?.nom || 'un joueur';
        let msg = '';
        if (this.effetEnAttente === 'CIBLE_ECHANGE') {
            const cible2 = this.joueursSurPlateau.find(j => j.id === cible2Id)?.nom || 'un joueur';
            msg = `🔄 Échange de place avec ${cible2} !`;
        } else {
            msg = `🎯 Cible ${cible1} avec ${this.effetEnAttente} !`;
        }
        this.ajouterAlerte(msg, 'success');
        
        this.ajouterLog(`🎯 ACTION : ${msg}`);
        
        this.effetEnAttente = null; 
        this.enAttenteServeur = false;
        
        this.traiterLogsIAPostEffet(res);
        this.rafraichirEtatPartie(); 
      },
      error: () => this.enAttenteServeur = false
    });
  }

  lancerQuestionSuperBonus() {
    this.positionAvantRequete = this.maPosition;
    this.enAttenteServeur = true;
    this.jeuService.preparerSuperBonus(this.partieId, this.monId, this.sbCategorie).subscribe({
      next: (q) => {
        this.questionEnCours = q;
        this.modeSuperBonus = true; 
        this.effetEnAttente = null; 
        this.enAttenteServeur = false;
        this.demarrerChrono(); 
      },
      error: () => this.enAttenteServeur = false
    });
  }

  ajouterLog(msg: string) {
    this.logs.unshift(msg); 
  }

  gererRetourServeur(res: ReponseTourDto) {
    this.questionEnCours = null;
    this.reponseSaisie = '';
    this.enAttenteServeur = false;
    this.modeSuperBonus = false;

    if (res.valeurDe !== null && res.valeurDe > 0) {
      this.animerDe(res.valeurDe, () => this.finaliserTour(res));
    } else {
      this.finaliserTour(res);
    }
  }

  animerDe(vraieValeur: number, callback: () => void) {
    this.deEnRotation = true;

    let tours = 0;
    
    const interval = setInterval(() => {
      this.valeurDe = Math.floor(Math.random() * 6) + 1; 
      tours++;
      
      if (tours > 10) { 
        clearInterval(interval);
        this.valeurDe = vraieValeur; 
        this.deEnRotation = false;
        
        setTimeout(() => callback(), 500); 
      }
    }, 100);
  }

  finaliserTour(res: ReponseTourDto) {
    const anciennePosition = this.positionAvantRequete;
    this.valeurDe = res.valeurDe; 
    
    const posApresMonTour = (res as any).positionAvantBots !== undefined ? (res as any).positionAvantBots : res.nouvellePosition;
    
    this.maPosition = res.nouvellePosition;

    const moi = this.joueursSurPlateau.find(j => j.id === this.monId);
    if (moi) moi.position = res.nouvellePosition;
    
    const casesAvancees = Math.abs(posApresMonTour - anciennePosition); 
    const effetDepart = this.effetsMap[anciennePosition]?.effet;

    let msgAlerte = res.resultatTour?.messageEffet || '';
    let typeAlerte: 'success' | 'danger' | 'warning' | 'dark' = 'success';

    if (msgAlerte) {
      if (msgAlerte.includes('pas de cible') || msgAlerte.includes('immunisé')) {
        msgAlerte = `🛡️ Tout le monde est sous bouclier, vous ne pouvez cibler personne ! En compensation, vous recevez un bonus de Sprint et avancez de ${casesAvancees} cases !`;
        typeAlerte = 'success';
      } else if (msgAlerte.includes('EXPLOSION') && res.etaitBonneReponse) {
        msgAlerte = `💣 EXPLOSION ! Vous avancez de ${casesAvancees} cases et les autres reculent de 3 cases !`;
        typeAlerte = 'success';
      } else if (msgAlerte.includes('gardez le Bouclier') || msgAlerte.includes('gardez le bouclier')) {
        msgAlerte = `⚠️ Mauvaise réponse ! Mais vous gardez quand même votre bouclier pour le prochain tour.`;
        typeAlerte = 'warning';
      } else if (msgAlerte.includes('Bouclier activé') || msgAlerte.includes('gagnez un Bouclier')) {
        msgAlerte = `🛡️ Vous avez activé le Bouclier ! Vous êtes protégé jusqu'au prochain tour.`;
        typeAlerte = 'success';
      } else if (msgAlerte.includes('SPRINT')) {
        msgAlerte = `⚡ Sprint activé ! Vous avancez de ${casesAvancees} cases !`;
        typeAlerte = 'success';
      } else if (msgAlerte.includes('EXPLOSION') || msgAlerte.includes('recul') || msgAlerte.includes('ÉCHEC') || msgAlerte.includes('perdu')) {
        typeAlerte = 'danger';
      }
    }

    this.ajouterLog("--------------------------------------------------");
    this.ajouterLog("👤 TOUR DE : VOUS");
    
    if (this.monAncienStatut === 'MALUS_PASSE_TOUR' || msgAlerte.includes("Tour passé")) {
      this.ajouterLog("🛑 Vous avez passé votre tour à cause du malus.");
    } else {
      this.ajouterLog("❓ Q : " + this.derniereQuestionPosee);
      
      const bonneRep = (res as any).bonneReponse || ''; 

      if (effetDepart === 'PARI_MULTIPLICATEUR' || this.monAncienStatut === 'PARI_MULTIPLICATEUR') {
        this.ajouterLog(`💬 R : Pari de ${this.pariChoisi} -> ${res.etaitBonneReponse ? '✅ Gagné' : '❌ Perdu (' + bonneRep + ')'}`);
      } else {
        this.ajouterLog(`💬 R : ${this.derniereReponseDonnee} -> ${res.etaitBonneReponse ? '✅ Juste' : '❌ Faux (' + bonneRep + ')'}`);
      }

      let logMouv = `🎲 Dé: ${res.valeurDe || 0} | 📍 Case ${anciennePosition} ➡️ ${posApresMonTour}`;
      if (msgAlerte) logMouv += ` | ✨ ${msgAlerte}`;
      
      this.ajouterLog(logMouv);

      if (res.etaitBonneReponse) {
        const effetsSansMessageGenerique = ['SPRINT', 'RISQUE_EXPLOSIF', 'PARI_MULTIPLICATEUR'];
        if (res.valeurDe !== null && res.valeurDe > 0 && !effetsSansMessageGenerique.includes(effetDepart) && this.monAncienStatut !== 'PARI_MULTIPLICATEUR') {
           this.ajouterAlerte(`🎲 Dé : ${res.valeurDe}. Vous avancez de ${casesAvancees} case(s) !`, 'success', 4000);
        }
      } else if (!msgAlerte) {
        this.ajouterAlerte(`❌ Faux ! La bonne réponse était : ${bonneRep}`, 'danger', 6000);
      }
    }

    if (msgAlerte) {
      this.ajouterAlerte(msgAlerte, typeAlerte, 5000);
    }

    this.aDroitDeuxiemeChance = res.resultatTour?.aDroitDeuxiemeChance || false;
    this.rafraichirEtatPartie(); 

    if (res.resultatTour?.aDroitDeuxiemeChance) {
      this.ajouterAlerte(`✨ Mauvaise réponse, mais vous avez une seconde chance !`, 'success');
    }

    if (res.resultatTour?.effetEnAttente) {
      this.effetEnAttente = res.resultatTour.effetEnAttente;
      this.adversaires = this.joueursSurPlateau.filter(j => j.id !== this.monId && j.statut !== 'BOUCLIER');
      this.joueursEchangeables = this.joueursSurPlateau.filter(j => j.statut !== 'BOUCLIER');
    }

    this.traiterLogsIAPostEffet(res);
  }

  traiterLogsIAPostEffet(res: ReponseTourDto) {
    if (res.logsIA && res.logsIA.length > 0) {
      res.logsIA.forEach((log: string) => {
        this.ajouterLog(log);
        
        // 👉 CORRECTION ICI : Détection sans se baser sur l'émoji '🤖'
        if (log.includes('Vous') && (log.includes('Cible') || log.includes('Échange'))) {
          const parties = log.split('|');
          const action = parties[parties.length - 1].trim();
          this.ajouterAlerte(`⚠️ Aïe ! Un bot a joué : ${action}`, 'danger', 6000);
        }
        else if (log.includes('EXPLOSION') && log.includes('Les autres joueurs reculent')) {
          this.ajouterAlerte(`💣 BOUM ! Un bot a déclenché une explosion, tout le monde recule !`, 'danger', 7000);
        }
      });
    }

    if ((res as any).partieTerminee) {
      this.partieTerminee = true;
      this.vainqueur = (res as any).nomVainqueur || '';
      this.questionEnCours = null;
      this.effetEnAttente = null; 
      
      if (this.vainqueur === 'Vous') {
        this.ajouterAlerte('🏆 FÉLICITATIONS ! Vous avez remporté la partie !', 'victoire-joueur' as any, 10000);
      } else {
        this.ajouterAlerte(`💀 FIN DE PARTIE. ${this.vainqueur} a franchi la ligne d'arrivée...`, 'victoire-bot' as any, 10000);
      }
    }
  }

  retourMenu() {
    this.router.navigate(['/salon']); 
  }
}