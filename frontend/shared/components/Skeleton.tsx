/** Squelette de chargement — rectangle animé (shimmer). */
export function Skeleton({ className = "" }: { className?: string }) {
  return <div className={`skeleton ${className}`} aria-hidden="true" />;
}

/** Ligne de tableau squelettique. */
export function SkeletonRow({ cols = 5 }: { cols?: number }) {
  return (
    <tr>
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className="px-4 py-3">
          <Skeleton className={`h-4 ${i === 0 ? "w-28" : "w-10 mx-auto"}`} />
        </td>
      ))}
    </tr>
  );
}

/** Carte squelettique. */
export function SkeletonCard() {
  return (
    <div className="rounded-xl border border-line bg-surface p-6">
      <Skeleton className="mb-4 h-5 w-40" />
      <Skeleton className="mb-2 h-4 w-full" />
      <Skeleton className="mb-2 h-4 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
    </div>
  );
}
