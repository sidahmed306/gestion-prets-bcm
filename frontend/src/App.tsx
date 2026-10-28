import type { ReactNode } from "react"
import { Navigate, Route, Routes, useLocation } from "react-router-dom"
import { Layout } from "./components/Layout"
import { Spinner } from "./components/Spinner"
import { useAuth } from "./lib/auth"
import { Dashboard } from "./pages/Dashboard"
import { Employes } from "./pages/Employes"
import { Categories } from "./pages/Categories"
import { Materiels } from "./pages/Materiels"
import { Prets } from "./pages/Prets"
import { Login } from "./pages/Login"
import { Utilisateurs } from "./pages/Utilisateurs"
import { Comptabilite } from "./pages/Comptabilite"

/** Pas connecté : on renvoie vers la page de connexion en retenant la page demandée. */
function RequireAuth({ children }: { children: ReactNode }) {
  const { utilisateur, chargement } = useAuth()
  const location = useLocation()
  if (chargement) return <Spinner />
  if (!utilisateur) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}

/** Page réservée aux administrateurs (le serveur vérifie aussi : ceci n'est que l'affichage). */
function RequireAdmin({ children }: { children: ReactNode }) {
  const { estAdmin } = useAuth()
  if (!estAdmin) return <Navigate to="/" replace />
  return children
}

function App() {
  return (
    <Routes>
      <Route path="login" element={<Login />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="prets" element={<Prets />} />
        <Route path="materiels" element={<Materiels />} />
        <Route path="employes" element={<Employes />} />
        <Route path="categories" element={<Categories />} />
        <Route path="comptabilite" element={<Comptabilite />} />
        <Route
          path="utilisateurs"
          element={
            <RequireAdmin>
              <Utilisateurs />
            </RequireAdmin>
          }
        />
      </Route>
    </Routes>
  )
}

export default App
