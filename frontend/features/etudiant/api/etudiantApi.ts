import type { Etudiant } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const etudiantApi = {
  /** Liste des étudiants d'une promotion — alimente les sélecteurs (EF2, EF5, Q1). */
  async listerParPromotion(promotionId: number): Promise<Etudiant[]> {
    return apiFetch<Etudiant[]>("/api/etudiants", { params: { promotionId } });
  },
};
