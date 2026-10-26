import { useMutation, useQuery } from "@tanstack/react-query"
import { AlertTriangle, ArrowRightLeft, Laptop, Mail, Users } from "lucide-react"
import { Link } from "react-router-dom"
import { employesApi } from "../api/employes"
import { materielsApi } from "../api/materiels"
import { pretsApi } from "../api/prets"
import { PageHeader } from "../components/PageHeader"
import { StatCard } from "../components/StatCard"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Badge } from "../components/Badge"
import { Button } from "../components/Button"
import { useToast } from "../components/Toast"
import { useAuth } from "../lib/auth"
import { getErrorMessage } from "../lib/errors"
import { formatDate, joursDeRetard } from "../lib/format"

export function Dashboard() {
  const { peutModifier } = useAuth()
  const notify = useToast()
  const employesQuery = useQuery({ queryKey: ["employes"], queryFn: employesApi.findAll })
  const materielsQuery = useQuery({ queryKey: ["materiels"], queryFn: materielsApi.findAll })
  const pretsQuery = useQuery({ queryKey: ["prets"], queryFn: pretsApi.findAll })

  const rappelsMutation = useMutation({
    mutationFn: pretsApi.envoyerRappelsRetard,
    onSuccess: ({ emailsEnvoyes }) =>
      notify("success", emailsEnvoyes === 0 ? "Aucun email envoyé (pas d'adresse email)" : `${emailsEnvoyes} rappel(s) envoyé(s) par email`),
    onError: (error) => notify("error", getErrorMessage(error)),
  })

  if (employesQuery.isLoading || materielsQuery.isLoading || pretsQuery.isLoading) {
    return <Spinner label="Chargement du tableau de bord..." />
  }

  const prets = pretsQuery.data ?? []
  const pretsEnCours = prets.filter((pret) => pret.statut === "EN_COURS")
  const pretsEnRetard = prets.filter((pret) => pret.enRetard)

  return (
    <div>
      <PageHeader title="Tableau de bord" description="Vue d'ensemble du parc matériel et des prêts en cours" />

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard index={0} label="Matériels" value={materielsQuery.data?.length ?? 0} icon={Laptop} tone="blue" />
        <StatCard index={1} label="Employés" value={employesQuery.data?.length ?? 0} icon={Users} tone="slate" />
        <StatCard index={2} label="Prêts en cours" value={pretsEnCours.length} icon={ArrowRightLeft} tone="blue" />
        <StatCard
          index={3}
          label="Prêts en retard"
          value={pretsEnRetard.length}
          icon={AlertTriangle}
          tone={pretsEnRetard.length > 0 ? "red" : "slate"}
        />
      </div>

      <div className="mt-8">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-ink-500">Prêts en retard</h2>
          {peutModifier && pretsEnRetard.length > 0 && (
            <Button variant="secondary" onClick={() => rappelsMutation.mutate()} disabled={rappelsMutation.isPending}>
              <Mail size={16} /> {rappelsMutation.isPending ? "Envoi..." : "Envoyer les rappels par email"}
            </Button>
          )}
        </div>
        {pretsEnRetard.length === 0 ? (
          <EmptyState icon={AlertTriangle} title="Aucun prêt en retard" description="Tous les prêts en cours sont dans les délais." />
        ) : (
          <div className="overflow-hidden rounded-sm border border-ink-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
                <tr>
                  <th className="px-4 py-2.5">Matériel</th>
                  <th className="px-4 py-2.5">Employé</th>
                  <th className="px-4 py-2.5">Retour prévu</th>
                  <th className="px-4 py-2.5">Retard</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {pretsEnRetard.map((pret) => (
                  <tr key={pret.id}>
                    <td className="px-4 py-2.5 font-medium text-ink-900">{pret.materiel.numeroInventaire}</td>
                    <td className="px-4 py-2.5 text-ink-600">{pret.employe.nom}</td>
                    <td className="px-4 py-2.5 text-ink-600">{formatDate(pret.dateRetourPrevue)}</td>
                    <td className="px-4 py-2.5">
                      <Badge tone="red">{joursDeRetard(pret.dateRetourPrevue)} j</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Link to="/prets" className="mt-3 inline-block text-sm font-medium text-brand-primary-700 hover:text-brand-primary-800">
          Voir tous les prêts →
        </Link>
      </div>
    </div>
  )
}
