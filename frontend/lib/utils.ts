export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return "—";
  const d = typeof date === "string" ? new Date(date) : date;
  return d.toLocaleString("fr-FR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function formatMoyenne(moyenne: number | null | undefined): string {
  if (moyenne === null || moyenne === undefined) return "—";
  return moyenne.toFixed(1);
}

export function isExpired(expirationAt: string | Date): boolean {
  return new Date(expirationAt).getTime() < Date.now();
}

export function clampNote(note: number, min = 0, max = 20): number {
  return Math.min(Math.max(note, min), max);
}

export function isValidUrl(str: string): boolean {
  try {
    new URL(str);
    return true;
  } catch {
    return false;
  }
}
