import clsx from "clsx"

type Tone = "slate" | "green" | "red" | "blue"

const toneClasses: Record<Tone, string> = {
  slate: "bg-ink-100 text-ink-600 border border-ink-200",
  green: "bg-brand-primary-100 text-brand-primary-800 border border-brand-primary-200",
  red: "bg-status-danger-bg text-status-danger-text border border-status-danger-text/15",
  blue: "bg-status-info-bg text-status-info-text border border-status-info-text/15",
}

export function Badge({ tone = "slate", children }: { tone?: Tone; children: React.ReactNode }) {
  return (
    <span className={clsx("inline-flex items-center rounded-sm px-2 py-0.5 text-xs font-medium", toneClasses[tone])}>
      {children}
    </span>
  )
}
