import Link from "next/link";

export default function FormateurLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-zinc-50">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <Link href="/" className="text-lg font-semibold text-zinc-900">
            KFOKAM48
          </Link>
          <nav className="flex gap-4 text-sm text-zinc-600">
            <Link href="/sessions" className="hover:text-zinc-900">Sessions</Link>
            <Link href="/tableau" className="hover:text-zinc-900">Tableau</Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
