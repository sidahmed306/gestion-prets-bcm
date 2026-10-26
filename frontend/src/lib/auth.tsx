import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { authApi } from "../api/auth"
import { tokenStorage } from "../api/client"
import type { Role, UtilisateurResponse } from "../api/types"

interface AuthContextValue {
  utilisateur: UtilisateurResponse | null
  chargement: boolean
  login: (username: string, motDePasse: string) => Promise<void>
  logout: () => void
  /** ADMIN et GESTIONNAIRE peuvent créer, modifier et supprimer ; LECTEUR consulte seulement. */
  peutModifier: boolean
  estAdmin: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export const ROLE_LIBELLES: Record<Role, string> = {
  ADMIN: "Administrateur",
  GESTIONNAIRE: "Gestionnaire",
  LECTEUR: "Lecteur",
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [utilisateur, setUtilisateur] = useState<UtilisateurResponse | null>(null)
  const [chargement, setChargement] = useState(() => tokenStorage.get() !== null)

  // Au chargement de la page : si un jeton est déjà enregistré, on demande au serveur qui on est
  useEffect(() => {
    if (!tokenStorage.get()) return
    authApi
      .me()
      .then(setUtilisateur)
      .catch(() => tokenStorage.clear())
      .finally(() => setChargement(false))
  }, [])

  const login = useCallback(async (username: string, motDePasse: string) => {
    const reponse = await authApi.login(username, motDePasse)
    tokenStorage.set(reponse.token)
    setUtilisateur(reponse.utilisateur)
  }, [])

  // JWT = pas de session côté serveur : se déconnecter, c'est simplement oublier le jeton
  const logout = useCallback(() => {
    tokenStorage.clear()
    setUtilisateur(null)
    queryClient.clear()
  }, [queryClient])

  const role = utilisateur?.role
  const value: AuthContextValue = {
    utilisateur,
    chargement,
    login,
    logout,
    peutModifier: role === "ADMIN" || role === "GESTIONNAIRE",
    estAdmin: role === "ADMIN",
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth doit être utilisé dans AuthProvider")
  return context
}
