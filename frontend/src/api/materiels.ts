import { api } from "./client"
import type { MaterielRequest, MaterielResponse } from "./types"

export const materielsApi = {
  findAll: () => api.get<MaterielResponse[]>("/materiels").then((r) => r.data),
  findById: (id: number) => api.get<MaterielResponse>(`/materiels/${id}`).then((r) => r.data),
  creer: (data: MaterielRequest) => api.post<MaterielResponse>("/materiels", data).then((r) => r.data),
  modifier: (id: number, data: MaterielRequest) =>
    api.put<MaterielResponse>(`/materiels/${id}`, data).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/materiels/${id}`).then(() => undefined),
}
