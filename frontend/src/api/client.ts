import axios from "axios"
import type { ApiErrorBody } from "./types"

const TOKEN_KEY = "bcm.token"

/** Le jeton JWT reçu à la connexion, gardé dans le navigateur entre deux visites. */
export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

export const api = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
})

export class ApiError extends Error {
  status: number
  fieldErrors?: Record<string, string> | null

  constructor(body: ApiErrorBody) {
    super(body.message)
    this.status = body.status
    this.fieldErrors = body.fieldErrors
  }
}

// Avant chaque requête : ajoute "Authorization: Bearer <jeton>" si l'utilisateur est connecté
api.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 hors page de connexion : jeton expiré ou compte désactivé, on renvoie vers la connexion
    const estLogin = error.config?.url === "/auth/login"
    if (error.response?.status === 401 && !estLogin && tokenStorage.get()) {
      tokenStorage.clear()
      window.location.assign("/login?expire=1")
    }
    if (error.response?.data?.message) {
      return Promise.reject(new ApiError(error.response.data as ApiErrorBody))
    }
    return Promise.reject(error)
  },
)
