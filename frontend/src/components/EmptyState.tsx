import type { LucideIcon } from "lucide-react"

export function EmptyState({ icon: Icon, title, description }: { icon: LucideIcon; title: string; description?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-sm border border-dashed border-ink-300 bg-white py-14 text-center">
      <div className="flex h-11 w-11 items-center justify-center rounded-sm bg-ink-100 text-ink-500">
        <Icon size={20} />
      </div>
      <p className="text-sm font-medium text-ink-600">{title}</p>
      {description && <p className="text-xs text-ink-400">{description}</p>}
    </div>
  )
}
