"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { relectureApi } from "@/features/relecture/api/relectureApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import type { ApiError, Etudiant, Promotion, Relecture } from "@/shared/types";
import { useI18n } from "@/lib/i18n/I18nProvider";

/**
 * EF8 / RG5 — relectures assignées (jamais le nom de l'auteur, RG6/Q8).
 * EF9 / EF10 — notation : note entière 0-20 et commentaire. Aucune validation
 * locale de la note (F3) : NOTE_INVALIDE, AUTO_RELECTURE, RELECTURE_DEJA_RENDUE viennent du backend.
 */
export default function RelecturesPage() {
  const { t } = useI18n();
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [promotionId, setPromotionId] = useState(0);
  const [etudiants, setEtudiants] = useState<Etudiant[]>([]);
  const [relecteurId, setRelecteurId] = useState(0);
  const [relectures, setRelectures] = useState<Relecture[]>([]);
  const [chargement, setChargement] = useState(false);
  const [enCours, setEnCours] = useState<number | null>(null);
  const [note, setNote] = useState(10);
  const [commentaire, setCommentaire] = useState("");
  const [error, setError] = useState<ApiError | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  const charger = useCallback(async (rid: number) => {
    if (!rid) return;
    setChargement(true);
    setError(null);
    try {
      setRelectures(await relectureApi.getRelectures(rid));
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
        if (p.length > 0) setPromotionId(p[0].id);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!promotionId) return;
    etudiantApi.listerParPromotion(promotionId).then(setEtudiants).catch(() => setEtudiants([]));
  }, [promotionId]);

  const choisirRelecteur = (id: number) => {
    setRelecteurId(id);
    setSucces(null);
    setError(null);
    charger(id);
  };

  const ouvrirFormulaire = (r: Relecture) => {
    setEnCours(r.id);
    setNote(r.note ?? 10);
    setCommentaire(r.commentaire ?? "");
    setError(null);
    setSucces(null);
  };

  const rendre = async (id: number) => {
    if (!relecteurId) return;
    setChargement(true);
    try {
      await relectureApi.rendreRelecture(id, relecteurId, note, commentaire);
      setSucces(t("relectures.done", { note }));
      setEnCours(null);
      await charger(relecteurId);
    } catch (err) {
      const e = err as ApiError;
      setError({ code: e.code || "ERREUR_INCONNU", message: e.message, status: e.status ?? 0 });
    } finally {
      setChargement(false);
    }
  };

  const inputClass =
    "w-full rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="animate-fade-in-up text-2xl font-bold tracking-tight">{t("relectures.title")}</h1>

      <Card className="animate-fade-in-up">
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
            <label htmlFor="relecteur" className="mb-1 block text-sm font-medium">
              {t("relectures.whoAmI")}
            </label>
            <select
              id="relecteur"
              value={relecteurId}
              onChange={(e) => choisirRelecteur(Number(e.target.value))}
              className={inputClass}
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
      </Card>

      {chargement && <p className="animate-pulse text-sm text-muted">{t("common.loading")}</p>}
      {error && <Alert type="error" message={error.message} code={error.code} />}
      {succes && <Alert type="success" message={succes} />}

      {relecteurId > 0 && !chargement && relectures.length === 0 && (
        <p className="text-sm text-muted">{t("relectures.empty")}</p>
      )}

      <ul className="stagger flex flex-col gap-3">
        {relectures.map((r) => (
          <li key={r.id}>
            <Card>
              <div className="flex flex-col gap-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="min-w-0">
                    <a
                      href={r.lienExercice}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block truncate text-sm font-medium text-accent-strong hover:underline dark:text-accent"
                    >
                      {r.lienExercice || t("relectures.toReview")}
                    </a>
                    <div className="mt-1.5 flex items-center gap-2 text-xs text-muted">
                      {r.statut === "EN_ATTENTE" ? (
                        <Badge tone="warning">{t("status.pending")}</Badge>
                      ) : (
                        <Badge tone="success">{t("status.rendered")}</Badge>
                      )}
                      {r.statut === "RENDUE" && r.note !== null && (
                        <span className="rounded-full bg-accent-soft px-2 py-0.5 font-semibold text-accent-strong dark:text-accent">
                          {t("relectures.givenGrade")} : {r.note}/20
                        </span>
                      )}
                    </div>
                  </div>
                  <Button
                    variant={r.statut === "EN_ATTENTE" ? "primary" : "secondary"}
                    onClick={() => ouvrirFormulaire(r)}
                  >
                    {r.statut === "EN_ATTENTE" ? t("relectures.review") : t("relectures.correct")}
                  </Button>
                </div>

                {enCours === r.id && (
                  <form
                    onSubmit={(e) => {
                      e.preventDefault();
                      rendre(r.id);
                    }}
                    className="animate-scale-in flex flex-col gap-3 rounded-lg border border-line bg-surface-muted p-4"
                  >
                    <div>
                      <label htmlFor={`note-${r.id}`} className="mb-1 block text-sm font-medium">
                        {t("relectures.grade")}
                      </label>
                      {/* RG3 (Q9) : la plage est indicative — le backend reste arbitre (F3). */}
                      <input
                        id={`note-${r.id}`}
                        type="number"
                        min={0}
                        max={20}
                        step={1}
                        value={note}
                        onChange={(e) => setNote(Number(e.target.value))}
                        className="w-28 rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
                        required
                      />
                    </div>
                    <div>
                      <label
                        htmlFor={`commentaire-${r.id}`}
                        className="mb-1 block text-sm font-medium"
                      >
                        {t("relectures.comment")}
                      </label>
                      <textarea
                        id={`commentaire-${r.id}`}
                        value={commentaire}
                        onChange={(e) => setCommentaire(e.target.value)}
                        rows={3}
                        className={inputClass}
                        required
                      />
                    </div>
                    <div className="flex gap-2">
                      <Button type="submit" disabled={chargement}>
                        {chargement ? t("relectures.sending") : t("relectures.send")}
                      </Button>
                      <Button type="button" variant="secondary" onClick={() => setEnCours(null)}>
                        {t("common.cancel")}
                      </Button>
                    </div>
                  </form>
                )}
              </div>
            </Card>
          </li>
        ))}
      </ul>
    </div>
  );
}
