"use client";

import { useCallback, useEffect, useState } from "react";
import { sessionApi } from "@/features/session/api/sessionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { presenceApi } from "@/features/presence/api/presenceApi";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { SkeletonCard } from "@/shared/components/Skeleton";
import type { ApiError, Etudiant, Promotion, Session } from "@/shared/types";
import { getStatutSession } from "@/features/session/hooks/useSessionStatus";
import { formatDate } from "@/lib/utils";
import { useI18n } from "@/lib/i18n/I18nProvider";

/**
 * EF11 / RG14 — clôturer une session (confirmation explicite), EF5 / RG11 — ajout
 * manuel de présence. Badges de statut animés, confirmation inline animée.
 */
export default function SessionsPage() {
  const { t } = useI18n();
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState(0);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [chargement, setChargement] = useState(true);
  const [chargementPresences, setChargementPresences] = useState<number | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [messagePresence, setMessagePresence] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<number | null>(null);

  const chargerSessions = useCallback(async (pid: number) => {
    setChargement(true);
    setError(null);
    try {
      const liste = await sessionApi.getSessions(pid || undefined);
      setSessions(liste);
    } catch (err) {
      const e = err as ApiError;
      setError({ code: e.code || "ERREUR_INCONNU", message: e.message, status: e.status ?? 0 });
    } finally {
      setChargement(false);
    }
  }, []);

  useEffect(() => {
    promotionApi
      .listerPromotions()
      .then((p) => {
        setPromotions(p);
        if (p.length > 0) {
          setPromotionId(p[0].id);
          chargerSessions(p[0].id);
          etudiantApi.listerParPromotion(p[0].id).then(setEtudiants).catch(() => setEtudiants([]));
        } else {
          setChargement(false);
        }
      })
      .catch(() => setChargement(false));
  }, [chargerSessions]);

  const changerPromotion = (pid: number) => {
    setPromotionId(pid);
    chargerSessions(pid);
    etudiantApi.listerParPromotion(pid).then(setEtudiants).catch(() => setEtudiants([]));
  };

  const cloturer = async (id: number) => {
    setConfirmation(null);
    setError(null);
    try {
      await sessionApi.cloturerSession(id);
      await chargerSessions(promotionId);
    } catch (err) {
      const e = err as ApiError;
      setError({ code: e.code || "ERREUR_INCONNU", message: e.message, status: e.status ?? 0 });
    }
  };

  const ajouterPresence = async (sessionId: number, etudiantId: number, nom: string) => {
    setChargementPresences(etudiantId);
    setError(null);
    setMessagePresence(null);
    try {
      await presenceApi.ajouterPresenceManuelle(sessionId, etudiantId);
      setMessagePresence(t("sessions.added", { nom }));
    } catch (err) {
      const e = err as ApiError;
      setError({
        code: e.code || "ERREUR_INCONNU",
        message: `${nom} : ${e.message}`,
        status: e.status ?? 0,
      });
    } finally {
      setChargementPresences(null);
    }
  };

  const badgeStatut = (statut: string) =>
    statut === "ouverte" ? (
      <Badge tone="success" live>{t("status.open")}</Badge>
    ) : statut === "expiree" ? (
      <Badge tone="warning">{t("status.expired")}</Badge>
    ) : (
      <Badge tone="neutral">{t("status.closed")}</Badge>
    );

  const selectClass =
    "rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6">
      <div className="animate-fade-in-up flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold tracking-tight">{t("sessions.title")}</h1>
        <select
          value={promotionId}
          onChange={(e) => changerPromotion(Number(e.target.value))}
          className={selectClass}
        >
          {promotions.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nom}
            </option>
          ))}
        </select>
      </div>

      {chargement && (
        <div className="flex flex-col gap-4">
          <SkeletonCard />
          <SkeletonCard />
        </div>
      )}
      {error && <Alert type="error" message={error.message} code={error.code} />}
      {messagePresence && <Alert type="success" message={messagePresence} />}

      {!chargement && sessions.length === 0 && (
        <p className="text-sm text-muted">{t("sessions.empty")}</p>
      )}

      <div className="stagger flex flex-col gap-4">
        {sessions.map((session) => {
          const statut = getStatutSession(session);
          const encoreOuverte = statut === "ouverte" || statut === "expiree";
          return (
            <Card key={session.id}>
              <div className="flex flex-col gap-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <h2 className="text-lg font-semibold tracking-tight">{session.titre}</h2>
                  {badgeStatut(statut)}
                </div>

                <div className="flex flex-wrap items-center gap-x-6 gap-y-1 text-sm text-muted">
                  <span>
                    {t("sessions.code")} :{" "}
                    <code className="rounded-md bg-surface-muted px-2 py-0.5 font-mono text-sm font-semibold tracking-widest text-foreground">
                      {session.code}
                    </code>
                  </span>
                  <span>
                    {t("sessions.openedAt")} : {formatDate(session.ouvertureAt)}
                  </span>
                </div>

                {session.clotureAt && (
                  <p className="text-sm text-muted">
                    {t("sessions.closedAt", { date: formatDate(session.clotureAt) })}
                  </p>
                )}

                {encoreOuverte && confirmation !== session.id && (
                  <Button
                    variant="danger"
                    className="self-start"
                    onClick={() => setConfirmation(session.id)}
                  >
                    {t("sessions.close")}
                  </Button>
                )}
                {confirmation === session.id && (
                  <div className="animate-scale-in flex flex-col gap-3 rounded-lg border border-danger/30 bg-danger-soft p-4 sm:flex-row sm:items-center">
                    <p className="flex-1 text-sm text-danger">
                      {t("sessions.closeWarning")}
                    </p>
                    <div className="flex gap-2">
                      <Button variant="danger" onClick={() => cloturer(session.id)}>
                        {t("common.confirm")}
                      </Button>
                      <Button variant="secondary" onClick={() => setConfirmation(null)}>
                        {t("common.cancel")}
                      </Button>
                    </div>
                  </div>
                )}

                {encoreOuverte && etudiants.length > 0 && (
                  <details className="group rounded-lg border border-line">
                    <summary className="cursor-pointer select-none px-3 py-2.5 text-sm font-medium text-foreground transition-colors hover:text-accent">
                      {t("sessions.addAttendance")}
                    </summary>
                    <ul className="mt-1 flex max-h-72 flex-col gap-1 overflow-y-auto border-t border-line px-3 py-3">
                      {etudiants.map((etudiant) => (
                        <li
                          key={etudiant.id}
                          className="flex items-center justify-between gap-2 rounded-md px-2 py-1.5 text-sm transition-colors hover:bg-surface-muted"
                        >
                          <span>{etudiant.nom}</span>
                          <Button
                            variant="secondary"
                            className="px-2.5 py-1 text-xs"
                            disabled={chargementPresences === etudiant.id}
                            onClick={() =>
                              ajouterPresence(session.id, etudiant.id, etudiant.nom)
                            }
                          >
                            {chargementPresences === etudiant.id
                              ? t("sessions.adding")
                              : t("sessions.add")}
                          </Button>
                        </li>
                      ))}
                    </ul>
                    <p className="px-3 pb-3 text-xs text-muted">{t("sessions.addNote")}</p>
                  </details>
                )}
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
