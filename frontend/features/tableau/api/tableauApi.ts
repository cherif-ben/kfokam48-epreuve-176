import type { LigneTableau } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const tableauApi = {
  /** EF12 — Le tableau récapitulatif du formateur par promotion */
  async getTableau(promotionId: number): Promise<LigneTableau[]> {
    return apiFetch<LigneTableau[]>("/api/tableau", {
      params: { promotionId },
    });
  },
};
