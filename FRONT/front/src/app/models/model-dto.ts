// models-jeu.ts

export interface CasePlateau {
  id?: number;
  partieId?: number;
  positionPlateau: number;
  effet: string;
  categorie: string;
  points: number;
}

export interface QuestionDto {
  questionId: number;
  texteQuestion: string;
  typeQuestion: string;
  propositions?: string[];
  indiceTexte?: string; //
}

export interface ResultatTour {
  victoire: boolean;
  effetEnAttente?: string;
  deuxiemeChance?: boolean;
  aDroitDeuxiemeChance?: boolean;
  messageEffet?: string;
}

export interface ReponseTourDto {
  etaitBonneReponse: boolean;
  valeurDe: number;
  nouvellePosition: number;
  resultatTour: ResultatTour;
  logsIA: string[];
  partieTerminee?: boolean; 
  nomVainqueur?: string;
}

export interface JoueurEtatDto {
  id: number;
  nom: string;
  position: number;
  estIa: boolean;
  statut: string;
  dureeEffet: number;
}

export interface EtatPartieDto {
  partieId: number;
  joueurs: JoueurEtatDto[];
}