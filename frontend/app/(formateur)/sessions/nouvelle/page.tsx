"use client";

import { useEffect, useState } from "react";
import { sessionApi } from "@/features/session/api/sessionApi";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import type { ApiError, Promotion, SessionOuverte } from "@/shared/types";
import { formatDate } from "@/lib/utils";
import { useI18n } from "@/lib/i18n/I18nProvider";

/**
 * EF1 / RG1 — ouverture de session : le code s'affiche en grand (police mono, dictable),
 * copie en un clic avec retour visuel animé.
 */
export default function NouvelleSessionPage() {
  const { t } = useI18n();
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
      .catch(() =>
        setError({ code: "CHARGEMENT_IMPOSSIBLE", message: t("nouvelle.loadError"), status: 0 }),
      )
      .finally(() => setChargementPromotions(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      setError({
        code: apiErr.code || "ERREUR_INCONNU",
        message: apiErr.message,
        status: apiErr.status ?? 0,
      });
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

  const inputClass =
    "w-full rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="animate-fade-in-up text-2xl font-bold tracking-tight">{t("nouvelle.title")}</h1>

      <Card title={t("nouvelle.cardTitle")} className="animate-fade-in-up">
        <form onSubmit={ouvrir} className="flex flex-col gap-4">
          <div>
            <label htmlFor="titre" className="mb-1 block text-sm font-medium">
              {t("form.title")}
            </label>
            <input
              id="titre"
              type="text"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              placeholder={t("form.titlePlaceholder")}
              className={inputClass}
              required
            />
          </div>
          <div>
            <label htmlFor="promotion" className="mb-1 block text-sm font-medium">
              {t("form.promotion")}
            </label>
            <select
              id="promotion"
              value={promotionId}
              onChange={(e) => setPromotionId(Number(e.target.value))}
              className={inputClass}
              required
            >
              <option value={0}>
                {chargementPromotions ? t("common.loading") : t("form.promotionPlaceholder")}
              </option>
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nom}
                </option>
              ))}
            </select>
          </div>
          <Button type="submit" disabled={loading || !titre.trim() || !promotionId}>
            {loading ? t("nouvelle.opening") : t("nouvelle.open")}
          </Button>
        </form>
      </Card>

      {error && <Alert type="error" message={error.message} code={error.code} />}

      {sessionOuverte && (
        <Card className="animate-scale-in border-accent/30">
          <div className="flex flex-col items-center gap-4 py-4">
            <p className="text-sm font-medium uppercase tracking-wider text-muted">
              {t("nouvelle.codeLabel")}
            </p>
            <p className="animate-scale-in rounded-xl bg-accent-soft px-8 py-4 font-mono text-5xl font-bold tracking-[0.3em] text-accent-strong dark:text-accent">
              {sessionOuverte.code}
            </p>
            <Button variant="secondary" onClick={copierCode} className="w-full sm:w-auto">
              {copie ? (
                <>
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    className="h-4 w-4 text-success"
                  >
                    <path
                      d="M20 6 9 17l-5-5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className="animate-draw-check"
                    />
                  </svg>
                  {t("nouvelle.copied")}
                </>
              ) : (
                t("nouvelle.copy")
              )}
            </Button>
            <p className="text-center text-sm text-muted">
              {t("nouvelle.validUntil", { date: formatDate(sessionOuverte.expirationAt) })}
            </p>
          </div>
        </Card>
      )}
    </div>
  );
}
