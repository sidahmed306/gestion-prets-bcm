export function formatDate(isoDate: string | null): string {
  if (!isoDate) return "—"
  return new Date(isoDate + "T00:00:00").toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })
}

export function formatDateTime(isoDateTime: string | null): string {
  if (!isoDateTime) return "—"
  return new Date(isoDateTime).toLocaleString("fr-FR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  })
}

export function joursDeRetard(dateRetourPrevue: string): number {
  const prevue = new Date(dateRetourPrevue + "T00:00:00")
  const aujourdhui = new Date()
  aujourdhui.setHours(0, 0, 0, 0)
  const diffMs = aujourdhui.getTime() - prevue.getTime()
  return Math.max(0, Math.round(diffMs / (1000 * 60 * 60 * 24)))
}

export function todayIso(): string {
  return new Date().toISOString().slice(0, 10)
}
