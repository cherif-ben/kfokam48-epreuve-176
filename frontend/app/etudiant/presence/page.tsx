"use client";

import { useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { useMarquerPresence } from "@/features/presence/hooks/useMarquerPresence";
import { FormulairePresence } from "@/features/presence/components/FormulairePresence";
import { Card } from "@/shared/components/Card";
import { Alert } from "@/shared/components/Alert";
import type { Etudiant, Promotion } from "@/shared/types";
import { useI18n } from "@/lib/i18n/I18nProvider";

/**
 * EF2/EF3/EF4 — l'étudiant choisit son nom (Q1 : pas de mot de passe) et saisit le code.
 * Les erreurs API (CODE_INCONNU, CODE_EXPIRE, DEJA_PRESENT) sont affichées telles quelles (F3).
 */
export default function PresencePage() {
  const { t } = useI18n();
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
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="animate-fade-in-up text-2xl font-bold tracking-tight">{t("presence.title")}</h1>

      <Card className="animate-fade-in-up">
        <div className="flex flex-col gap-4">
          <div>
            <label htmlFor="promotion" className="mb-1 block text-sm font-medium">
              {t("presence.myPromotion")}
            </label>
            <select
              id="promotion"
              value={promotionId}
              onChange={(e) => setPromotionId(Number(e.target.value))}
              className="w-full rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
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

      {loading && <p className="animate-pulse text-sm text-muted">{t("presence.sending")}</p>}

      {error && <Alert type="error" message={error.message} code={error.code} />}

      {presence && !error && (
        <Alert
          type="success"
          message={t("presence.success", { source: t(`source.${presence.source.toLowerCase()}` as "source.etudiant" | "source.formateur") })}
          code="PRESENCE_ENREGISTREE"
        />
      )}
    </div>
  );
}
