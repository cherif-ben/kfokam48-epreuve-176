import type { Session } from "@/shared/types";
import { formatDate } from "@/lib/utils";
import { getStatutSession, type StatutSession } from "../hooks/useSessionStatus";

interface DetailsSessionProps {
  session: Session | null;
}

const STATUT_LABELS: Record<StatutSession, string> = {
  ouverte: "Ouverte",
  expiree: "Expirée",
  cloturee: "Clôturée",
};

const STATUT_CLASSES: Record<StatutSession, string> = {
  ouverte: "text-green-600 font-medium",
  expiree: "text-orange-600 font-medium",
  cloturee: "text-zinc-600 font-medium",
};

export function DetailsSession({ session }: DetailsSessionProps) {
  if (!session) {
    return <p className="text-sm text-zinc-500">Session introuvable.</p>;
  }

  const statut = getStatutSession(session);

  return (
    <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
      <div>
        <span className="text-zinc-500">Titre :</span> {session.titre}
      </div>
      <div>
        <span className="text-zinc-500">Code :</span>{" "}
        <code className="font-mono">{session.code}</code>
      </div>
      <div>
        <span className="text-zinc-500">Ouverture :</span>{" "}
        {formatDate(session.ouvertureAt)}
      </div>
      <div>
        <span className="text-zinc-500">Expiration :</span>{" "}
        {formatDate(session.expirationAt)}
      </div>
      <div>
        <span className="text-zinc-500">Statut :</span>{" "}
        <span className={STATUT_CLASSES[statut]}>
          {STATUT_LABELS[statut]}
        </span>
      </div>
      {session.clotureAt && (
        <div>
          <span className="text-zinc-500">Clôturée le :</span>{" "}
          {formatDate(session.clotureAt)}
        </div>
      )}
    </div>
  );
}