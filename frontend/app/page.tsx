import Link from "next/link";

export default function Home() {
  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-10 bg-zinc-50 px-4 py-16">
      <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">
        KFOKAM48 — Suivi de présence &amp; relecture entre pairs
      </h1>
      <div className="grid w-full max-w-3xl gap-6 sm:grid-cols-3">
        <Link
          href="/sessions/nouvelle"
          className="group rounded-lg border border-zinc-200 bg-white p-6 shadow-sm transition hover:border-blue-400 hover:shadow"
        >
          <h2 className="mb-2 text-lg font-semibold text-zinc-900 group-hover:text-blue-600">
            Formateur
          </h2>
          <p className="text-sm text-zinc-600">
            Ouvrir une session, afficher le code, ajouter des présences, clôturer, consulter le
            tableau.
          </p>
        </Link>
        <Link
          href="/etudiant/presence"
          className="group rounded-lg border border-zinc-200 bg-white p-6 shadow-sm transition hover:border-blue-400 hover:shadow"
        >
          <h2 className="mb-2 text-lg font-semibold text-zinc-900 group-hover:text-blue-600">
            Étudiant — Présence
          </h2>
          <p className="text-sm text-zinc-600">
            Marquer sa présence avec le code fourni par le formateur.
          </p>
        </Link>
        <Link
          href="/etudiant/exercices"
          className="group rounded-lg border border-zinc-200 bg-white p-6 shadow-sm transition hover:border-blue-400 hover:shadow"
        >
          <h2 className="mb-2 text-lg font-semibold text-zinc-900 group-hover:text-blue-600">
            Étudiant — Exercices
          </h2>
          <p className="text-sm text-zinc-600">
            Déposer le lien de son exercice et consulter ses relectures assignées.
          </p>
        </Link>
      </div>
    </div>
  );
}
