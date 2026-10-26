import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { Pencil, Plus, Tags, Trash2 } from "lucide-react"
import { categoriesApi } from "../api/categories"
import type { CategorieRequest, CategorieResponse } from "../api/types"
import { PageHeader } from "../components/PageHeader"
import { Button } from "../components/Button"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Modal } from "../components/Modal"
import { ConfirmDialog } from "../components/ConfirmDialog"
import { TextField } from "../components/Form"
import { useToast } from "../components/Toast"
import { getErrorMessage } from "../lib/errors"
import { useAuth } from "../lib/auth"

const EMPTY_FORM: CategorieRequest = { code: "", libelle: "" }

export function Categories() {
  const { peutModifier } = useAuth()
  const queryClient = useQueryClient()
  const notify = useToast()
  const { data: categories, isLoading } = useQuery({ queryKey: ["categories"], queryFn: categoriesApi.findAll })

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<CategorieResponse | null>(null)
  const [form, setForm] = useState<CategorieRequest>(EMPTY_FORM)
  const [deleting, setDeleting] = useState<CategorieResponse | null>(null)

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["categories"] })

  const creerMutation = useMutation({
    mutationFn: (data: CategorieRequest) => categoriesApi.creer(data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Catégorie créée avec succès")
    },
    onError: (error) => notify("error", getErrorMessage(error)),
  })

  const modifierMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CategorieRequest }) => categoriesApi.modifier(id, data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Catégorie modifiée avec succès")
    },
    onError: (error) => notify("error", getErrorMessage(error)),
  })

  const supprimerMutation = useMutation({
    mutationFn: (id: number) => categoriesApi.supprimer(id),
    onSuccess: () => {
      invalidate()
      setDeleting(null)
      notify("success", "Catégorie supprimée")
    },
    onError: (error) => {
      setDeleting(null)
      notify("error", getErrorMessage(error))
    },
  })

  function openCreate() {
    setEditing(null)
    setForm(EMPTY_FORM)
    setModalOpen(true)
  }

  function openEdit(categorie: CategorieResponse) {
    setEditing(categorie)
    setForm({ code: categorie.code ?? "", libelle: categorie.libelle ?? "" })
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
        title="Catégories"
        description="Les types de matériel (ordinateur, écran, téléphone...)"
        action={
          peutModifier && (
            <Button onClick={openCreate}>
              <Plus size={16} /> Nouvelle catégorie
            </Button>
          )
        }
      />

      {isLoading ? (
        <Spinner />
      ) : categories && categories.length > 0 ? (
        <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-2.5">Code</th>
                <th className="px-4 py-2.5">Libellé</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {categories.map((categorie) => (
                <tr key={categorie.id} className="group">
                  <td className="px-4 py-2.5 font-medium text-ink-900">{categorie.code || "—"}</td>
                  <td className="px-4 py-2.5 text-ink-700">{categorie.libelle || "—"}</td>
                  <td className="px-4 py-2.5">
                    {peutModifier && (
                      <div className="flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                        <button
                          onClick={() => openEdit(categorie)}
                          className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                          aria-label="Modifier"
                        >
                          <Pencil size={16} />
                        </button>
                        <button
                          onClick={() => setDeleting(categorie)}
                          className="rounded-sm p-1.5 text-ink-400 hover:bg-status-danger-bg hover:text-status-danger-text"
                          aria-label="Supprimer"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState icon={Tags} title="Aucune catégorie" description="Commence par créer une catégorie de matériel." />
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? "Modifier la catégorie" : "Nouvelle catégorie"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <TextField label="Code" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
          <TextField label="Libellé" value={form.libelle} onChange={(e) => setForm({ ...form, libelle: e.target.value })} />
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
        title="Supprimer la catégorie"
        message={`Supprimer définitivement la catégorie "${deleting?.libelle}" ?`}
        onCancel={() => setDeleting(null)}
        onConfirm={() => deleting && supprimerMutation.mutate(deleting.id)}
      />
    </div>
  )
}
