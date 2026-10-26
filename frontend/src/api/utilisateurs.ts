import { api } from "./client"
import type { UtilisateurCreateRequest, UtilisateurResponse, UtilisateurUpdateRequest } from "./types"

export const utilisateursApi = {
  findAll: () => api.get<UtilisateurResponse[]>("/utilisateurs").then((r) => r.data),
  creer: (data: UtilisateurCreateRequest) => api.post<UtilisateurResponse>("/utilisateurs", data).then((r) => r.data),
  modifier: (id: number, data: UtilisateurUpdateRequest) =>
    api.put<UtilisateurResponse>(`/utilisateurs/${id}`, data).then((r) => r.data),
  reinitialiserMotDePasse: (id: number, nouveauMotDePasse: string) =>
    api.put(`/utilisateurs/${id}/mot-de-passe`, { nouveauMotDePasse }).then(() => undefined),
  supprimer: (id: number) => api.delete(`/utilisateurs/${id}`).then(() => undefined),
}
