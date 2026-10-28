import { useQuery } from "@tanstack/react-query"
import { comptabiliteApi } from "../api/comptabilite"
import { Modal } from "./Modal"
import { Spinner } from "./Spinner"
import { formatDate, formatMontant } from "../lib/format"
import { getErrorMessage } from "../lib/errors"

const anneeEnCours = new Date().getFullYear()

/** Le tableau d'amortissement d'un matériel, année par année. */
export function PlanAmortissementModal({ materielId, onClose }: { materielId: number | null; onClose: () => void }) {
  const { data: plan, isLoading, error } = useQuery({
    queryKey: ["plan-amortissement", materielId],
    queryFn: () => comptabiliteApi.plan(materielId!),
    enabled: materielId !== null,
  })

  return (
    <Modal open={materielId !== null} onClose={onClose} title="Plan d'amortissement" large>
      {isLoading ? (
        <Spinner />
      ) : error ? (
        <p className="text-sm text-status-danger-text">{getErrorMessage(error)}</p>
      ) : plan ? (
        <div>
          <dl className="mb-4 grid grid-cols-2 gap-x-6 gap-y-1.5 text-sm">
            <dt className="text-ink-500">Matériel</dt>
            <dd className="font-medium text-ink-900">
              {plan.numeroInventaire} {plan.designation && `— ${plan.designation}`}
            </dd>
            <dt className="text-ink-500">Prix d'achat</dt>
            <dd className="text-ink-900">{formatMontant(plan.prixAchat)}</dd>
            <dt className="text-ink-500">Date d'achat</dt>
            <dd className="text-ink-900">{formatDate(plan.dateAchat)}</dd>
            <dt className="text-ink-500">Méthode</dt>
            <dd className="text-ink-900">
              Linéaire sur {plan.dureeAmortissementAnnees} an(s), annuité {formatMontant(plan.annuite)}
            </dd>
          </dl>
          <div className="overflow-x-auto rounded-sm border border-ink-200">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-ink-200 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
                <tr>
                  <th className="px-3 py-2">Exercice</th>
                  <th className="px-3 py-2 text-right">Dotation</th>
                  <th className="px-3 py-2 text-right">Cumul</th>
                  <th className="px-3 py-2 text-right">Valeur nette</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {plan.lignes.map((ligne) => (
                  <tr key={ligne.exercice} className={ligne.exercice === anneeEnCours ? "bg-brand-primary-50" : undefined}>
                    <td className="px-3 py-2 font-medium text-ink-900">
                      {ligne.exercice}
                      {ligne.exercice === anneeEnCours && <span className="ml-1.5 text-xs font-normal text-ink-500">(en cours)</span>}
                    </td>
                    <td className="px-3 py-2 text-right tabular-nums text-ink-700">{formatMontant(ligne.dotation)}</td>
                    <td className="px-3 py-2 text-right tabular-nums text-ink-700">{formatMontant(ligne.cumul)}</td>
                    <td className="px-3 py-2 text-right tabular-nums font-medium text-ink-900">{formatMontant(ligne.valeurNette)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="mt-3 text-xs text-ink-500">
            La 1re année est calculée au prorata des jours depuis la date d'achat ; le reste est reporté sur la dernière ligne.
          </p>
        </div>
      ) : null}
    </Modal>
  )
}
