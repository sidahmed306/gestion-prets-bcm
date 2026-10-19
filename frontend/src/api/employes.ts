import { api } from "./client"
import type { EmployeRequest, EmployeResponse } from "./types"

export const employesApi = {
  findAll: () => api.get<EmployeResponse[]>("/employes").then((r) => r.data),
  findById: (id: number) => api.get<EmployeResponse>(`/employes/${id}`).then((r) => r.data),
  creer: (data: EmployeRequest) => api.post<EmployeResponse>("/employes", data).then((r) => r.data),
  modifier: (id: number, data: EmployeRequest) =>
    api.put<EmployeResponse>(`/employes/${id}`, data).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/employes/${id}`).then(() => undefined),
}
