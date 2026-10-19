import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useSearchParams } from "react-router-dom"
import { ArrowRightLeft, CornerUpLeft, Plus } from "lucide-react"
import clsx from "clsx"
import { pretsApi } from "../api/prets"
import { materielsApi } from "../api/materiels"
import { employesApi } from "../api/employes"
import type { PretRequest, PretResponse } from "../api/types"
import { PageHeader } from "../components/PageHeader"
import { Button } from "../components/Button"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Modal } from "../components/Modal"
import { TextField, SelectField } from "../components/Form"
import { Badge } from "../components/Badge"
import { useToast } from "../components/Toast"
import { getErrorMessage } from "../lib/errors"
import { formatDate, todayIso } from "../lib/format"

type Filtre = "tous" | "en_cours" | "en_retard" | "historique"

const EMPTY_FORM: PretRequest = { materielId: 0, employeId: 0, datePret: todayIso(), dateRetourPrevue: "", commentaire: "" }

function statutDuPret(pret: PretResponse) {
  if (pret.enRetard) return { tone: "red" as const, label: "En retard" }
  if (pret.statut === "EN_COURS") return { tone: "blue" as const, label: "En cours" }
  return { tone: "green" as const, label: "Retourné" }
}

export function Prets() {
  const queryClient = useQueryClient()
  const notify = useToast()
  const { data: prets, isLoading } = useQuery({ queryKey: ["prets"], queryFn: pretsApi.findAll })
  const { data: materiels } = useQuery({ queryKey: ["materiels"], queryFn: materielsApi.findAll })
  const { data: employes } = useQuery({ queryKey: ["employes"], queryFn: employesApi.findAll })

  const [searchParams, setSearchParams] = useSearchParams()
  const filtreParam = searchParams.get("filtre")
  const filtre: Filtre =
    filtreParam === "en_cours" || filtreParam === "en_retard" || filtreParam === "historique" ? filtreParam : "tous"
  const setFiltre = (value: Filtre) => setSearchParams(value === "tous" ? {} : { filtre: value })
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState<PretRequest>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [retourCible, setRetourCible] = useState<PretResponse | null>(null)
  const [dateRetour, setDateRetour] = useState(todayIso())

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["prets"] })
    queryClient.invalidateQueries({ queryKey: ["materiels"] })
  }

  const creerMutation = useMutation({
    mutationFn: (data: PretRequest) => pretsApi.creer(data),
    onSuccess: () => {
      invalidate()
      setModalOpen(false)
      notify("success", "Prêt créé avec succès")
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  const retournerMutation = useMutation({
    mutationFn: ({ id, dateRetourEffective }: { id: number; dateRetourEffective: string }) =>
      pretsApi.retourner(id, { dateRetourEffective }),
    onSuccess: () => {
      invalidate()
      setRetourCible(null)
      notify("success", "Retour enregistré")
    },
    onError: (error) => {
      setRetourCible(null)
      notify("error", getErrorMessage(error))
    },
  })

  const materielsDisponibles = (materiels ?? []).filter(
    (materiel) => !(prets ?? []).some((pret) => pret.materiel.id === materiel.id && pret.statut === "EN_COURS"),
  )

  const pretsFiltres = (prets ?? []).filter((pret) => {
    if (filtre === "en_cours") return pret.statut === "EN_COURS"
    if (filtre === "en_retard") return pret.enRetard
    if (filtre === "historique") return pret.statut === "RETOURNE"
    return true
  })

  function openCreate() {
    setForm({ ...EMPTY_FORM, materielId: materielsDisponibles[0]?.id ?? 0, employeId: employes?.[0]?.id ?? 0 })
    setFieldErrors({})
    setModalOpen(true)
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    creerMutation.mutate(form)
  }

  const filtres: { value: Filtre; label: string }[] = [
    { value: "tous", label: "Tous" },
    { value: "en_cours", label: "En cours" },
    { value: "en_retard", label: "En retard" },
    { value: "historique", label: "Historique" },
  ]

  return (
    <div>
      <PageHeader
        title="Prêts"
        description="Suivi des emprunts de matériel par les employés"
        action={
          <Button onClick={openCreate} disabled={materielsDisponibles.length === 0}>
            <Plus size={16} /> Nouveau prêt
          </Button>
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
      ) : pretsFiltres.length > 0 ? (
        <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-2.5">Matériel</th>
                <th className="px-4 py-2.5">Employé</th>
                <th className="px-4 py-2.5">Prêt le</th>
                <th className="px-4 py-2.5">Retour prévu</th>
                <th className="px-4 py-2.5">Statut</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {pretsFiltres.map((pret) => {
                const statut = statutDuPret(pret)
                return (
                  <tr key={pret.id}>
                    <td className="px-4 py-2.5 font-medium text-ink-900">{pret.materiel.numeroInventaire}</td>
                    <td className="px-4 py-2.5 text-ink-700">{pret.employe.nom}</td>
                    <td className="px-4 py-2.5 text-ink-500">{formatDate(pret.datePret)}</td>
                    <td className="px-4 py-2.5 text-ink-500">{formatDate(pret.dateRetourPrevue)}</td>
                    <td className="px-4 py-2.5">
                      <Badge tone={statut.tone}>{statut.label}</Badge>
                    </td>
                    <td className="px-4 py-2.5 text-right">
                      {pret.statut === "EN_COURS" && (
                        <button
                          onClick={() => {
                            setRetourCible(pret)
                            setDateRetour(todayIso())
                          }}
                          className="inline-flex items-center gap-1.5 rounded-sm px-2 py-1 text-sm font-medium text-brand-primary-700 hover:bg-brand-primary-50"
                        >
                          <CornerUpLeft size={14} /> Retourner
                        </button>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState icon={ArrowRightLeft} title="Aucun prêt" description="Aucun prêt ne correspond à ce filtre." />
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nouveau prêt">
        <form onSubmit={handleSubmit} className="space-y-4">
          <SelectField
            label="Matériel"
            value={form.materielId}
            onChange={(e) => setForm({ ...form, materielId: Number(e.target.value) })}
            error={fieldErrors.materielId}
            required
          >
            <option value={0} disabled>
              Choisir un matériel disponible
            </option>
            {materielsDisponibles.map((materiel) => (
              <option key={materiel.id} value={materiel.id}>
                {materiel.numeroInventaire} — {materiel.designation}
              </option>
            ))}
          </SelectField>
          <SelectField
            label="Employé"
            value={form.employeId}
            onChange={(e) => setForm({ ...form, employeId: Number(e.target.value) })}
            error={fieldErrors.employeId}
            required
          >
            <option value={0} disabled>
              Choisir un employé
            </option>
            {employes?.map((employe) => (
              <option key={employe.id} value={employe.id}>
                {employe.nom} ({employe.matricule})
              </option>
            ))}
          </SelectField>
          <div className="grid grid-cols-2 gap-4">
            <TextField
              label="Date de prêt"
              type="date"
              value={form.datePret}
              onChange={(e) => setForm({ ...form, datePret: e.target.value })}
              error={fieldErrors.datePret}
              required
            />
            <TextField
              label="Retour prévu"
              type="date"
              value={form.dateRetourPrevue}
              onChange={(e) => setForm({ ...form, dateRetourPrevue: e.target.value })}
              error={fieldErrors.dateRetourPrevue}
              required
            />
          </div>
          <TextField
            label="Commentaire"
            value={form.commentaire}
            onChange={(e) => setForm({ ...form, commentaire: e.target.value })}
            placeholder="Mission, remplacement, etc. (facultatif)"
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={creerMutation.isPending}>
              {creerMutation.isPending ? "Création..." : "Créer le prêt"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={retourCible !== null} onClose={() => setRetourCible(null)} title="Enregistrer le retour">
        <div className="space-y-4">
          <p className="text-sm text-ink-600">
            Matériel <span className="font-medium text-ink-900">{retourCible?.materiel.numeroInventaire}</span>, emprunté par{" "}
            <span className="font-medium text-ink-900">{retourCible?.employe.nom}</span>.
          </p>
          <TextField label="Date de retour effective" type="date" value={dateRetour} onChange={(e) => setDateRetour(e.target.value)} />
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" onClick={() => setRetourCible(null)}>
              Annuler
            </Button>
            <Button
              onClick={() => retourCible && retournerMutation.mutate({ id: retourCible.id, dateRetourEffective: dateRetour })}
              disabled={retournerMutation.isPending}
            >
              {retournerMutation.isPending ? "Enregistrement..." : "Confirmer le retour"}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
