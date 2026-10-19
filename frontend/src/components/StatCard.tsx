import type { LucideIcon } from "lucide-react"
import { motion } from "framer-motion"
import clsx from "clsx"

type Tone = "blue" | "slate" | "red"

const toneClasses: Record<Tone, string> = {
  blue: "bg-status-info-bg text-status-info-text",
  slate: "bg-ink-100 text-ink-700",
  red: "bg-status-danger-bg text-status-danger-text",
}

interface StatCardProps {
  label: string
  value: number | string
  icon: LucideIcon
  tone?: Tone
  index?: number
}

export function StatCard({ label, value, icon: Icon, tone = "slate", index = 0 }: StatCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2, delay: index * 0.05, ease: "easeOut" }}
      className="rounded-sm border border-ink-200 bg-white p-4"
    >
      <div className="flex items-center gap-3">
        <div className={clsx("flex h-10 w-10 items-center justify-center rounded-sm", toneClasses[tone])}>
          <Icon size={18} />
        </div>
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-ink-500">{label}</p>
          <p className="text-xl font-semibold text-ink-900" style={{ fontVariantNumeric: "proportional-nums" }}>
            {value}
          </p>
        </div>
      </div>
    </motion.div>
  )
}
