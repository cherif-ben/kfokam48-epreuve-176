"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { tableauApi } from "@/features/tableau/api/tableauApi";
import { Alert } from "@/shared/components/Alert";
import { SkeletonRow } from "@/shared/components/Skeleton";
import { formatMoyenne } from "@/lib/utils";
import { useI18n } from "@/lib/i18n/I18nProvider";
import type { ApiError, LigneTableau, Promotion } from "@/shared/types";

/**
 * EF12 / Q16 — tableau récapitulatif du formateur. Moyenne null affichée « — » (jamais 0),
 * valeur venant de l'API uniquement (F3). Squelettes pendant le chargement.
 */
export default function TableauPage() {
  const { t } = useI18n();
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

  const selectClass =
    "rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-6">
      <div className="animate-fade-in-up flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold tracking-tight">{t("tableau.title")}</h1>
        <select
          value={promotionId}
          onChange={(e) => {
            setPromotionId(Number(e.target.value));
            charger(Number(e.target.value));
          }}
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
        <div className="animate-fade-in overflow-hidden rounded-xl border border-line bg-surface shadow-sm">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-line bg-surface-muted">
                {[t("tableau.student"), t("tableau.presences"), t("tableau.submissions"), t("tableau.average"), t("tableau.pending")].map(
                  (h, i) => (
                    <th key={i} className="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-muted">
                      {h}
                    </th>
                  ),
                )}
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: 8 }).map((_, i) => (
                <SkeletonRow key={i} />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {error && <Alert type="error" message={error.message} code={error.code} />}

      {!chargement && !error && lignes.length > 0 && (
        <div className="animate-fade-in-up overflow-hidden rounded-xl border border-line bg-surface shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-line bg-surface-muted text-xs uppercase tracking-wide text-muted">
                  <th className="px-4 py-3 font-semibold">{t("tableau.student")}</th>
                  <th className="px-4 py-3 text-center font-semibold">{t("tableau.presences")}</th>
                  <th className="px-4 py-3 text-center font-semibold">{t("tableau.submissions")}</th>
                  <th className="px-4 py-3 text-center font-semibold">{t("tableau.average")}</th>
                  <th className="px-4 py-3 text-center font-semibold">{t("tableau.pending")}</th>
                </tr>
              </thead>
              <tbody>
                {lignes.map((l) => (
                  <tr
                    key={l.etudiantId}
                    className="border-b border-line/60 transition-colors last:border-0 hover:bg-surface-muted"
                  >
                    <td className="px-4 py-3 font-medium">{l.nom}</td>
                    <td className="px-4 py-3 text-center tabular-nums">{l.presences}</td>
                    <td className="px-4 py-3 text-center tabular-nums">{l.exercicesDeposes}</td>
                    <td className="px-4 py-3 text-center tabular-nums">
                      {l.moyenne === null ? (
                        <span className="text-muted">—</span>
                      ) : (
                        <span className="font-semibold">{formatMoyenne(l.moyenne)}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-center">
                      {l.relecturesEnAttente > 0 ? (
                        <span className="inline-flex min-w-6 justify-center rounded-full bg-warning-soft px-2 py-0.5 text-xs font-semibold text-warning">
                          {l.relecturesEnAttente}
                        </span>
                      ) : (
                        <span className="text-muted">0</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!chargement && !error && lignes.length === 0 && promotionId > 0 && (
        <p className="text-sm text-muted">{t("tableau.empty")}</p>
      )}
    </div>
  );
}
