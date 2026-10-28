import { useState } from "react"
import { Navigate, useLocation, useNavigate, useSearchParams } from "react-router-dom"
import { LogIn } from "lucide-react"
import { bcmLogoDataUri } from "../assets/bcmLogo"
import { Button } from "../components/Button"
import { TextField } from "../components/Form"
import { useAuth } from "../lib/auth"
import { getErrorMessage } from "../lib/errors"

export function Login() {
  const { utilisateur, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const [username, setUsername] = useState("")
  const [motDePasse, setMotDePasse] = useState("")
  const [erreur, setErreur] = useState<string | null>(
    searchParams.get("expire") ? "Votre session a expiré, veuillez vous reconnecter." : null,
  )
  const [envoi, setEnvoi] = useState(false)

  // Page demandée avant d'être redirigé vers la connexion (sinon le tableau de bord)
  const destination = (location.state as { from?: string } | null)?.from ?? "/"

  if (utilisateur) return <Navigate to={destination} replace />

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setEnvoi(true)
    setErreur(null)
    try {
      await login(username, motDePasse)
      navigate(destination, { replace: true })
    } catch (error) {
      setErreur(getErrorMessage(error))
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-brand-primary-950 px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-sm bg-white p-2">
            <img src={bcmLogoDataUri} alt="Banque Centrale de Mauritanie" className="h-full w-full object-contain" />
          </div>
          <p className="font-serif text-lg font-semibold text-white">Banque Centrale de Mauritanie</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-brand-gold-300">Gestion des prêts de matériel</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 rounded-sm border border-ink-200 bg-white p-6 shadow-lg">
          <div className="border-b border-ink-200 pb-3">
            <h1 className="font-serif text-base font-semibold text-ink-900">Connexion</h1>
            <p className="mt-1 text-xs text-ink-500">Utilisez votre compte de l'annuaire BCM (identifiant Windows) ou un compte local.</p>
          </div>
          {erreur && (
            <p role="alert" className="rounded-sm bg-status-danger-bg px-3 py-2 text-sm text-status-danger-text">
              {erreur}
            </p>
          )}
          <TextField
            label="Identifiant"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            required
          />
          <TextField
            label="Mot de passe"
            type="password"
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            autoComplete="current-password"
            required
          />
          <Button type="submit" disabled={envoi} className="w-full justify-center">
            <LogIn size={16} /> {envoi ? "Connexion..." : "Se connecter"}
          </Button>
        </form>
      </div>
    </div>
  )
}
