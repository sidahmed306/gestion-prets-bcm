import { api } from "./client"
import type { SynchronisationResponse } from "./types"

export const annuaireApi = {
  synchroniserEmployes: () =>
    api.post<SynchronisationResponse>("/annuaire/synchroniser-employes").then((r) => r.data),
}
