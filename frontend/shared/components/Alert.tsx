interface AlertProps {
  type: "success" | "error" | "info";
  message: string;
  code?: string;
}

const styles = {
  success: "border-success/30 bg-success-soft text-success",
  error: "border-danger/30 bg-danger-soft text-danger",
  info: "border-accent/30 bg-accent-soft text-accent-strong dark:text-accent",
} as const;

const icons = {
  success: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="h-4 w-4 shrink-0">
      <path d="M20 6 9 17l-5-5" strokeLinecap="round" strokeLinejoin="round" className="animate-draw-check" />
    </svg>
  ),
  error: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4 shrink-0">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 8v4m0 4h.01" strokeLinecap="round" />
    </svg>
  ),
  info: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4 shrink-0">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 16v-4m0-4h.01" strokeLinecap="round" />
    </svg>
  ),
} as const;

export function Alert({ type, message, code }: AlertProps) {
  return (
    <div
      className={`animate-slide-down flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm ${styles[type]}`}
      role="alert"
    >
      {icons[type]}
      <div>
        <p className="font-medium">{message}</p>
        {code && <p className="mt-0.5 font-mono text-xs opacity-70">{code}</p>}
      </div>
    </div>
  );
}
