import type { Session } from "@/shared/types";
import { isExpired } from "@/lib/utils";

export type StatutSession = "ouverte" | "expiree" | "cloturee";

export function getStatutSession(session: Session): StatutSession {
  if (session.clotureAt) return "cloturee";
  if (isExpired(session.expirationAt)) return "expiree";
  return "ouverte";
}
