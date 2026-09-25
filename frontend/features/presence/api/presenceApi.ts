import type { Presence } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const presenceApi = {
  /** EF2/EF3/EF4 — L'étudiant marque sa présence avec un code */
  async marquerPresence(
    code: string,
    etudiantId: number,
  ): Promise<Presence> {
    return apiFetch<Presence>("/api/presences", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, etudiantId }),
    });
  },
};
