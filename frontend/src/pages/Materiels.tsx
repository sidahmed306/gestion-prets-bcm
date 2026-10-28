import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useSearchParams } from "react-router-dom"
import { Calculator, Laptop, Pencil, Plus, Trash2 } from "lucide-react"
import clsx from "clsx"
import { materielsApi } from "../api/materiels"
import { categoriesApi } from "../api/categories"
import { pretsApi } from "../api/prets"
import type { MaterielRequest, MaterielResponse } from "../api/types"
import { PageHeader } from "../components/PageHeader"
import { Button } from "../components/Button"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Modal } from "../components/Modal"
import { ConfirmDialog } from "../components/ConfirmDialog"
import { TextField, SelectField } from "../components/Form"
import { Badge } from "../components/Badge"
import { useToast } from "../components/Toast"
import { getErrorMessage } from "../lib/errors"
import { useAuth } from "../lib/auth"
import { formatMontant, todayIso } from "../lib/format"
import { PlanAmortissementModal } from "../components/PlanAmortissementModal"

type Filtre = "tous" | "disponibles" | "empruntes"

const EMPTY_FORM: MaterielRequest = {
  numeroInventaire: "",
  designation: "",
  categorieId: 0,
  marque: "",
  modele: "",
  etatPhysique: "",
  prixAchat: null,
  dateAchat: null,
  dureeAmortissementAnnees: null,
  fournisseur: "",
}

