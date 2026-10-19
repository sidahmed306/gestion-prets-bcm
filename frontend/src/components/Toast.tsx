import { createContext, useCallback, useContext, useState, type ReactNode } from "react"
import { AnimatePresence, motion } from "framer-motion"
import { CheckCircle2, XCircle } from "lucide-react"

interface Toast {
  id: number
  type: "success" | "error"
  message: string
}

const ToastContext = createContext<((type: Toast["type"], message: string) => void) | null>(null)

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error("useToast doit être utilisé dans ToastProvider")
  return context
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const notify = useCallback((type: Toast["type"], message: string) => {
    const id = Date.now()
    setToasts((current) => [...current, { id, type, message }])
    setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, 3500)
  }, [])

  return (
    <ToastContext.Provider value={notify}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
        <AnimatePresence>
          {toasts.map((toast) => (
            <motion.div
              key={toast.id}
              initial={{ opacity: 0, y: 8, scale: 0.97 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8, scale: 0.97 }}
              transition={{ duration: 0.18 }}
              className="flex items-center gap-2 rounded-sm border bg-white px-4 py-3 text-sm shadow-lg"
              style={{ borderColor: toast.type === "success" ? "#b4eec5" : "#e8b9b0" }}
            >
              {toast.type === "success" ? (
                <CheckCircle2 size={18} className="text-brand-primary-700" />
              ) : (
                <XCircle size={18} className="text-status-danger-text" />
              )}
              <span className="text-ink-700">{toast.message}</span>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  )
}
