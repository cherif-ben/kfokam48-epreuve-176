"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { tableauApi } from "@/features/tableau/api/tableauApi";
import { Alert } from "@/shared/components/Alert";
import { formatMoyenne } from "@/lib/utils";
import type { ApiError, LigneTableau, Promotion } from "@/shared/types";

/**
 * EF12 / Q16 — tableau récapitulatif du formateur : une ligne par étudiant (présences, dépôts,
 * moyenne, relectures en attente). Q16 : moyenne null affichée « — », jamais 0 — la valeur vient
 * de l'API, jamais recalculée côté front (F3). 404 PROMOTION_INCONNUE affiché si besoin.
 */
export default function TableauPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState(0);
  const [lignes, setLignes] = useState<LigneTableau[]>([]);
  const [chargement, setChargement] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  const charger = useCallback(async (pid: number) => {
    if (!pid) return;
    setChargement(true);
    setError(null);
    try {
      setLignes(await tableauApi.getTableau(pid));
    } catch (err) {
      const e = err as ApiError;
      setLignes([]);
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
          charger(p[0].id);
        } else {
          setChargement(false);
        }
      })
      .catch(() => setChargement(false));
  }, [charger]);

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-6 px-4 py-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-zinc-900">Tableau récapitulatif</h1>
        <select
          value={promotionId}
          onChange={(e) => {
            setPromotionId(Number(e.target.value));
            charger(Number(e.target.value));
          }}
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

      {!chargement && !error && lignes.length > 0 && (
        <div className="overflow-x-auto rounded-lg border border-zinc-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-zinc-200 bg-zinc-50 text-xs uppercase tracking-wide text-zinc-500">
                <th className="px-4 py-3">Étudiant</th>
                <th className="px-4 py-3 text-center">Présences</th>
                <th className="px-4 py-3 text-center">Exercices déposés</th>
                <th className="px-4 py-3 text-center">Moyenne</th>
                <th className="px-4 py-3 text-center">Relectures en attente</th>
              </tr>
            </thead>
            <tbody>
              {lignes.map((l) => (
                <tr key={l.etudiantId} className="border-b border-zinc-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-zinc-900">{l.nom}</td>
                  <td className="px-4 py-3 text-center">{l.presences}</td>
                  <td className="px-4 py-3 text-center">{l.exercicesDeposes}</td>
                  <td className="px-4 py-3 text-center">
                    {/* Q16 : moyenne null → « — », pas 0. */}
                    {l.moyenne === null ? "—" : formatMoyenne(l.moyenne)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    {l.relecturesEnAttente > 0 ? (
                      <span className="rounded-full bg-orange-100 px-2 py-0.5 text-xs font-medium text-orange-700">
                        {l.relecturesEnAttente}
                      </span>
                    ) : (
                      <span className="text-zinc-400">0</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!chargement && !error && lignes.length === 0 && promotionId > 0 && (
        <p className="text-sm text-zinc-500">Aucun étudiant dans cette promotion.</p>
      )}
    </div>
  );
}
