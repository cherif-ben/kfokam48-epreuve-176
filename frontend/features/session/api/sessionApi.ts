import type { Session, SessionCloturee, SessionOuverte } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const sessionApi = {
  /** EF1 — Le formateur ouvre une session et obtient un code de présence */
  async ouvrirSession(
    titre: string,
    promotionId: number,
  ): Promise<SessionOuverte> {
    return apiFetch<SessionOuverte>("/api/sessions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ titre, promotionId }),
    });
  },

  /** Libre — Consulter le détail d'une session (titre, expiration, clôture) */
  async getSession(id: number): Promise<Session> {
    return apiFetch<Session>(`/api/sessions/${id}`);
  },

  /** EF11 / RG14 — Le formateur clôture une session */
  async cloturerSession(id: number): Promise<SessionCloturee> {
    return apiFetch<SessionCloturee>(`/api/sessions/${id}/cloture`, {
      method: "PATCH",
    });
  },

  /** EF5 / RG11 — Le formateur ajoute une présence manuellement */
  async ajouterPresence(
    id: number,
    etudiantId: number,
  ): Promise<{ id: number; sessionId: number; etudiantId: number; source: string }> {
    return apiFetch<{ id: number; sessionId: number; etudiantId: number; source: string }>(`/api/sessions/${id}/presences`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ etudiantId }),
    });
  },
};
