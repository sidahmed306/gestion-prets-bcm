export type StatutPret = "EN_COURS" | "RETOURNE"

export interface CategorieResponse {
  id: number
  code: string | null
  libelle: string | null
}

export interface CategorieRequest {
  code?: string
  libelle?: string
}

export interface EmployeResponse {
  id: number
  matricule: string
  nom: string
  departement: string | null
  email: string | null
}

export interface EmployeRequest {
  matricule: string
  nom: string
  departement?: string
  email?: string
}

export interface MaterielResponse {
  id: number
  numeroInventaire: string
  designation: string | null
  categorie: CategorieResponse
  marque: string | null
  modele: string | null
  etatPhysique: string | null
}

export interface MaterielRequest {
  numeroInventaire: string
  designation?: string
  categorieId: number
  marque?: string
  modele?: string
  etatPhysique?: string
}

export interface PretResponse {
  id: number
  materiel: MaterielResponse
  employe: EmployeResponse
  datePret: string
  dateRetourPrevue: string
  dateRetourEffective: string | null
  statut: StatutPret
  commentaire: string | null
  enRetard: boolean
}

export interface PretRequest {
  materielId: number
  employeId: number
  datePret: string
  dateRetourPrevue: string
  commentaire?: string
}

export interface PretRetourRequest {
  dateRetourEffective?: string
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors?: Record<string, string> | null
}
