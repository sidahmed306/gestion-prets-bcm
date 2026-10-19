import { Route, Routes } from "react-router-dom"
import { Layout } from "./components/Layout"
import { Dashboard } from "./pages/Dashboard"
import { Employes } from "./pages/Employes"
import { Categories } from "./pages/Categories"
import { Materiels } from "./pages/Materiels"
import { Prets } from "./pages/Prets"

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="prets" element={<Prets />} />
        <Route path="materiels" element={<Materiels />} />
        <Route path="employes" element={<Employes />} />
        <Route path="categories" element={<Categories />} />
      </Route>
    </Routes>
  )
}

export default App
