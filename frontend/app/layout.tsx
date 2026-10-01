import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Providers } from "./Providers";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "KFOKAM48 — Présences & relectures",
  description: "Suivi de présence, dépôt d'exercices et relecture entre pairs.",
};

/**
 * Script anti-foystick : applique le thème stocké (ou système) AVANT la première peinture,
 * pour éviter tout flash de mauvais thème au chargement.
 */
const themeInitScript = `
(function () {
  try {
    var stocke = localStorage.getItem("kfokam48.theme");
    var sombre =
      stocke === "dark" ||
      (stocke === null && window.matchMedia("(prefers-color-scheme: dark)").matches);
    if (sombre) document.documentElement.classList.add("dark");
    document.documentElement.style.colorScheme = sombre ? "dark" : "light";
    var lang = localStorage.getItem("kfokam48.lang");
    if (lang === "en" || lang === "fr") document.documentElement.lang = lang;
  } catch (e) {}
})();
`;

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="fr"
      suppressHydrationWarning
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col theme-transition">
        <script dangerouslySetInnerHTML={{ __html: themeInitScript }} />
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
