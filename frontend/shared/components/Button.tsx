interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "danger" | "ghost";
}

export function Button({ variant = "primary", className = "", ...props }: ButtonProps) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 focus-visible:ring-offset-background active:scale-[0.97] disabled:pointer-events-none disabled:opacity-50";
  const variants = {
    primary:
      "bg-accent text-accent-fg shadow-sm hover:bg-accent-strong hover:shadow-md dark:text-white",
    secondary:
      "border border-line bg-surface text-foreground hover:bg-surface-muted",
    danger: "bg-danger text-white shadow-sm hover:brightness-110",
    ghost: "text-muted hover:bg-surface-muted hover:text-foreground",
  } as const;

  return <button className={`${base} ${variants[variant]} ${className}`} {...props} />;
}
