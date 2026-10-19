import type { ButtonHTMLAttributes } from "react"
import clsx from "clsx"

type Variant = "primary" | "secondary" | "danger" | "ghost"

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
}

const variantClasses: Record<Variant, string> = {
  primary: "bg-brand-primary-700 text-white hover:bg-brand-primary-800 focus-visible:outline-brand-primary-700",
  secondary: "bg-white text-ink-700 border border-ink-300 hover:bg-ink-50 focus-visible:outline-ink-400",
  danger:
    "bg-white text-status-danger-text border border-status-danger-text/30 hover:bg-status-danger-bg focus-visible:outline-status-danger-text",
  ghost: "bg-transparent text-ink-600 hover:bg-ink-100 focus-visible:outline-ink-400",
}

export function Button({ variant = "primary", className, ...props }: ButtonProps) {
  return (
    <button
      className={clsx(
        "inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm font-medium transition-colors duration-150 disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-2 focus-visible:outline-offset-2",
        variantClasses[variant],
        className,
      )}
      {...props}
    />
  )
}
