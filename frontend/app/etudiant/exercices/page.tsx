"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { sessionApi } from "@/features/session/api/sessionApi";
import { exerciceApi } from "@/features/exercice/api/exerciceApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import type { ApiError, Etudiant, ExerciceComplet, Promotion, Session } from "@/shared/types";
import { getStatutSession } from "@/features/session/hooks/useSessionStatus";

/**
 * EF6 / RG9 — déposer le lien de son exercice (#28) et EF7 / RG10 — remplacer le lien tant que
 * la relecture n'a pas commencé (#29). Aucune validation locale de l'URL (F3) : le backend
 * renvoie LIEN_INVALIDE ; le bouton « Modifier le lien » n'apparaît que si statut ≠ RELU.
 */
export default function ExercicesPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState(0);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [etudiantId, setEtudiantId] = useState(0);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [sessionId, setSessionId] = useState(0);
  const [lien, setLien] = useState("");
  const [exercices, setExercices] = useState<ExerciceComplet[]>([]);
  const [enEdition, setEnEdition] = useState<number | null>(null);
  const [nouveauLien, setNouveauLien] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  const chargerExercices = useCallback(async (eid: number) => {
    if (!eid) return;
    try {
      setExercices(await exerciceApi.getExercices(eid));
    } catch {
      setExercices([]);
    }
  }, []);

  useEffect(() => {
    promotionApi
      .listerPromotions()
      .then((p) => {
        setPromotions(p);
        if (p.length > 0) setPromotionId(p[0].id);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!promotionId) return;
    etudiantApi.listerParPromotion(promotionId).then(setEtudiants).catch(() => setEtudiants([]));
    sessionApi
      .getSessions(promotionId)
      .then((s) => {
        // Le sélecteur masque les sessions clôturées (RG14).
        setSessions(s.filter((x) => getStatutSession(x) !== "cloturee"));
      })
      .catch(() => setSessions([]));
  }, [promotionId]);

  const deposer = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSucces(null);
    try {
      const exercice = await exerciceApi.deposerExercice(sessionId, etudiantId, lien.trim());
      setSucces(`Exercice déposé — statut : ${exercice.statut}`);
      setLien("");
      await chargerExercices(etudiantId);
    } catch (err) {
      const e2 = err as ApiError;
      setError({ code: e2.code || "ERREUR_INCONNU", message: e2.message, status: e2.status ?? 0 });
    } finally {
      setLoading(false);
    }
  };

  const remplacerLien = async (exerciceId: number) => {
    setLoading(true);
    setError(null);
    setSucces(null);
    try {
      await exerciceApi.remplacerExercice(exerciceId, nouveauLien.trim());
      setSucces("Lien remplacé.");
      setEnEdition(null);
      setNouveauLien("");
      await chargerExercices(etudiantId);
    } catch (err) {
      const e2 = err as ApiError;
      setError({ code: e2.code || "ERREUR_INCONNU", message: e2.message, status: e2.status ?? 0 });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 px-4 py-8">
      <h1 className="text-2xl font-semibold text-zinc-900">Mes exercices</h1>

      <Card title="Déposer mon exercice">
        <form onSubmit={deposer} className="flex flex-col gap-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label htmlFor="promotion" className="mb-1 block text-sm font-medium text-zinc-700">
                Promotion
              </label>
              <select
                id="promotion"
                value={promotionId}
                onChange={(e) => setPromotionId(Number(e.target.value))}
                className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm"
              >
                {promotions.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nom}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="etudiant" className="mb-1 block text-sm font-medium text-zinc-700">
                Mon nom
              </label>
              <select
                id="etudiant"
                value={etudiantId}
                onChange={(e) => setEtudiantId(Number(e.target.value))}
                className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm"
                required
              >
                <option value={0}>Sélectionner…</option>
                {etudiants.map((et) => (
                  <option key={et.id} value={et.id}>
                    {et.nom}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label htmlFor="session" className="mb-1 block text-sm font-medium text-zinc-700">
              Session (ouvertes uniquement)
            </label>
            <select
              id="session"
              value={sessionId}
              onChange={(e) => setSessionId(Number(e.target.value))}
              className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm"
              required
            >
              <option value={0}>Sélectionner…</option>
              {sessions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.titre}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="lien" className="mb-1 block text-sm font-medium text-zinc-700">
              Lien de l&apos;exercice
            </label>
            <input
              id="lien"
              type="url"
              value={lien}
              onChange={(e) => setLien(e.target.value)}
              placeholder="https://github.com/…"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            />
          </div>
          <Button type="submit" disabled={loading || !sessionId || !etudiantId || !lien.trim()}>
            {loading ? "Dépôt…" : "Déposer"}
          </Button>
        </form>
      </Card>

      {error && <Alert type="error" message={error.message} code={error.code} />}
      {succes && <Alert type="success" message={succes} />}

      <Card title="Mes dépôts">
        {!etudiantId && <p className="text-sm text-zinc-500">Sélectionnez votre nom ci-dessus.</p>}
        {etudiantId && exercices.length === 0 && (
          <p className="text-sm text-zinc-500">Aucun exercice déposé pour le moment.</p>
        )}
        <ul className="flex flex-col gap-3">
          {exercices.map((ex) => (
            <li key={ex.id} className="rounded-md border border-zinc-200 p-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="min-w-0">
                  <a
                    href={ex.lien}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="block truncate text-sm font-medium text-blue-600 hover:underline"
                  >
                    {ex.lien}
                  </a>
                  <p className="text-xs text-zinc-500">
                    Statut : <strong>{ex.statut}</strong>
                    {ex.note !== null && (
                      <>
                        {" "}
                        — Note : <strong>{ex.note}/20</strong>
                      </>
                    )}
                  </p>
                  {ex.commentaire && (
                    <p className="mt-1 text-sm text-zinc-600">« {ex.commentaire} »</p>
                  )}
                  {/* RG6 (Q8) : le nom du relecteur n'est jamais affiché. */}
                </div>
                {/* EF7 / RG10 (Q13) : modification possible tant que non relu — le backend tranche. */}
                {ex.statut !== "RELU" && enEdition !== ex.id && (
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setEnEdition(ex.id);
                      setNouveauLien(ex.lien);
                    }}
                  >
                    Modifier le lien
                  </Button>
                )}
              </div>
              {enEdition === ex.id && (
                <div className="mt-3 flex flex-col gap-2">
                  <input
                    type="url"
                    value={nouveauLien}
                    onChange={(e) => setNouveauLien(e.target.value)}
                    className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                  />
                  <div className="flex gap-2">
                    <Button
                      disabled={loading || !nouveauLien.trim()}
                      onClick={() => remplacerLien(ex.id)}
                    >
                      Confirmer le remplacement
                    </Button>
                    <Button variant="secondary" onClick={() => setEnEdition(null)}>
                      Annuler
                    </Button>
                  </div>
                </div>
              )}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
