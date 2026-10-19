import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from "react"
import clsx from "clsx"

interface FieldWrapperProps {
  label: string
  error?: string
  children: ReactNode
}

function FieldWrapper({ label, error, children }: FieldWrapperProps) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-ink-700">{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs text-status-danger-text">{error}</span>}
    </label>
  )
}

const fieldClasses =
  "w-full rounded-sm border border-ink-300 px-3.5 py-2.5 text-sm text-ink-900 transition-colors focus:border-brand-primary-600 focus:outline-none focus:ring-1 focus:ring-brand-primary-600"

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

export function TextField({ label, error, className, ...props }: TextFieldProps) {
  return (
    <FieldWrapper label={label} error={error}>
      <input className={clsx(fieldClasses, error && "border-status-danger-text", className)} {...props} />
    </FieldWrapper>
  )
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
}

export function SelectField({ label, error, className, children, ...props }: SelectFieldProps) {
  return (
    <FieldWrapper label={label} error={error}>
      <select className={clsx(fieldClasses, "bg-white", error && "border-status-danger-text", className)} {...props}>
        {children}
      </select>
    </FieldWrapper>
  )
}
