import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { Link } from "react-router-dom"
import { Calculator, Coins, Landmark, TrendingDown, Wallet } from "lucide-react"
import { comptabiliteApi } from "../api/comptabilite"
import { materielsApi } from "../api/materiels"
import { PageHeader } from "../components/PageHeader"
import { StatCard } from "../components/StatCard"
import { Spinner } from "../components/Spinner"
import { EmptyState } from "../components/EmptyState"
import { Badge } from "../components/Badge"
import { PlanAmortissementModal } from "../components/PlanAmortissementModal"
import { formatDate, formatMontant } from "../lib/format"

function pourcentage(partie: number, total: number): number {
  return total === 0 ? 0 : Math.round((partie / total) * 1000) / 10
}

export function Comptabilite() {
  const syntheseQuery = useQuery({ queryKey: ["comptabilite", "synthese"], queryFn: comptabiliteApi.synthese })
  const materielsQuery = useQuery({ queryKey: ["materiels"], queryFn: materielsApi.findAll })
  const [planMaterielId, setPlanMaterielId] = useState<number | null>(null)

  if (syntheseQuery.isLoading || materielsQuery.isLoading) {
    return <Spinner label="Calcul des valeurs..." />
  }

  const synthese = syntheseQuery.data
  if (!synthese) return null

  const materielsValorises = (materielsQuery.data ?? [])
    .filter((materiel) => materiel.situationComptable !== null)
    .sort((a, b) => b.situationComptable!.valeurNetteComptable - a.situationComptable!.valeurNetteComptable)
  const annee = new Date(synthese.dateCalcul).getFullYear()

  return (
    <div>
      <PageHeader
        title="Comptabilité"
        description={`Valeur du parc matériel au ${formatDate(synthese.dateCalcul)} — amortissement linéaire`}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <StatCard index={0} label="Valeur d'achat du parc" value={formatMontant(synthese.valeurAchatTotale)} icon={Wallet} tone="slate" />
        <StatCard index={1} label="Valeur actuelle (nette)" value={formatMontant(synthese.valeurNetteTotale)} icon={Landmark} tone="blue" />
        <StatCard index={2} label="Amortissements cumulés" value={formatMontant(synthese.amortissementCumuleTotal)} icon={TrendingDown} tone="slate" />
        <StatCard index={3} label={`Dotation de l'exercice ${annee}`} value={formatMontant(synthese.dotationExerciceEnCours)} icon={Coins} tone="slate" />
      </div>

      <p className="mt-4 text-sm text-ink-600">
        {synthese.nombreValorises} matériel(s) valorisé(s) sur {synthese.nombreMateriels}
        {synthese.nombreTotalementAmortis > 0 && <> — dont {synthese.nombreTotalementAmortis} totalement amorti(s), à envisager pour un renouvellement</>}.
      </p>
      {synthese.nombreNonValorises > 0 && (
        <p className="mt-1 text-sm text-ink-500">
          {synthese.nombreNonValorises} matériel(s) sans prix d'achat ne sont pas comptés.{" "}
          <Link to="/materiels" className="font-medium text-brand-primary-700 hover:text-brand-primary-800">
            Compléter dans la page Matériels →
          </Link>
        </p>
      )}

      {synthese.nombreValorises === 0 ? (
        <div className="mt-8">
          <EmptyState
            icon={Calculator}
            title="Aucun matériel valorisé"
            description="Renseignez le prix, la date d'achat et la durée d'amortissement d'un matériel pour voir sa valeur ici."
          />
        </div>
      ) : (
        <>
          <h2 className="mb-3 mt-8 text-xs font-semibold uppercase tracking-wide text-ink-500">Par catégorie</h2>
          <div className="overflow-x-auto rounded-sm border border-ink-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
                <tr>
                  <th className="px-4 py-2.5">Catégorie</th>
                  <th className="px-4 py-2.5 text-right">Nombre</th>
                  <th className="px-4 py-2.5 text-right">Valeur d'achat</th>
                  <th className="px-4 py-2.5 text-right">Valeur actuelle</th>
                  <th className="px-4 py-2.5 text-right">Amorti</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {synthese.parCategorie.map((ligne) => (
                  <tr key={ligne.categorie}>
                    <td className="px-4 py-2.5 font-medium text-ink-900">{ligne.categorie}</td>
                    <td className="px-4 py-2.5 text-right tabular-nums text-ink-600">{ligne.nombre}</td>
                    <td className="px-4 py-2.5 text-right tabular-nums text-ink-600">{formatMontant(ligne.valeurAchat)}</td>
                    <td className="px-4 py-2.5 text-right tabular-nums font-medium text-ink-900">{formatMontant(ligne.valeurNette)}</td>
                    <td className="px-4 py-2.5 text-right tabular-nums text-ink-600">
                      {pourcentage(ligne.valeurAchat - ligne.valeurNette, ligne.valeurAchat)} %
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <h2 className="mb-3 mt-8 text-xs font-semibold uppercase tracking-wide text-ink-500">Détail par matériel</h2>
          <div className="overflow-x-auto rounded-sm border border-ink-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
                <tr>
                  <th className="px-4 py-2.5">Matériel</th>
                  <th className="px-4 py-2.5">Achat</th>
                  <th className="px-4 py-2.5 text-right">Prix d'achat</th>
                  <th className="px-4 py-2.5 text-right">Valeur actuelle</th>
                  <th className="px-4 py-2.5">Amorti</th>
                  <th className="px-4 py-2.5" />
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {materielsValorises.map((materiel) => {
                  const situation = materiel.situationComptable!
                  return (
                    <tr key={materiel.id}>
                      <td className="px-4 py-2.5">
                        <span className="font-medium text-ink-900">{materiel.numeroInventaire}</span>
                        {materiel.designation && <span className="ml-1.5 text-ink-500">{materiel.designation}</span>}
                      </td>
                      <td className="px-4 py-2.5 text-ink-600">{formatDate(materiel.dateAchat)}</td>
                      <td className="px-4 py-2.5 text-right tabular-nums text-ink-600">{formatMontant(materiel.prixAchat)}</td>
                      <td className="px-4 py-2.5 text-right tabular-nums font-medium text-ink-900">
                        {formatMontant(situation.valeurNetteComptable)}
                      </td>
                      <td className="px-4 py-2.5">
                        {situation.totalementAmorti ? (
                          <Badge tone="red">Totalement amorti</Badge>
                        ) : (
                          <div className="flex items-center gap-2" title={`Fin d'amortissement : ${formatDate(situation.dateFinAmortissement)}`}>
                            <div className="h-1.5 w-20 overflow-hidden rounded-full bg-ink-100">
                              <div
                                className="h-full rounded-full bg-brand-primary-600"
                                style={{ width: `${situation.pourcentageAmorti}%` }}
                              />
                            </div>
                            <span className="tabular-nums text-xs text-ink-500">{situation.pourcentageAmorti} %</span>
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-2.5 text-right">
                        <button
                          onClick={() => setPlanMaterielId(materiel.id)}
                          className="inline-flex items-center gap-1.5 rounded-sm px-2 py-1 text-sm font-medium text-brand-primary-700 hover:bg-brand-primary-50"
                        >
                          <Calculator size={14} /> Plan
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      <PlanAmortissementModal materielId={planMaterielId} onClose={() => setPlanMaterielId(null)} />
    </div>
  )
}
