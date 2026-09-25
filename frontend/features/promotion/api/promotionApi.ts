import type { Promotion } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const promotionApi = {
  /** Référentiel des promotions — alimente les sélecteurs (EF1, EF2, EF12). */
  async listerPromotions(): Promise<Promotion[]> {
    return apiFetch<Promotion[]>("/api/promotions");
  },
};
