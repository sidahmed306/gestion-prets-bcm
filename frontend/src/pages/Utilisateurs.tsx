import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { KeyRound, Pencil, Plus, ShieldCheck, Trash2 } from "lucide-react"
import { utilisateursApi } from "../api/utilisateurs"
import type { Role, UtilisateurCreateRequest, UtilisateurResponse, UtilisateurUpdateRequest } from "../api/types"
import { PageHeader } from "../components/PageHeader"
import { Button } from "../components/Button"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Modal } from "../components/Modal"
import { ConfirmDialog } from "../components/ConfirmDialog"
import { SelectField, TextField } from "../components/Form"
import { Badge } from "../components/Badge"
import { useToast } from "../components/Toast"
import { ROLE_LIBELLES, useAuth } from "../lib/auth"
import { getErrorMessage } from "../lib/errors"
import { formatDateTime } from "../lib/format"

const ROLES: Role[] = ["ADMIN", "GESTIONNAIRE", "LECTEUR"]

const ROLE_DESCRIPTIONS: Record<Role, string> = {
  ADMIN: "Tous les droits, y compris la gestion des comptes",
  GESTIONNAIRE: "Gère les employés, matériels, catégories et prêts",
  LECTEUR: "Consultation uniquement",
}

const EMPTY_CREATE: UtilisateurCreateRequest = { username: "", motDePasse: "", nomComplet: "", role: "GESTIONNAIRE" }

