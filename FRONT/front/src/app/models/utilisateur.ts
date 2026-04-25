export interface Utilisateur {
  id: number;
  pseudo: string;
  token?: string; // Le point d'interrogation signifie que c'est optionnel
}