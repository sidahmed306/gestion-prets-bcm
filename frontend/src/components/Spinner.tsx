import { Loader2 } from "lucide-react"

export function Spinner({ label = "Chargement..." }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-ink-500">
      <Loader2 size={18} className="animate-spin text-brand-primary-700" />
      {label}
    </div>
  )
}
