export interface Promotion {
  id: number;
  nom: string;
}

export interface Formateur {
  id: number;
  nom: string;
}

export interface Etudiant {
  id: number;
  nom: string;
  promotionId: number;
}

export type SourcePresence = "ETUDIANT" | "FORMATEUR";

export type StatutExercice = "DEPOSE" | "EN_ATTENTE_RELECTURE" | "RELU";

export type StatutRelecture = "EN_ATTENTE" | "RENDUE";

export interface Session {
  id: number;
  titre: string;
  promotionId: number;
  formateurId: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
  clotureAt: string | null;
}

export interface Presence {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: SourcePresence;
  createdAt: string;
}

export interface Exercice {
  id: number;
  sessionId: number;
  etudiantId: number;
  lien: string;
  statut: StatutExercice;
  createdAt: string;
  updatedAt: string;
}

export interface ExerciceComplet extends Exercice {
  note: number | null;
  commentaire: string | null;
}

export interface Relecture {
  id: number;
  exerciceId: number;
  lienExercice: string;
  statut: StatutRelecture;
  note: number | null;
  commentaire: string | null;
  relecteurId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

export interface ErreurApi {
  code: string;
  message: string;
}

export interface SessionOuverte {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface SessionCloturee {
  id: number;
  clotureAt: string;
}
