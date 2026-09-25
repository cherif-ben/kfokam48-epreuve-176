import type { Relecture, StatutRelecture } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const relectureApi = {
  /** EF8/EF9 — Le relecteur consulte les relectures qui lui sont assignées */
  async getRelectures(relecteurId: number, statut?: StatutRelecture): Promise<Relecture[]> {
    return apiFetch<Relecture[]>("/api/relectures", { params: { relecteurId, statut } });
  },

  /** EF9/EF10 — Le relecteur rend (puis corrige, RG11/Q10) sa note et son commentaire */
  async rendreRelecture(
    id: number,
    relecteurId: number,
    note: number,
    commentaire: string,
  ): Promise<Relecture> {
    return apiFetch<Relecture>(`/api/relectures/${id}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ relecteurId, note, commentaire }),
    });
  },
};
