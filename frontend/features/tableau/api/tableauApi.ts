import type { LigneTableau } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const tableauApi = {
  /** EF12 / Q16 — Tableau récapitulatif du formateur (moyenne nullable : « — » si null) */
  async getTableau(promotionId: number): Promise<LigneTableau[]> {
    return apiFetch<LigneTableau[]>("/api/tableau", { params: { promotionId } });
  },
};
