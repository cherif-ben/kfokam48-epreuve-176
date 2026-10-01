"use client";

import { useTheme } from "@/lib/theme/ThemeProvider";
import { useI18n } from "@/lib/i18n/I18nProvider";

/** Bascule clair/sombre avec icônes animées (rotation). */
export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  const { t } = useI18n();
  const isDark = theme === "dark";

  return (
    <button
      type="button"
      onClick={toggleTheme}
      title={isDark ? t("common.theme.toLight") : t("common.theme.toDark")}
      aria-label={isDark ? t("common.theme.toLight") : t("common.theme.toDark")}
      className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-line bg-surface text-muted transition-all duration-200 hover:bg-surface-muted hover:text-foreground active:scale-90"
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        className={`h-4 w-4 transition-transform duration-500 ${isDark ? "rotate-180" : ""}`}
      >
        {isDark ? (
          <path
            d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ) : (
          <>
            <circle cx="12" cy="12" r="4" />
            <path
              d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41m11.32 11.32 1.41 1.41M2 12h2m16 0h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
              strokeLinecap="round"
            />
          </>
        )}
      </svg>
    </button>
  );
}

/** Sélecteur de langue FR / EN. */
export function LanguageToggle() {
  const { lang, setLang } = useI18n();

  return (
    <div className="flex items-center rounded-lg border border-line bg-surface p-0.5 text-xs font-semibold">
      {(["fr", "en"] as const).map((code) => (
        <button
          key={code}
          type="button"
          onClick={() => setLang(code)}
          aria-pressed={lang === code}
          className={`rounded-md px-2 py-1 uppercase transition-all duration-200 ${
            lang === code
              ? "bg-accent text-accent-fg shadow-sm dark:text-white"
              : "text-muted hover:text-foreground"
          }`}
        >
          {code}
        </button>
      ))}
    </div>
  );
}
