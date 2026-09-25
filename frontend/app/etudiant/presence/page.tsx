"use client";

import { useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { useMarquerPresence } from "@/features/presence/hooks/useMarquerPresence";
import { FormulairePresence } from "@/features/presence/components/FormulairePresence";
import { Card } from "@/shared/components/Card";
import { Alert } from "@/shared/components/Alert";
import type { Etudiant, Promotion } from "@/shared/types";

/**
 * EF2/EF3/EF4 — l'étudiant choisit son nom (Q1 : pas de mot de passe) dans sa promotion et saisit
 * le code. Les erreurs CODE_INCONNU, CODE_EXPIRE (expiration OU clôture — RG1/RG14) et
 * DEJA_PRESENT (RG12) sont affichées avec l'objet { code, message } de l'API. Aucune vérification
 * locale d'expiration (F3) : c'est le backend qui tranche.
 */
export default function PresencePage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState(0);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const { presence, loading, error, marquerPresence } = useMarquerPresence();

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
    etudiantApi
      .listerParPromotion(promotionId)
      .then(setEtudiants)
      .catch(() => setEtudiants([]));
  }, [promotionId]);

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 px-4 py-8">
      <h1 className="text-2xl font-semibold text-zinc-900">Marquer ma présence</h1>

      <Card>
        <div className="flex flex-col gap-4">
          <div>
            <label htmlFor="promotion" className="mb-1 block text-sm font-medium text-zinc-700">
              Ma promotion
            </label>
            <select
              id="promotion"
              value={promotionId}
              onChange={(e) => setPromotionId(Number(e.target.value))}
              className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nom}
                </option>
              ))}
            </select>
          </div>

          <FormulairePresence
            etudiants={etudiants}
            disabled={loading}
            onSubmit={(code, etudiantId) => marquerPresence(code, etudiantId)}
          />
        </div>
      </Card>

      {loading && <p className="text-sm text-zinc-500">Envoi en cours…</p>}

      {error && <Alert type="error" message={error.message} code={error.code} />}

      {presence && !error && (
        <Alert
          type="success"
          message={`Présence enregistrée (source : ${presence.source}). À bientôt !`}
          code="PRESENCE_ENREGISTREE"
        />
      )}
    </div>
  );
}
