import { AppHeader } from "@/shared/components/AppHeader";

export default function EtudiantLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen">
      <AppHeader
        links={[
          { href: "/etudiant/presence", labelKey: "nav.presence" },
          { href: "/etudiant/exercices", labelKey: "nav.exercises" },
          { href: "/etudiant/relectures", labelKey: "nav.reviews" },
        ]}
      />
      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
