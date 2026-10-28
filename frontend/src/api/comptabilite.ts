import { api } from "./client"
import type { PlanAmortissement, SyntheseComptable } from "./types"

export const comptabiliteApi = {
  synthese: () => api.get<SyntheseComptable>("/comptabilite/synthese").then((r) => r.data),
  plan: (materielId: number) =>
    api.get<PlanAmortissement>(`/comptabilite/materiels/${materielId}/plan`).then((r) => r.data),
}
