"use client";

import { useState } from "react";
import type { Etudiant } from "@/shared/types";
import { useI18n } from "@/lib/i18n/I18nProvider";

interface FormulairePresenceProps {
  etudiants: Etudiant[];
  onSubmit: (code: string, etudiantId: number) => void;
  disabled?: boolean;
}

export function FormulairePresence({ etudiants, onSubmit, disabled }: FormulairePresenceProps) {
  const { t } = useI18n();
  const [code, setCode] = useState("");
  const [etudiantId, setEtudiantId] = useState<number>(0);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code || !etudiantId) return;
    onSubmit(code, etudiantId);
  };

  const inputClass =
    "w-full rounded-lg border border-line bg-surface px-3 py-2 text-sm transition-colors focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent";

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label htmlFor="code-presence" className="mb-1 block text-sm font-medium">
          {t("form.code")}
        </label>
        <input
          id="code-presence"
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder={t("form.codePlaceholder")}
          maxLength={6}
          className={`${inputClass} font-mono text-lg font-semibold tracking-[0.3em] uppercase`}
          disabled={disabled}
          required
        />
      </div>
      <div>
        <label htmlFor="nom-etudiant" className="mb-1 block text-sm font-medium">
          {t("form.yourName")}
        </label>
        <select
          id="nom-etudiant"
          value={etudiantId}
          onChange={(e) => setEtudiantId(Number(e.target.value))}
          className={inputClass}
          disabled={disabled}
          required
        >
          <option value={0}>{t("form.selectStudent")}</option>
          {etudiants.map((e) => (
            <option key={e.id} value={e.id}>
              {e.nom}
            </option>
          ))}
        </select>
      </div>
      <button
        type="submit"
        disabled={disabled || !code || !etudiantId}
        className="inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-medium text-accent-fg shadow-sm transition-all duration-200 hover:bg-accent-strong hover:shadow-md active:scale-[0.97] disabled:pointer-events-none disabled:opacity-50 dark:text-white"
      >
        {t("presence.submit")}
      </button>
    </form>
  );
}
