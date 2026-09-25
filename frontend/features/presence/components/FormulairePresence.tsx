import { useState } from "react";
import type { Etudiant } from "@/shared/types";

interface FormulairePresenceProps {
  etudiants: Etudiant[];
  onSubmit: (code: string, etudiantId: number) => void;
  disabled?: boolean;
}

export function FormulairePresence({ etudiants, onSubmit, disabled }: FormulairePresenceProps) {
  const [code, setCode] = useState("");
  const [etudiantId, setEtudiantId] = useState<number>(0);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code || !etudiantId) return;
    onSubmit(code, etudiantId);
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="block text-sm font-medium text-zinc-700 mb-1">
          Code de présence
        </label>
        <input
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="Ex: ABC123"
          maxLength={6}
          className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm font-mono focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          disabled={disabled}
          required
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-zinc-700 mb-1">
          Votre nom
        </label>
        <select
          value={etudiantId}
          onChange={(e) => setEtudiantId(Number(e.target.value))}
          className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          disabled={disabled}
          required
        >
          <option value={0}>Sélectionner un étudiant</option>
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
        className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
      >
        {disabled ? "Envoi…" : "Marquer ma présence"}
      </button>
    </form>
  );
}
