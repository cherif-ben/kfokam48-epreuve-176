"use client";

import { useCallback, useEffect, useState } from "react";
import { promotionApi } from "@/features/promotion/api/promotionApi";
import { etudiantApi } from "@/features/etudiant/api/etudiantApi";
import { relectureApi } from "@/features/relecture/api/relectureApi";
import { Card } from "@/shared/components/Card";
import { Button } from "@/shared/components/Button";
import { Alert } from "@/shared/components/Alert";
import type { ApiError, Etudiant, Promotion, Relecture } from "@/shared/types";

/**
 * EF8 / RG5 — liste des relectures assignées (#30) : chaque ligne affiche le lien de l'exercice
 * (jamais le nom de l'auteur, RG6/Q8). EF9 / EF10 — formulaire de notation (#31) : note entière
 * 0-20 et commentaire. Aucune validation locale de la note (F3) : NOTE_INVALIDE,
 * AUTO_RELECTURE et RELECTURE_DEJA_RENDUE viennent du backend.
 */
export default function RelecturesPage() {
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
      setSucces(`Relecture ${enCours === id ? "rendue" : ""} enregistrée (note ${note}/20).`);
      setEnCours(null);
      await charger(relecteurId);
    } catch (err) {
      const e = err as ApiError;
      setError({ code: e.code || "ERREUR_INCONNU", message: e.message, status: e.status ?? 0 });
    } finally {
      setChargement(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 px-4 py-8">
      <h1 className="text-2xl font-semibold text-zinc-900">Mes relectures</h1>

      <Card>
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
            <label htmlFor="relecteur" className="mb-1 block text-sm font-medium text-zinc-700">
              Je suis…
            </label>
            <select
              id="relecteur"
              value={relecteurId}
              onChange={(e) => choisirRelecteur(Number(e.target.value))}
              className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm"
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
      </Card>

      {chargement && <p className="text-sm text-zinc-500">Chargement…</p>}
      {error && <Alert type="error" message={error.message} code={error.code} />}
      {succes && <Alert type="success" message={succes} />}

      {relecteurId > 0 && !chargement && relectures.length === 0 && (
        <p className="text-sm text-zinc-500">Aucune relecture assignée — profitez-en !</p>
      )}

      <ul className="flex flex-col gap-3">
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
                      className="block truncate text-sm font-medium text-blue-600 hover:underline"
                    >
                      {r.lienExercice || "Exercice à relire"}
                    </a>
                    <p className="text-xs text-zinc-500">
                      Statut : <strong>{r.statut}</strong>
                      {r.statut === "RENDUE" && r.note !== null && (
                        <>
                          {" "}
                          — Note donnée : <strong>{r.note}/20</strong>
                        </>
                      )}
                    </p>
                  </div>
                  <Button
                    variant={r.statut === "EN_ATTENTE" ? "primary" : "secondary"}
                    onClick={() => ouvrirFormulaire(r)}
                  >
                    {r.statut === "EN_ATTENTE" ? "Relire" : "Corriger la note"}
                  </Button>
                </div>

                {enCours === r.id && (
                  <form
                    onSubmit={(e) => {
                      e.preventDefault();
                      rendre(r.id);
                    }}
                    className="flex flex-col gap-3 rounded-md border border-zinc-200 bg-zinc-50 p-3"
                  >
                    <div>
                      <label htmlFor={`note-${r.id}`} className="mb-1 block text-sm font-medium text-zinc-700">
                        Note (entière, 0–20)
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
                        className="w-32 rounded-md border border-zinc-300 px-3 py-2 text-sm"
                        required
                      />
                    </div>
                    <div>
                      <label
                        htmlFor={`commentaire-${r.id}`}
                        className="mb-1 block text-sm font-medium text-zinc-700"
                      >
                        Commentaire
                      </label>
                      <textarea
                        id={`commentaire-${r.id}`}
                        value={commentaire}
                        onChange={(e) => setCommentaire(e.target.value)}
                        rows={3}
                        className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                        required
                      />
                    </div>
                    <div className="flex gap-2">
                      <Button type="submit" disabled={chargement}>
                        {chargement ? "Envoi…" : "Envoyer"}
                      </Button>
                      <Button type="button" variant="secondary" onClick={() => setEnCours(null)}>
                        Annuler
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
