import { useState } from "react"
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom"
import { AnimatePresence, motion } from "framer-motion"
import { ArrowRightLeft, KeyRound, LayoutDashboard, LogOut, ShieldCheck, Tags, Laptop, Users } from "lucide-react"
import clsx from "clsx"
import { bcmLogoDataUri } from "../assets/bcmLogo"
import { ROLE_LIBELLES, useAuth } from "../lib/auth"
import { ChangePasswordModal } from "./ChangePasswordModal"

interface NavItem {
  to: string
  label: string
  icon: typeof LayoutDashboard
  end?: boolean
  adminSeulement?: boolean
}

const navItems: NavItem[] = [
  { to: "/", label: "Tableau de bord", icon: LayoutDashboard, end: true },
  { to: "/employes", label: "Employés", icon: Users },
  { to: "/materiels", label: "Matériels", icon: Laptop },
  { to: "/prets", label: "Prêts", icon: ArrowRightLeft },
  { to: "/categories", label: "Catégories", icon: Tags },
  { to: "/utilisateurs", label: "Utilisateurs", icon: ShieldCheck, adminSeulement: true },
]

export function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { utilisateur, estAdmin, logout } = useAuth()
  const [changementMdp, setChangementMdp] = useState(false)

  function seDeconnecter() {
    logout()
    navigate("/login", { replace: true })
  }

  return (
    <div className="min-h-screen bg-page">
      <aside className="fixed inset-y-0 left-0 flex w-64 flex-col bg-brand-primary-950">
        <div className="flex items-center gap-3 border-b border-brand-primary-800 px-5 py-5">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm bg-white p-1.5">
            <img src={bcmLogoDataUri} alt="Banque Centrale de Mauritanie" className="h-full w-full object-contain" />
          </div>
          <div>
            <p className="font-serif text-sm font-semibold leading-tight text-white">
              Banque Centrale <br /> de Mauritanie
            </p>
            <p className="mt-1 text-[11px] uppercase tracking-wide text-brand-gold-300">Gestion des prêts</p>
          </div>
        </div>
        <nav className="flex flex-col py-3">
          {navItems.filter((item) => estAdmin || !item.adminSeulement).map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                clsx(
                  "flex items-center gap-3 border-l-2 px-5 py-2.5 text-sm font-medium transition-colors",
                  isActive
                    ? "border-l-brand-gold-400 bg-brand-primary-900 text-white"
                    : "border-l-transparent text-brand-primary-200 hover:bg-brand-primary-900/60 hover:text-white",
                )
              }
            >
              <Icon size={17} />
              {label}
            </NavLink>
          ))}
        </nav>

        {utilisateur && (
          <div className="mt-auto border-t border-brand-primary-800 px-5 py-4">
            <p className="truncate text-sm font-medium text-white">{utilisateur.nomComplet}</p>
            <p className="mt-0.5 text-[11px] uppercase tracking-wide text-brand-gold-300">
              {ROLE_LIBELLES[utilisateur.role]}
            </p>
            <div className="mt-3 flex flex-col gap-0.5">
              <button
                onClick={() => setChangementMdp(true)}
                className="flex items-center gap-2 rounded-sm py-1.5 text-sm text-brand-primary-200 transition-colors hover:text-white"
              >
                <KeyRound size={15} /> Mon mot de passe
              </button>
              <button
                onClick={seDeconnecter}
                className="flex items-center gap-2 rounded-sm py-1.5 text-sm text-brand-primary-200 transition-colors hover:text-white"
              >
                <LogOut size={15} /> Se déconnecter
              </button>
            </div>
          </div>
        )}
      </aside>
      <ChangePasswordModal open={changementMdp} onClose={() => setChangementMdp(false)} />

      <main className="ml-64 min-h-screen">
        <div className="mx-auto max-w-5xl px-8 py-8">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.16, ease: "easeOut" }}
            >
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  )
}
