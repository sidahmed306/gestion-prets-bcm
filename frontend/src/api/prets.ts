import { api } from "./client"
import type { PretRequest, PretResponse, PretRetourRequest } from "./types"

export const pretsApi = {
  findAll: () => api.get<PretResponse[]>("/prets").then((r) => r.data),
  findById: (id: number) => api.get<PretResponse>(`/prets/${id}`).then((r) => r.data),
  findByEmploye: (employeId: number) =>
    api.get<PretResponse[]>(`/prets/employe/${employeId}`).then((r) => r.data),
  findEnRetard: () => api.get<PretResponse[]>("/prets/en-retard").then((r) => r.data),
  creer: (data: PretRequest) => api.post<PretResponse>("/prets", data).then((r) => r.data),
  retourner: (id: number, data: PretRetourRequest) =>
    api.post<PretResponse>(`/prets/${id}/retour`, data).then((r) => r.data),
}
