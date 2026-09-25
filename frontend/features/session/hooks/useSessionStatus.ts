import type { Session } from "@/shared/types";
import { isExpired } from "@/lib/utils";

export function getStatutSession(session: Session): "ouverte" | "expiree" | "cloturee" {
  if (session.clotureAt) return "cloturee";
  if (isExpired(session.expirationAt)) return "expiree";
  return "ouverte";
}
