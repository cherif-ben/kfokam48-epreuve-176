interface CardProps {
  title?: string;
  children: React.ReactNode;
  className?: string;
}

export function Card({ title, children, className = "" }: CardProps) {
  return (
    <div
      className={`rounded-xl border border-line bg-surface p-6 shadow-sm shadow-black/[0.03] transition-colors ${className}`}
    >
      {title && <h2 className="mb-4 text-lg font-semibold tracking-tight">{title}</h2>}
      {children}
    </div>
  );
}
