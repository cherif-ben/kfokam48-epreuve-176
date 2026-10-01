"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useI18n } from "@/lib/i18n/I18nProvider";
import { ThemeToggle, LanguageToggle } from "./PreferencesControls";

/**
 * En-tête partagé : logo, navigation avec soulignement animé de l'onglet actif,
 * bascule de langue et bascule de thème. Utilisé par les espaces formateur et étudiant.
 */
export function AppHeader({
  links,
}: {
  links: { href: string; labelKey: "nav.sessions" | "nav.dashboard" | "nav.presence" | "nav.exercises" | "nav.reviews" }[];
}) {
  const { t } = useI18n();
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-surface/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-3">
        <div className="flex items-center gap-6">
          <Link href="/" className="group flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent text-sm font-bold text-accent-fg shadow-sm transition-transform duration-200 group-hover:scale-105 dark:text-white">
              K48
            </span>
            <span className="hidden text-base font-semibold tracking-tight sm:block">
              KFOKAM48
            </span>
          </Link>
          <nav className="flex gap-1">
            {links.map((link) => {
              const actif = pathname.startsWith(link.href);
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`relative rounded-lg px-3 py-1.5 text-sm transition-colors duration-200 ${
                    actif
                      ? "font-medium text-accent-strong dark:text-accent"
                      : "text-muted hover:bg-surface-muted hover:text-foreground"
                  }`}
                >
                  {t(link.labelKey)}
                  {actif && (
                    <span className="absolute inset-x-3 -bottom-[13px] h-0.5 rounded-full bg-accent animate-fade-in" />
                  )}
                </Link>
              );
            })}
          </nav>
        </div>
        <div className="flex items-center gap-2">
          <LanguageToggle />
          <ThemeToggle />
        </div>
      </div>
    </header>
  );
}
