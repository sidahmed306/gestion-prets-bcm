import { api } from "./client"
import type { CategorieRequest, CategorieResponse } from "./types"

export const categoriesApi = {
  findAll: () => api.get<CategorieResponse[]>("/categories").then((r) => r.data),
  findById: (id: number) => api.get<CategorieResponse>(`/categories/${id}`).then((r) => r.data),
  creer: (data: CategorieRequest) => api.post<CategorieResponse>("/categories", data).then((r) => r.data),
  modifier: (id: number, data: CategorieRequest) =>
    api.put<CategorieResponse>(`/categories/${id}`, data).then((r) => r.data),
  supprimer: (id: number) => api.delete(`/categories/${id}`).then(() => undefined),
}
