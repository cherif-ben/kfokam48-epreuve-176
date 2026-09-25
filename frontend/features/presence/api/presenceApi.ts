import type { Presence } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const presenceApi = {
  /** EF2/EF3/EF4 — L'étudiant marque sa présence avec un code (source=ETUDIANT par défaut) */
  async marquerPresence(code: string, etudiantId: number): Promise<Presence> {
    return apiFetch<Presence>("/api/presences", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, etudiantId }),
    });
  },

  /** EF5 / RG11 (Q14) — Le formateur ajoute une présence manuellement (source=FORMATEUR) */
  async ajouterPresenceManuelle(sessionId: number, etudiantId: number): Promise<Presence> {
    return apiFetch<Presence>(`/api/sessions/${sessionId}/presences`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ etudiantId }),
    });
  },
};
