import type { Etudiant } from "@/shared/types";

interface SelectEtudiantProps {
  etudiants: Etudiant[];
  value: number;
  onChange: (id: number) => void;
}

export function SelectEtudiant({ etudiants, value, onChange }: SelectEtudiantProps) {
  return (
    <select
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
    >
      <option value="">Sélectionner un étudiant</option>
      {etudiants.map((e) => (
        <option key={e.id} value={e.id}>
          {e.nom}
        </option>
      ))}
    </select>
  );
}
