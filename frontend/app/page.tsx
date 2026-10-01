"use client";

import Link from "next/link";
import { useI18n } from "@/lib/i18n/I18nProvider";
import { ThemeToggle, LanguageToggle } from "@/shared/components/PreferencesControls";

const cartes = [
  {
    href: "/sessions/nouvelle",
    labelKey: "home.formateur.title" as const,
    descKey: "home.formateur.desc" as const,
    icone: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
        <path d="M12 3 2 8l10 5 10-5-10-5z" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M6 10.5V15c0 1.5 2.7 3 6 3s6-1.5 6-3v-4.5" strokeLinecap="round" />
      </svg>
    ),
  },
  {
    href: "/etudiant/presence",
    labelKey: "home.presence.title" as const,
    descKey: "home.presence.desc" as const,
    icone: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21c0-4 3.6-6 8-6s8 2 8 6" strokeLinecap="round" />
        <path d="m9 12 2 2 4-4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
  },
  {
    href: "/etudiant/exercices",
    labelKey: "home.exercices.title" as const,
    descKey: "home.exercices.desc" as const,
    icone: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
        <path d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9l-6-6z" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M14 3v6h6M9 13h6M9 17h4" strokeLinecap="round" />
      </svg>
    ),
  },
  {
    href: "/etudiant/relectures",
    labelKey: "home.relectures.title" as const,
    descKey: "home.relectures.desc" as const,
    icone: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
        <path d="M21 12a9 9 0 1 1-9-9" strokeLinecap="round" />
        <path d="M17 3l4 4-4 4" strokeLinecap="round" strokeLinejoin="round" transform="translate(0 -1) scale(0.9)" />
        <path d="m9 12 2 2 4-5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
  },
];

export default function Home() {
  const { t } = useI18n();

  return (
    <div className="relative flex flex-1 flex-col overflow-hidden">
      {/* Halos décoratifs d'arrière-plan */}
      <div aria-hidden="true" className="pointer-events-none absolute inset-0">
        <div className="absolute -top-32 left-1/4 h-72 w-72 rounded-full bg-accent/10 blur-3xl" />
        <div className="absolute right-1/5 top-40 h-64 w-64 rounded-full bg-success/10 blur-3xl" />
        <div className="absolute bottom-0 left-10 h-56 w-56 rounded-full bg-warning/10 blur-3xl" />
      </div>

      {/* Contrôles en haut à droite */}
      <div className="relative z-10 flex items-center justify-end gap-2 px-4 pt-4">
        <LanguageToggle />
        <ThemeToggle />
      </div>

      <div className="relative z-10 mx-auto flex w-full max-w-5xl flex-1 flex-col items-center justify-center gap-12 px-4 py-16">
        {/* Hero */}
        <div className="animate-fade-in-up flex flex-col items-center gap-5 text-center">
          <span className="rounded-full border border-accent/25 bg-accent-soft px-3 py-1 text-xs font-semibold uppercase tracking-wider text-accent-strong dark:text-accent">
            {t("home.badge")}
          </span>
          <h1 className="max-w-3xl text-balance text-4xl font-bold tracking-tight sm:text-5xl">
            {t("home.title").replace(t("home.titleHighlight"), "")}
            <span className="bg-gradient-to-r from-accent to-accent-strong bg-clip-text text-transparent">
              {t("home.titleHighlight")}
            </span>
          </h1>
          <p className="max-w-2xl text-pretty text-base leading-relaxed text-muted sm:text-lg">
            {t("home.subtitle")}
          </p>
        </div>

        {/* Cartes d'entrée */}
        <div className="stagger grid w-full gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {cartes.map((carte) => (
            <Link
              key={carte.href}
              href={carte.href}
              className="group relative flex flex-col gap-3 overflow-hidden rounded-xl border border-line bg-surface p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:border-accent/40 hover:shadow-lg hover:shadow-accent/10"
            >
              <div className="absolute inset-x-0 top-0 h-0.5 origin-left scale-x-0 bg-gradient-to-r from-accent to-accent-strong transition-transform duration-300 group-hover:scale-x-100" />
              <span className="flex h-11 w-11 items-center justify-center rounded-lg bg-accent-soft text-accent-strong transition-colors duration-300 group-hover:bg-accent group-hover:text-accent-fg dark:text-accent dark:group-hover:text-white">
                {carte.icone}
              </span>
              <h2 className="text-base font-semibold tracking-tight">{t(carte.labelKey)}</h2>
              <p className="text-sm leading-relaxed text-muted">{t(carte.descKey)}</p>
              <span className="mt-auto inline-flex items-center gap-1 pt-2 text-sm font-medium text-accent opacity-0 transition-all duration-300 group-hover:translate-x-1 group-hover:opacity-100">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
                  <path d="M5 12h14m-6-6 6 6-6 6" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </span>
            </Link>
          ))}
        </div>

        <p className="animate-fade-in text-xs text-muted">{t("home.footer")}</p>
      </div>
    </div>
  );
}
