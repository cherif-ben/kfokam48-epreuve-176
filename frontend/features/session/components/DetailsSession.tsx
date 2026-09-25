import type { SessionOuverte } from "@/shared/types";
import { formatDate } from "@/lib/utils";
import { getStatutSession } from "./useSessionStatus";
import type { Session } from "@/shared/types";

interface DetailsSessionProps {
  session: Session | null;
}

export function DetailsSession({ session }: DetailsSessionProps) {
  if (!session) {
    return <p className="text-sm text-zinc-500">Session introuvable.</p>;
  }

  const statut = getStatutSession(session);
  const statutLabel = {
    ouverte: "Ouverte",
    expiree: "Expirée",
    cloturee: "Clôturée",
  };

  return (
    <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
      <div>
        <span className="text-zinc-500">Titre :</span> {session.titre}
      </div>
      <div>
        <span className="text-zinc-500">Code :</span> <code className="font-mono">{session.code}</code>
      </div>
      <div>
        <span className="text-zinc-500">Ouverture :</span> {formatDate(session.ouvertureAt)}
      </div>
      <div>
        <span className="text-zinc-500">Expiration :</span> {formatDate(session.expirationAt)}
      </div>
      <div>
        <span className="text-zinc-500">Statut :</span>{" "}
        <span
          className={
            statut === "ouverte"
              ? "text-green-600 font-medium"
              : statut === "expiree"
                ? "text-orange-600 font-medium"
                : "text-zinc-600 font-medium"
          }
        >
          {statutLabel[statut]}
        </span>
      </div>
      {session.clotureAt && (
        <div>
          <span className="text-zinc-500">Clôturée le :</span> {formatDate(session.clotureAt)}
        </div>
      )}
    </div>
  );
}
