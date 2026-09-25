import type { Session, SessionCloturee, SessionOuverte } from "@/shared/types";
import { apiFetch } from "@/lib/api";

export const sessionApi = {
  /** EF1 — Le formateur ouvre une session et obtient un code de présence */
  async ouvrirSession(titre: string, promotionId: number): Promise<SessionOuverte> {
    return apiFetch<SessionOuverte>("/api/sessions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ titre, promotionId }),
    });
  },

  /** Détail d'une session (titre, expiration, clôture) */
  async getSession(id: number): Promise<Session> {
    return apiFetch<Session>(`/api/sessions/${id}`);
  },

  /** Liste des sessions, filtrable par promotion et/ou formateur */
  async getSessions(promotionId?: number): Promise<Session[]> {
    return apiFetch<Session[]>("/api/sessions", { params: { promotionId } });
  },

  /** EF11 / RG14 — Le formateur clôture une session (irréversible) */
  async cloturerSession(id: number): Promise<SessionCloturee> {
    return apiFetch<SessionCloturee>(`/api/sessions/${id}/cloture`, { method: "PATCH" });
  },
};
