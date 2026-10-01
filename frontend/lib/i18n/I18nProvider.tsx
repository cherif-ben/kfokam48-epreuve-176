"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { dictionaries, type Lang, type TranslationKey } from "./dictionaries";

const LANG_STORAGE_KEY = "kfokam48.lang";

interface I18nContextValue {
  lang: Lang;
  setLang: (lang: Lang) => void;
  /** Traduit une clé ; les placeholders « {nom} » sont remplacés par `params`. */
  t: (key: TranslationKey, params?: Record<string, string | number>) => string;
}

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [lang, setLangState] = useState<Lang>("fr");

  // Restaure la langue choisie après le montage (évite tout mismatch SSR).
  useEffect(() => {
    const stockee = window.localStorage.getItem(LANG_STORAGE_KEY);
    if (stockee === "fr" || stockee === "en") {
      setLangState(stockee);
      document.documentElement.lang = stockee;
    }
  }, []);

  const setLang = useCallback((nouvelle: Lang) => {
    setLangState(nouvelle);
    window.localStorage.setItem(LANG_STORAGE_KEY, nouvelle);
    document.documentElement.lang = nouvelle;
  }, []);

  const t = useCallback(
    (key: TranslationKey, params?: Record<string, string | number>) => {
      const template: string = dictionaries[lang][key];
      if (!params) return template;
      return template.replace(/\{(\w+)\}/g, (_match, nom: string) =>
        params[nom] !== undefined ? String(params[nom]) : `{${nom}}`,
      );
    },
    [lang],
  );

  const value = useMemo(() => ({ lang, setLang, t }), [lang, setLang, t]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext);
  if (!ctx) {
    throw new Error("useI18n doit être utilisé dans un I18nProvider");
  }
  return ctx;
}
