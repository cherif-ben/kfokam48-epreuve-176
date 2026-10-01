import { AppHeader } from "@/shared/components/AppHeader";

export default function FormateurLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen">
      <AppHeader
        links={[
          { href: "/sessions", labelKey: "nav.sessions" },
          { href: "/tableau", labelKey: "nav.dashboard" },
        ]}
      />
      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
