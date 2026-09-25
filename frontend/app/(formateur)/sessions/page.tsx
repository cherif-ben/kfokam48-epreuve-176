"use client";

import { useCallback, useEffect, useState } from "react";
import { sessionApi } from "@/features/session/api/sessionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { presenceApi } from "@/features/presence/api/presenceApi";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import type { ApiError, Etudiant, Promotion, Session } from "@/shared/types";
import { getStatutSession } from "@/features/session/hooks/useSessionStatus";
import { formatDate } from "@/lib/utils";

/**
 * EF11 / RG14 — clôturer une session (#34) avec confirmation explicite,
 * EF5 / RG11 — ajouter une présence manuellement (#33) avec marquage « ajouté par le formateur ».
 */
export default function SessionsPage() {
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
      setMessagePresence(`Présence de ${nom} ajoutée (marquée « ajouté par le formateur »).`);
    } catch (err) {
      const e = err as ApiError;
      setError({ code: e.code || "ERREUR_INCONNU", message: `${nom} : ${e.message}`, status: e.status ?? 0 });
    } finally {
      setChargementPresences(null);
    }
  };

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 px-4 py-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-zinc-900">Sessions</h1>
        <select
          value={promotionId}
          onChange={(e) => changerPromotion(Number(e.target.value))}
          className="rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm"
        >
          {promotions.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nom}
            </option>
          ))}
        </select>
      </div>

      {chargement && <p className="text-sm text-zinc-500">Chargement…</p>}
      {error && <Alert type="error" message={error.message} code={error.code} />}
      {messagePresence && <Alert type="success" message={messagePresence} />}

      {!chargement && sessions.length === 0 && (
        <p className="text-sm text-zinc-500">Aucune session pour cette promotion.</p>
      )}

      {sessions.map((session) => {
        const statut = getStatutSession(session);
        const encoreOuverte = statut === "ouverte" || statut === "expiree";
        return (
          <Card key={session.id} title={session.titre}>
            <div className="flex flex-col gap-4">
              <div className="flex flex-wrap items-center gap-x-6 gap-y-1 text-sm">
                <span className="text-zinc-500">
                  Code : <code className="font-mono text-zinc-900">{session.code}</code>
                </span>
                <span className="text-zinc-500">Ouverture : {formatDate(session.ouvertureAt)}</span>
                <span
                  className={
                    statut === "ouverte"
                      ? "font-medium text-green-600"
                      : statut === "expiree"
                        ? "font-medium text-orange-600"
                        : "font-medium text-zinc-600"
                  }
                >
                  {statut === "ouverte" ? "Ouverte" : statut === "expiree" ? "Expirée" : "Clôturée"}
                </span>
              </div>

              {session.clotureAt && (
                <p className="text-sm text-zinc-500">
                  Clôturée le {formatDate(session.clotureAt)} — présences, dépôts et relectures
                  verrouillés (RG14).
                </p>
              )}

              {/* EF11 / RG14 (#34) : confirmation explicite, action irréversible. */}
              {encoreOuverte && confirmation !== session.id && (
                <Button
                  variant="danger"
                  className="self-start"
                  onClick={() => setConfirmation(session.id)}
                >
                  Clôturer la session
                </Button>
              )}
              {confirmation === session.id && (
                <div className="flex flex-col gap-2 rounded-md border border-red-200 bg-red-50 p-3 sm:flex-row sm:items-center">
                  <p className="flex-1 text-sm text-red-800">
                    Clôturer cette session ? L&apos;action est <strong>irréversible</strong> :
                    plus aucune présence, dépôt ou relecture ne sera possible.
                  </p>
                  <div className="flex gap-2">
                    <Button variant="danger" onClick={() => cloturer(session.id)}>
                      Confirmer
                    </Button>
                    <Button variant="secondary" onClick={() => setConfirmation(null)}>
                      Annuler
                    </Button>
                  </div>
                </div>
              )}

              {/* EF5 / RG11 (#33) : ajout manuel — visible tant que la session n'est pas clôturée. */}
              {encoreOuverte && etudiants.length > 0 && (
                <details className="rounded-md border border-zinc-200 p-3">
                  <summary className="cursor-pointer text-sm font-medium text-zinc-700">
                    Ajouter une présence manuellement
                  </summary>
                  <ul className="mt-3 flex flex-col gap-2">
                    {etudiants.map((etudiant) => (
                      <li
                        key={etudiant.id}
                        className="flex items-center justify-between gap-2 text-sm"
                      >
                        <span>{etudiant.nom}</span>
                        <Button
                          variant="secondary"
                          disabled={chargementPresences === etudiant.id}
                          onClick={() =>
                            ajouterPresence(session.id, etudiant.id, etudiant.nom)
                          }
                        >
                          {chargementPresences === etudiant.id
                            ? "Ajout…"
                            : "Ajouter manuellement"}
                        </Button>
                      </li>
                    ))}
                  </ul>
                  <p className="mt-2 text-xs text-zinc-500">
                    La présence sera marquée « ajouté par le formateur » (Q14). Un étudiant déjà
                    présent sera refusé (409 DEJA_PRESENT).
                  </p>
                </details>
              )}
            </div>
          </Card>
        );
      })}
    </div>
  );
}
