"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { sessionApi } from "@/features/session/api/sessionApi";
import { exerciceApi } from "@/features/exercice/api/exerciceApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import type { ApiError, Etudiant, ExerciceComplet, Promotion, Session } from "@/shared/types";
import { getStatutSession } from "@/features/session/hooks/useSessionStatus";
import { useI18n } from "@/lib/i18n/I18nProvider";

/**
 * EF6 / RG9 — dépôt du lien d'exercice, EF7 / RG10 — remplacement tant que non relu.
 * Aucune validation locale d'URL (F3) : le backend renvoie LIEN_INVALIDE.
 */
export default function ExercicesPage() {
  const { t } = useI18n();
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
      setSucces(t("exercices.deposited", { statut: t(`status.${statutCle(exercice.statut)}` as "status.submitted") }));
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
      setSucces(t("exercices.replaced"));
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

  const inputClass =
    "w-full rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  const badgeStatutExercice = (statut: ExerciceComplet["statut"]) => {
    if (statut === "RELU") return <Badge tone="success">{t("status.reviewed")}</Badge>;
    if (statut === "EN_ATTENTE_RELECTURE") return <Badge tone="accent">{t("status.pendingReview")}</Badge>;
    return <Badge tone="neutral">{t("status.submitted")}</Badge>;
  };

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="animate-fade-in-up text-2xl font-bold tracking-tight">{t("exercices.title")}</h1>

      <Card title={t("exercices.submitCard")} className="animate-fade-in-up">
        <form onSubmit={deposer} className="flex flex-col gap-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label htmlFor="promotion" className="mb-1 block text-sm font-medium">
                {t("form.promotion")}
              </label>
              <select
                id="promotion"
                value={promotionId}
                onChange={(e) => setPromotionId(Number(e.target.value))}
                className={inputClass}
              >
                {promotions.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nom}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="etudiant" className="mb-1 block text-sm font-medium">
                {t("form.myName")}
              </label>
              <select
                id="etudiant"
                value={etudiantId}
                onChange={(e) => setEtudiantId(Number(e.target.value))}
                className={inputClass}
                required
              >
                <option value={0}>{t("common.select")}</option>
                {etudiants.map((et) => (
                  <option key={et.id} value={et.id}>
                    {et.nom}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label htmlFor="session" className="mb-1 block text-sm font-medium">
              {t("form.session")}
            </label>
            <select
              id="session"
              value={sessionId}
              onChange={(e) => setSessionId(Number(e.target.value))}
              className={inputClass}
              required
            >
              <option value={0}>{t("common.select")}</option>
              {sessions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.titre}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="lien" className="mb-1 block text-sm font-medium">
              {t("form.link")}
            </label>
            <input
              id="lien"
              type="url"
              value={lien}
              onChange={(e) => setLien(e.target.value)}
              placeholder="https://github.com/…"
              className={inputClass}
              required
            />
          </div>
          <Button type="submit" disabled={loading || !sessionId || !etudiantId || !lien.trim()}>
            {loading ? t("exercices.submitting") : t("exercices.submit")}
          </Button>
        </form>
      </Card>

      {error && <Alert type="error" message={error.message} code={error.code} />}
      {succes && <Alert type="success" message={succes} />}

      <Card title={t("exercices.mySubmissions")}>
        {!etudiantId && <p className="text-sm text-muted">{t("exercices.pickName")}</p>}
        {etudiantId && exercices.length === 0 && (
          <p className="text-sm text-muted">{t("exercices.empty")}</p>
        )}
        <ul className="stagger flex flex-col gap-3">
          {exercices.map((ex) => (
            <li
              key={ex.id}
              className="rounded-lg border border-line p-4 transition-all duration-300 hover:border-accent/30 hover:shadow-sm"
            >
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="min-w-0">
                  <a
                    href={ex.lien}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="block truncate text-sm font-medium text-accent-strong hover:underline dark:text-accent"
                  >
                    {ex.lien}
                  </a>
                  <div className="mt-1.5 flex flex-wrap items-center gap-2 text-xs text-muted">
                    {badgeStatutExercice(ex.statut)}
                    {ex.note !== null && (
                      <span className="rounded-full bg-accent-soft px-2 py-0.5 font-semibold text-accent-strong dark:text-accent">
                        {t("exercices.grade")} : {ex.note}/20
                      </span>
                    )}
                  </div>
                  {ex.commentaire && (
                    <p className="mt-2 border-l-2 border-line pl-3 text-sm italic text-muted">
                      « {ex.commentaire} »
                    </p>
                  )}
                  {/* RG6 (Q8) : le nom du relecteur n'est jamais affiché. */}
                </div>
                {/* EF7 / RG10 (Q13) : modification possible tant que non relu — le backend tranche. */}
                {ex.statut !== "RELU" && enEdition !== ex.id && (
                  <Button
                    variant="secondary"
                    className="px-2.5 py-1 text-xs"
                    onClick={() => {
                      setEnEdition(ex.id);
                      setNouveauLien(ex.lien);
                    }}
                  >
                    {t("exercices.editLink")}
                  </Button>
                )}
              </div>
              {enEdition === ex.id && (
                <div className="animate-slide-down mt-3 flex flex-col gap-2">
                  <input
                    type="url"
                    value={nouveauLien}
                    onChange={(e) => setNouveauLien(e.target.value)}
                    className={inputClass}
                  />
                  <div className="flex gap-2">
                    <Button
                      className="px-3 py-1.5 text-xs"
                      disabled={loading || !nouveauLien.trim()}
                      onClick={() => remplacerLien(ex.id)}
                    >
                      {t("exercices.confirmReplace")}
                    </Button>
                    <Button
                      variant="secondary"
                      className="px-3 py-1.5 text-xs"
                      onClick={() => setEnEdition(null)}
                    >
                      {t("common.cancel")}
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

/** Mappe un statut d'exercice vers sa clé de traduction. */
function statutCle(statut: string): string {
  switch (statut) {
    case "RELU":
      return "reviewed";
    case "EN_ATTENTE_RELECTURE":
      return "pendingReview";
    default:
      return "submitted";
  }
}
