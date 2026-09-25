import type { Exercice, ExerciceComplet } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const exerciceApi = {
  /** EF6 — L'étudiant dépose le lien de son exercice */
  async deposerExercice(
    sessionId: number,
    etudiantId: number,
    lien: string,
  ): Promise<Exercice> {
    return apiFetch<Exercice>("/api/exercices", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sessionId, etudiantId, lien }),
    });
  },

  /** EF7 / RG10 — L'étudiant remplace le lien de son exercice */
  async remplacerExercice(id: number, lien: string): Promise<Exercice> {
    return apiFetch<Exercice>(`/api/exercices/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ lien }),
    });
  },

  /** RG6 — L'étudiant consulte ses propres exercices */
  async getExercices(etudiantId: number, sessionId?: number): Promise<ExerciceComplet[]> {
    return apiFetch<ExerciceComplet[]>("/api/exercices", {
      params: { etudiantId, sessionId },
    });
  },
};
