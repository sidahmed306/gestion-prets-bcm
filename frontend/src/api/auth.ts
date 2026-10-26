import { api } from "./client"
import type { LoginResponse, UtilisateurResponse } from "./types"

export const authApi = {
  login: (username: string, motDePasse: string) =>
    api.post<LoginResponse>("/auth/login", { username, motDePasse }).then((r) => r.data),
  me: () => api.get<UtilisateurResponse>("/auth/me").then((r) => r.data),
  changerMotDePasse: (ancienMotDePasse: string, nouveauMotDePasse: string) =>
    api.put("/auth/mot-de-passe", { ancienMotDePasse, nouveauMotDePasse }).then(() => undefined),
}