export function Utilisateurs() {
  const queryClient = useQueryClient()
  const notify = useToast()
  const { utilisateur: moi } = useAuth()
  const { data: utilisateurs, isLoading } = useQuery({ queryKey: ["utilisateurs"], queryFn: utilisateursApi.findAll })

  const [creation, setCreation] = useState(false)
  const [createForm, setCreateForm] = useState<UtilisateurCreateRequest>(EMPTY_CREATE)
  const [editing, setEditing] = useState<UtilisateurResponse | null>(null)
  const [editForm, setEditForm] = useState<UtilisateurUpdateRequest>({ nomComplet: "", role: "LECTEUR", actif: true })
  const [resetCible, setResetCible] = useState<UtilisateurResponse | null>(null)
  const [nouveauMotDePasse, setNouveauMotDePasse] = useState("")
  const [deleting, setDeleting] = useState<UtilisateurResponse | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["utilisateurs"] })
  const onError = (error: unknown) => {
    setFieldErrors((error as any).fieldErrors ?? {})
    notify("error", getErrorMessage(error))
  }

  const creerMutation = useMutation({
    mutationFn: (data: UtilisateurCreateRequest) => utilisateursApi.creer(data),
    onSuccess: () => {
      invalidate()
      setCreation(false)
      notify("success", "Compte créé avec succès")
    },
    onError,
  })

  const modifierMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UtilisateurUpdateRequest }) => utilisateursApi.modifier(id, data),
    onSuccess: () => {
      invalidate()
      setEditing(null)
      notify("success", "Compte modifié avec succès")
    },
    onError,
  })

  const resetMutation = useMutation({
    mutationFn: ({ id, motDePasse }: { id: number; motDePasse: string }) =>
      utilisateursApi.reinitialiserMotDePasse(id, motDePasse),
    onSuccess: () => {
      setResetCible(null)
      notify("success", "Mot de passe réinitialisé")
    },
    onError,
  })

  const supprimerMutation = useMutation({
    mutationFn: (id: number) => utilisateursApi.supprimer(id),
    onSuccess: () => {
      invalidate()
      setDeleting(null)
      notify("success", "Compte supprimé")
    },
    onError: (error) => {
      setDeleting(null)
      notify("error", getErrorMessage(error))
    },
  })

  function openCreate() {
    setCreateForm(EMPTY_CREATE)
    setFieldErrors({})
    setCreation(true)
  }

  function openEdit(utilisateur: UtilisateurResponse) {
    setEditing(utilisateur)
    setEditForm({ nomComplet: utilisateur.nomComplet, role: utilisateur.role, actif: utilisateur.actif })
    setFieldErrors({})
  }

  function openReset(utilisateur: UtilisateurResponse) {
    setResetCible(utilisateur)
    setNouveauMotDePasse("")
    setFieldErrors({})
  }

  const estMoi = (utilisateur: UtilisateurResponse) => utilisateur.id === moi?.id

  return (
    <div>
      <PageHeader
        title="Utilisateurs"
        description="Les comptes qui peuvent se connecter à l'application et leurs droits"
        action={
          <Button onClick={openCreate}>
            <Plus size={16} /> Nouvel utilisateur
          </Button>
        }
      />

      {isLoading ? (
        <Spinner />
      ) : utilisateurs && utilisateurs.length > 0 ? (
        <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-2.5">Identifiant</th>
                <th className="px-4 py-2.5">Nom</th>
                <th className="px-4 py-2.5">Rôle</th>
                <th className="px-4 py-2.5">Statut</th>
                <th className="px-4 py-2.5">Dernière connexion</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {utilisateurs.map((utilisateur) => (
                <tr key={utilisateur.id} className="group">
                  <td className="px-4 py-2.5 font-medium text-ink-900">
                    {utilisateur.username}
                    {estMoi(utilisateur) && <span className="ml-1.5 text-xs font-normal text-ink-400">(vous)</span>}
                  </td>
                  <td className="px-4 py-2.5 text-ink-700">{utilisateur.nomComplet}</td>
                  <td className="px-4 py-2.5">
                    <Badge tone={utilisateur.role === "ADMIN" ? "blue" : "slate"}>{ROLE_LIBELLES[utilisateur.role]}</Badge>
                  </td>
                  <td className="px-4 py-2.5">
                    {utilisateur.actif ? <Badge tone="green">Actif</Badge> : <Badge tone="red">Désactivé</Badge>}
                  </td>
                  <td className="px-4 py-2.5 text-ink-500">{formatDateTime(utilisateur.derniereConnexion)}</td>
                  <td className="px-4 py-2.5">
                    <div className="flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                      <button
                        onClick={() => openEdit(utilisateur)}
                        className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                        aria-label="Modifier"
                        title="Modifier"
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        onClick={() => openReset(utilisateur)}
                        className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                        aria-label="Réinitialiser le mot de passe"
                        title="Réinitialiser le mot de passe"
                      >
                        <KeyRound size={16} />
                      </button>
                      {!estMoi(utilisateur) && (
                        <button
                          onClick={() => setDeleting(utilisateur)}
                          className="rounded-sm p-1.5 text-ink-400 hover:bg-status-danger-bg hover:text-status-danger-text"
                          aria-label="Supprimer"
                          title="Supprimer"
                        >
                          <Trash2 size={16} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState icon={ShieldCheck} title="Aucun utilisateur" description="Créez un premier compte." />
      )}

      <Modal open={creation} onClose={() => setCreation(false)} title="Nouvel utilisateur">
        <form
          onSubmit={(e) => {
            e.preventDefault()
            creerMutation.mutate(createForm)
          }}
          className="space-y-4"
        >
          <TextField
            label="Identifiant de connexion"
            value={createForm.username}
            onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
            error={fieldErrors.username}
            autoComplete="off"
            required
          />
          <TextField
            label="Nom complet"
            value={createForm.nomComplet}
            onChange={(e) => setCreateForm({ ...createForm, nomComplet: e.target.value })}
            error={fieldErrors.nomComplet}
            required
          />
          <RoleField
            value={createForm.role}
            onChange={(role) => setCreateForm({ ...createForm, role })}
            error={fieldErrors.role}
          />
          <TextField
            label="Mot de passe initial (8 caractères minimum)"
            type="password"
            value={createForm.motDePasse}
            onChange={(e) => setCreateForm({ ...createForm, motDePasse: e.target.value })}
            error={fieldErrors.motDePasse}
            autoComplete="new-password"
            minLength={8}
            required
          />
          <FormActions onCancel={() => setCreation(false)} saving={creerMutation.isPending} />
        </form>
      </Modal>

      <Modal open={editing !== null} onClose={() => setEditing(null)} title={`Modifier ${editing?.username ?? ""}`}>
        <form
          onSubmit={(e) => {
            e.preventDefault()
            if (editing) modifierMutation.mutate({ id: editing.id, data: editForm })
          }}
          className="space-y-4"
        >
          <TextField
            label="Nom complet"
            value={editForm.nomComplet}
            onChange={(e) => setEditForm({ ...editForm, nomComplet: e.target.value })}
            error={fieldErrors.nomComplet}
            required
          />
          <RoleField
            value={editForm.role}
            onChange={(role) => setEditForm({ ...editForm, role })}
            error={fieldErrors.role}
            disabled={editing !== null && estMoi(editing)}
          />
          <label className="flex items-center gap-2 text-sm text-ink-700">
            <input
              type="checkbox"
              checked={editForm.actif}
              onChange={(e) => setEditForm({ ...editForm, actif: e.target.checked })}
              disabled={editing !== null && estMoi(editing)}
              className="h-4 w-4 accent-brand-primary-700"
            />
            Compte actif (décocher pour bloquer la connexion)
          </label>
          {editing !== null && estMoi(editing) && (
            <p className="text-xs text-ink-500">Vous ne pouvez pas modifier votre propre rôle ni désactiver votre compte.</p>
          )}
          <FormActions onCancel={() => setEditing(null)} saving={modifierMutation.isPending} />
        </form>
      </Modal>

      <Modal
        open={resetCible !== null}
        onClose={() => setResetCible(null)}
        title={`Nouveau mot de passe pour ${resetCible?.username ?? ""}`}
      >
        <form
          onSubmit={(e) => {
            e.preventDefault()
            if (resetCible) resetMutation.mutate({ id: resetCible.id, motDePasse: nouveauMotDePasse })
          }}
          className="space-y-4"
        >
          <p className="text-sm text-ink-600">Communiquez ce mot de passe à l'utilisateur. Il pourra ensuite le changer.</p>
          <TextField
            label="Nouveau mot de passe (8 caractères minimum)"
            type="password"
            value={nouveauMotDePasse}
            onChange={(e) => setNouveauMotDePasse(e.target.value)}
            error={fieldErrors.nouveauMotDePasse}
            autoComplete="new-password"
            minLength={8}
            required
          />
          <FormActions onCancel={() => setResetCible(null)} saving={resetMutation.isPending} />
        </form>
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title="Supprimer le compte"
        message={`Supprimer définitivement le compte ${deleting?.username} ? Pour bloquer temporairement l'accès, préférez le désactiver.`}
        onCancel={() => setDeleting(null)}
        onConfirm={() => deleting && supprimerMutation.mutate(deleting.id)}
      />
    </div>
  )
}

function RoleField({
  value,
  onChange,
  error,
  disabled,
}: {
  value: Role
  onChange: (role: Role) => void
  error?: string
  disabled?: boolean
}) {
  return (
    <div>
      <SelectField label="Rôle" value={value} onChange={(e) => onChange(e.target.value as Role)} error={error} disabled={disabled}>
        {ROLES.map((role) => (
          <option key={role} value={role}>
            {ROLE_LIBELLES[role]}
          </option>
        ))}
      </SelectField>
      <p className="mt-1 text-xs text-ink-500">{ROLE_DESCRIPTIONS[value]}</p>
    </div>
  )
}

function FormActions({ onCancel, saving }: { onCancel: () => void; saving: boolean }) {
  return (
    <div className="flex justify-end gap-2 pt-2">
      <Button type="button" variant="secondary" onClick={onCancel}>
        Annuler
      </Button>
      <Button type="submit" disabled={saving}>
        {saving ? "Enregistrement..." : "Enregistrer"}
      </Button>
    </div>
  )
}
