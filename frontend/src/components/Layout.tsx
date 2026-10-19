import { NavLink, Outlet, useLocation } from "react-router-dom"
import { AnimatePresence, motion } from "framer-motion"
import { ArrowRightLeft, LayoutDashboard, Tags, Laptop, Users } from "lucide-react"
import clsx from "clsx"
import { bcmLogoDataUri } from "../assets/bcmLogo"

interface NavItem {
  to: string
  label: string
  icon: typeof LayoutDashboard
  end?: boolean
}

const navItems: NavItem[] = [
  { to: "/", label: "Tableau de bord", icon: LayoutDashboard, end: true },
  { to: "/employes", label: "Employés", icon: Users },
  { to: "/materiels", label: "Matériels", icon: Laptop },
  { to: "/prets", label: "Prêts", icon: ArrowRightLeft },
  { to: "/categories", label: "Catégories", icon: Tags },
]

export function Layout() {
  const location = useLocation()

  return (
    <div className="min-h-screen bg-page">
      <aside className="fixed inset-y-0 left-0 w-64 bg-brand-primary-950">
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
          {navItems.map(({ to, label, icon: Icon, end }) => (
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
      </aside>

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