export function Materiels() {
  const { peutModifier } = useAuth()
  const queryClient = useQueryClient()
  const notify = useToast()
  const { data: materiels, isLoading } = useQuery({ queryKey: ["materiels"], queryFn: materielsApi.findAll })
  const { data: categories } = useQuery({ queryKey: ["categories"], queryFn: categoriesApi.findAll })
  const { data: prets } = useQuery({ queryKey: ["prets"], queryFn: pretsApi.findAll })

  const [searchParams, setSearchParams] = useSearchParams()
  const filtreParam = searchParams.get("filtre")
  const filtre: Filtre = filtreParam === "disponibles" || filtreParam === "empruntes" ? filtreParam : "tous"
  const setFiltre = (value: Filtre) => setSearchParams(value === "tous" ? {} : { filtre: value })

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<MaterielResponse | null>(null)
  const [form, setForm] = useState<MaterielRequest>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [deleting, setDeleting] = useState<MaterielResponse | null>(null)
  const [planMaterielId, setPlanMaterielId] = useState<number | null>(null)

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["materiels"] })

  const creerMutation = useMutation({
    mutationFn: (data: MaterielRequest) => materielsApi.creer(data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Matériel créé avec succès")
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  const modifierMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: MaterielRequest }) => materielsApi.modifier(id, data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Matériel modifié avec succès")
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  const supprimerMutation = useMutation({
    mutationFn: (id: number) => materielsApi.supprimer(id),
    onSuccess: () => {
      invalidate()
      setDeleting(null)
      notify("success", "Matériel supprimé")
    },
    onError: (error) => {
      setDeleting(null)
      notify("error", getErrorMessage(error))
    },
  })

  function openCreate() {
    setEditing(null)
    setForm({ ...EMPTY_FORM, categorieId: categories?.[0]?.id ?? 0 })
    setFieldErrors({})
    setModalOpen(true)
  }

  function openEdit(materiel: MaterielResponse) {
    setEditing(materiel)
    setForm({
      numeroInventaire: materiel.numeroInventaire,
      designation: materiel.designation ?? "",
      categorieId: materiel.categorie.id,
      marque: materiel.marque ?? "",
      modele: materiel.modele ?? "",
      etatPhysique: materiel.etatPhysique ?? "",
      prixAchat: materiel.prixAchat,
      dateAchat: materiel.dateAchat,
      dureeAmortissementAnnees: materiel.dureeAmortissementAnnees,
      fournisseur: materiel.fournisseur ?? "",
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

  const materielsEmpruntesIds = new Set(
    (prets ?? []).filter((pret) => pret.statut === "EN_COURS").map((pret) => pret.materiel.id),
  )

  const materielsFiltres = (materiels ?? []).filter((materiel) => {
    if (filtre === "disponibles") return !materielsEmpruntesIds.has(materiel.id)
    if (filtre === "empruntes") return materielsEmpruntesIds.has(materiel.id)
    return true
  })

  const filtres: { value: Filtre; label: string }[] = [
    { value: "tous", label: "Tous" },
    { value: "disponibles", label: "Disponibles" },
    { value: "empruntes", label: "Empruntés" },
  ]

  return (
    <div>
      <PageHeader
        title="Matériels"
        description="Le parc informatique disponible au prêt"
        action={
          peutModifier && (
            <Button onClick={openCreate}>
              <Plus size={16} /> Nouveau matériel
            </Button>
          )
        }
      />

      <div className="mb-4 flex gap-1 border-b border-ink-200 w-fit">
        {filtres.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => setFiltre(value)}
            className={clsx(
              "border-b-2 px-3 py-2 text-sm font-medium transition-colors",
              filtre === value
                ? "border-brand-primary-700 text-brand-primary-800"
                : "border-transparent text-ink-500 hover:text-ink-700",
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <Spinner />
      ) : materielsFiltres.length > 0 ? (
        <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-2.5">N° inventaire</th>
                <th className="px-4 py-2.5">Désignation</th>
                <th className="px-4 py-2.5">Catégorie</th>
                <th className="px-4 py-2.5">Marque / Modèle</th>
                <th className="px-4 py-2.5">État</th>
                <th className="px-4 py-2.5">Statut</th>
                <th className="px-4 py-2.5 text-right">Valeur actuelle</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {materielsFiltres.map((materiel) => (
                <tr key={materiel.id} className="group">
                  <td className="px-4 py-2.5 font-medium text-ink-900">{materiel.numeroInventaire}</td>
                  <td className="px-4 py-2.5 text-ink-700">{materiel.designation || "—"}</td>
                  <td className="px-4 py-2.5 text-ink-500">{materiel.categorie.libelle || "—"}</td>
                  <td className="px-4 py-2.5 text-ink-500">
                    {[materiel.marque, materiel.modele].filter(Boolean).join(" ") || "—"}
                  </td>
                  <td className="px-4 py-2.5 text-ink-500">{materiel.etatPhysique || "—"}</td>
                  <td className="px-4 py-2.5">
                    {materielsEmpruntesIds.has(materiel.id) ? (
                      <Badge tone="blue">Emprunté</Badge>
                    ) : (
                      <Badge tone="green">Disponible</Badge>
                    )}
                  </td>
                  <td className="px-4 py-2.5 text-right tabular-nums">
                    {materiel.situationComptable ? (
                      <span
                        className={materiel.situationComptable.totalementAmorti ? "text-ink-400" : "text-ink-900"}
                        title={`Prix d'achat : ${formatMontant(materiel.prixAchat)} — amorti à ${materiel.situationComptable.pourcentageAmorti} %`}
                      >
                        {formatMontant(materiel.situationComptable.valeurNetteComptable)}
                      </span>
                    ) : (
                      <span className="text-ink-400">—</span>
                    )}
                  </td>
                  <td className="px-4 py-2.5">
                    <div className="flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                      {materiel.situationComptable && (
                        <button
                          onClick={() => setPlanMaterielId(materiel.id)}
                          className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                          aria-label="Plan d'amortissement"
                          title="Plan d'amortissement"
                        >
                          <Calculator size={16} />
                        </button>
                      )}
                      {peutModifier && (
                        <>
                          <button
                            onClick={() => openEdit(materiel)}
                            className="rounded-sm p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                            aria-label="Modifier"
                          >
                            <Pencil size={16} />
                          </button>
                          <button
                            onClick={() => setDeleting(materiel)}
                            className="rounded-sm p-1.5 text-ink-400 hover:bg-status-danger-bg hover:text-status-danger-text"
                            aria-label="Supprimer"
                          >
                            <Trash2 size={16} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState icon={Laptop} title="Aucun matériel" description="Aucun matériel ne correspond à ce filtre." />
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? "Modifier le matériel" : "Nouveau matériel"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <TextField
            label="Numéro d'inventaire"
            value={form.numeroInventaire}
            onChange={(e) => setForm({ ...form, numeroInventaire: e.target.value })}
            error={fieldErrors.numeroInventaire}
            required
          />
          <TextField
            label="Désignation"
            value={form.designation}
            onChange={(e) => setForm({ ...form, designation: e.target.value })}
          />
          <SelectField
            label="Catégorie"
            value={form.categorieId}
            onChange={(e) => setForm({ ...form, categorieId: Number(e.target.value) })}
            error={fieldErrors.categorieId}
            required
          >
            <option value={0} disabled>
              Choisir une catégorie
            </option>
            {categories?.map((categorie) => (
              <option key={categorie.id} value={categorie.id}>
                {categorie.libelle}
              </option>
            ))}
          </SelectField>
          <div className="grid grid-cols-2 gap-4">
            <TextField label="Marque" value={form.marque} onChange={(e) => setForm({ ...form, marque: e.target.value })} />
            <TextField label="Modèle" value={form.modele} onChange={(e) => setForm({ ...form, modele: e.target.value })} />
          </div>
          <TextField
            label="État physique"
            value={form.etatPhysique}
            onChange={(e) => setForm({ ...form, etatPhysique: e.target.value })}
            placeholder="Bon, Usé, Endommagé..."
          />
          <fieldset className="space-y-4 border-t border-ink-200 pt-4">
            <legend className="pr-2 text-xs font-semibold uppercase tracking-wide text-ink-500">
              Informations comptables (facultatif)
            </legend>
            <div className="grid grid-cols-2 gap-4">
              <TextField
                label="Prix d'achat (MRU)"
                type="number"
                min={0}
                step="0.01"
                value={form.prixAchat ?? ""}
                onChange={(e) => setForm({ ...form, prixAchat: e.target.value === "" ? null : Number(e.target.value) })}
                error={fieldErrors.prixAchat}
              />
              <TextField
                label="Date d'achat"
                type="date"
                max={todayIso()}
                value={form.dateAchat ?? ""}
                onChange={(e) => setForm({ ...form, dateAchat: e.target.value || null })}
                error={fieldErrors.dateAchat}
              />
              <TextField
                label="Durée d'amortissement (ans)"
                type="number"
                min={1}
                max={50}
                value={form.dureeAmortissementAnnees ?? ""}
                onChange={(e) =>
                  setForm({ ...form, dureeAmortissementAnnees: e.target.value === "" ? null : Number(e.target.value) })
                }
                error={fieldErrors.dureeAmortissementAnnees}
                placeholder="Ex : 3 pour un ordinateur"
              />
              <TextField
                label="Fournisseur"
                value={form.fournisseur}
                onChange={(e) => setForm({ ...form, fournisseur: e.target.value })}
              />
            </div>
            {fieldErrors.infosComptablesCompletes && (
              <p className="text-xs text-status-danger-text">{fieldErrors.infosComptablesCompletes}</p>
            )}
          </fieldset>
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

      <PlanAmortissementModal materielId={planMaterielId} onClose={() => setPlanMaterielId(null)} />

      <ConfirmDialog
        open={deleting !== null}
        title="Supprimer le matériel"
        message={`Supprimer définitivement ${deleting?.numeroInventaire} ?`}
        onCancel={() => setDeleting(null)}
        onConfirm={() => deleting && supprimerMutation.mutate(deleting.id)}
      />
    </div>
  )
}
