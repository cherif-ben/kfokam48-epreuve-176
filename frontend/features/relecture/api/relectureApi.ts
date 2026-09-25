import type { Relecture } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const relectureApi = {
  /** EF8/EF9 — Le relecteur consulte les relectures qui lui sont assignées */
  async getRelectures(relecteurId: number, statut?: "EN_ATTENTE" | "RENDUE"): Promise<Relecture[]> {
    return apiFetch<Relecture[]>("/api/relectures", {
      params: { relecteurId, statut },
    });
  },

  /** EF9/EF10 — Le relecteur rend sa note et son commentaire */
  async rendreRelecture(
    id: number,
    note: number,
    commentaire: string,
  ): Promise<void> {
    return apiFetch<void>(`/api/relectures/${id}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ note, commentaire }),
    });
  },
};
