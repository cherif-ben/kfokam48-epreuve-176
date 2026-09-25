"use client";

import { useEffect, useState } from "react";
import { sessionApi } from "@/features/session/api/sessionApi";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import type { ApiError, Promotion, SessionOuverte } from "@/shared/types";
import { formatDate } from "@/lib/utils";

/**
 * EF1 / RG1 — le formateur ouvre une session : le code de présence s'affiche en grand
 * avec sa durée de validité (issue de l'API, jamais recalculée côté front) et un
 * bouton « copier ». États loading et error gérés (F3).
 */
export default function NouvelleSessionPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [titre, setTitre] = useState("");
  const [promotionId, setPromotionId] = useState(0);
  const [sessionOuverte, setSessionOuverte] = useState<SessionOuverte | null>(null);
  const [loading, setLoading] = useState(false);
  const [chargementPromotions, setChargementPromotions] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [copie, setCopie] = useState(false);

  useEffect(() => {
    promotionApi
      .listerPromotions()
      .then(setPromotions)
      .catch(() => setError({ code: "CHARGEMENT_IMPOSSIBLE", message: "Impossible de charger les promotions.", status: 0 }))
      .finally(() => setChargementPromotions(false));
  }, []);

  const ouvrir = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSessionOuverte(null);
    try {
      const session = await sessionApi.ouvrirSession(titre.trim(), promotionId);
      setSessionOuverte(session);
    } catch (err) {
      const apiErr = err as ApiError;
      setError({ code: apiErr.code || "ERREUR_INCONNU", message: apiErr.message, status: apiErr.status ?? 0 });
    } finally {
      setLoading(false);
    }
  };

  const copierCode = async () => {
    if (!sessionOuverte) return;
    try {
      await navigator.clipboard.writeText(sessionOuverte.code);
      setCopie(true);
      setTimeout(() => setCopie(false), 2000);
    } catch {
      setCopie(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 px-4 py-8">
      <h1 className="text-2xl font-semibold text-zinc-900">Ouvrir une session</h1>

      <Card title="Nouvelle session">
        <form onSubmit={ouvrir} className="flex flex-col gap-4">
          <div>
            <label htmlFor="titre" className="mb-1 block text-sm font-medium text-zinc-700">
              Titre de la session
            </label>
            <input
              id="titre"
              type="text"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              placeholder="Ex : Algorithmique — Séance 3"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            />
          </div>
          <div>
            <label htmlFor="promotion" className="mb-1 block text-sm font-medium text-zinc-700">
              Promotion
            </label>
            <select
              id="promotion"
              value={promotionId}
              onChange={(e) => setPromotionId(Number(e.target.value))}
              className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            >
              <option value={0}>
                {chargementPromotions ? "Chargement…" : "Sélectionner une promotion"}
              </option>
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nom}
                </option>
              ))}
            </select>
          </div>
          <Button type="submit" disabled={loading || !titre.trim() || !promotionId}>
            {loading ? "Ouverture…" : "Ouvrir la session"}
          </Button>
        </form>
      </Card>

      {error && <Alert type="error" message={error.message} code={error.code} />}

      {sessionOuverte && (
        <Card title="Session ouverte">
          <div className="flex flex-col items-center gap-3 py-4">
            <p className="text-sm text-zinc-500">Code de présence</p>
            {/* ENF1 : lisible et dictable — grand format, police mono. */}
            <p className="font-mono text-5xl font-bold tracking-[0.3em] text-zinc-900">
              {sessionOuverte.code}
            </p>
            <Button variant="secondary" onClick={copierCode} className="w-full sm:w-auto">
              {copie ? "✓ Copié" : "Copier le code"}
            </Button>
            <p className="text-center text-sm text-zinc-600">
              Valable jusqu&apos;à <strong>{formatDate(sessionOuverte.expirationAt)}</strong>{" "}
              (15 minutes après l&apos;ouverture — RG1).
            </p>
          </div>
        </Card>
      )}
    </div>
  );
}
