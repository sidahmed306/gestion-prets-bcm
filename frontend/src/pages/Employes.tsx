import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { Pencil, Plus, Trash2, Users } from "lucide-react"
import { employesApi } from "../api/employes"
import type { EmployeRequest, EmployeResponse } from "../api/types"
import { PageHeader } from "../components/PageHeader"
import { Button } from "../components/Button"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Modal } from "../components/Modal"
import { ConfirmDialog } from "../components/ConfirmDialog"
import { TextField } from "../components/Form"
import { useToast } from "../components/Toast"
import { getErrorMessage } from "../lib/errors"

const EMPTY_FORM: EmployeRequest = { matricule: "", nom: "", departement: "", email: "" }

export function Employes() {
  const queryClient = useQueryClient()
  const notify = useToast()
  const { data: employes, isLoading } = useQuery({ queryKey: ["employes"], queryFn: employesApi.findAll })

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<EmployeResponse | null>(null)
  const [form, setForm] = useState<EmployeRequest>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [deleting, setDeleting] = useState<EmployeResponse | null>(null)

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["employes"] })

  const creerMutation = useMutation({
    mutationFn: (data: EmployeRequest) => employesApi.creer(data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Employé créé avec succès")
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  const modifierMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: EmployeRequest }) => employesApi.modifier(id, data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Employé modifié avec succès")
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  const supprimerMutation = useMutation({
    mutationFn: (id: number) => employesApi.supprimer(id),
    onSuccess: () => {
      invalidate()
      setDeleting(null)
      notify("success", "Employé supprimé")
    },
    onError: (error) => {
      setDeleting(null)
      notify("error", getErrorMessage(error))
    },
  })

  function openCreate() {
    setEditing(null)
    setForm(EMPTY_FORM)
    setFieldErrors({})
    setModalOpen(true)
  }

  function openEdit(employe: EmployeResponse) {
    setEditing(employe)
    setForm({
      matricule: employe.matricule,
      nom: employe.nom,
      departement: employe.departement ?? "",
      email: employe.email ?? "",
    })
    setFieldErrors({})
    setModalOpen(true)
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (editing) {
      modifierMutation.mutate({ id: editing.id, data: form })
    } else {
      creerMutation.mutate(form)
    }
  }

  const isSaving = creerMutation.isPending || modifierMutation.isPending

  return (
    <div>
      <PageHeader
        title="Employés"
        description="Les personnes qui peuvent emprunter du matériel"
        action={
          <Button onClick={openCreate}>
            <Plus size={16} /> Nouvel employé
          </Button>
        }
      />

      {isLoading ? (
        <Spinner />
      ) : employes && employes.length > 0 ? (
        <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-2.5">Matricule</th>
                <th className="px-4 py-2.5">Nom</th>
                <th className="px-4 py-2.5">Département</th>
                <th className="px-4 py-2.5">Email</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {employes.map((employe) => (
                <tr key={employe.id} className="group">
                  <td className="px-4 py-2.5 font-medium text-ink-900">{employe.matricule}</td>
                  <td className="px-4 py-2.5 text-ink-700">{employe.nom}</td>
                  <td className="px-4 py-2.5 text-ink-500">{employe.departement || "—"}</td>
                  <td className="px-4 py-2.5 text-ink-500">{employe.email || "—"}</td>
                  <td className="px-4 py-2.5">
                    <div className="flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                      <button
                        onClick={() => openEdit(employe)}
                        className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                        aria-label="Modifier"
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        onClick={() => setDeleting(employe)}
                        className="rounded-sm p-1.5 text-ink-400 hover:bg-status-danger-bg hover:text-status-danger-text"
                        aria-label="Supprimer"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState icon={Users} title="Aucun employé" description="Commence par créer un employé." />
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? "Modifier l'employé" : "Nouvel employé"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <TextField
            label="Matricule"
            value={form.matricule}
            onChange={(e) => setForm({ ...form, matricule: e.target.value })}
            error={fieldErrors.matricule}
            required
          />
          <TextField
            label="Nom"
            value={form.nom}
            onChange={(e) => setForm({ ...form, nom: e.target.value })}
            error={fieldErrors.nom}
            required
          />
          <TextField
            label="Département"
            value={form.departement}
            onChange={(e) => setForm({ ...form, departement: e.target.value })}
            error={fieldErrors.departement}
          />
          <TextField
            label="Email"
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            error={fieldErrors.email}
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={isSaving}>
              {isSaving ? "Enregistrement..." : "Enregistrer"}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title="Supprimer l'employé"
        message={`Supprimer définitivement ${deleting?.nom} (${deleting?.matricule}) ?`}
        onCancel={() => setDeleting(null)}
        onConfirm={() => deleting && supprimerMutation.mutate(deleting.id)}
      />
    </div>
  )
}
