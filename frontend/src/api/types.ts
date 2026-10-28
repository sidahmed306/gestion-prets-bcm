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
  prixAchat: number | null
  dateAchat: string | null
  dureeAmortissementAnnees: number | null
  fournisseur: string | null
  situationComptable: SituationComptable | null
}

export interface MaterielRequest {
  numeroInventaire: string
  designation?: string
  categorieId: number
  marque?: string
  modele?: string
  etatPhysique?: string
  prixAchat?: number | null
  dateAchat?: string | null
  dureeAmortissementAnnees?: number | null
  fournisseur?: string
}

export interface SituationComptable {
  amortissementAnnuel: number
  amortissementCumule: number
  valeurNetteComptable: number
  pourcentageAmorti: number
  dateFinAmortissement: string
  totalementAmorti: boolean
}

export interface LigneAmortissement {
  exercice: number
  dotation: number
  cumul: number
  valeurNette: number
}

export interface PlanAmortissement {
  materielId: number
  numeroInventaire: string
  designation: string | null
  prixAchat: number
  dateAchat: string
  dureeAmortissementAnnees: number
  annuite: number
  lignes: LigneAmortissement[]
}

export interface SyntheseComptable {
  dateCalcul: string
  nombreMateriels: number
  nombreValorises: number
  nombreNonValorises: number
  nombreTotalementAmortis: number
  valeurAchatTotale: number
  amortissementCumuleTotal: number
  valeurNetteTotale: number
  dotationExerciceEnCours: number
  parCategorie: { categorie: string; nombre: number; valeurAchat: number; valeurNette: number }[]
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

export type Role = "ADMIN" | "GESTIONNAIRE" | "LECTEUR"

export interface UtilisateurResponse {
  id: number
  username: string
  nomComplet: string
  role: Role
  actif: boolean
  derniereConnexion: string | null
  /** LOCAL = compte créé dans l'application ; ANNUAIRE = compte de l'annuaire d'entreprise */
  source: "LOCAL" | "ANNUAIRE"
  /** Direction et poste, recopiés depuis l'annuaire (null pour un compte local) */
  departement: string | null
  poste: string | null
}

export interface SynchronisationResponse {
  lus: number
  crees: number
  misAJour: number
  inchanges: number
  ignores: number
}

export interface UtilisateurCreateRequest {
  username: string
  motDePasse: string
  nomComplet: string
  role: Role
}

export interface UtilisateurUpdateRequest {
  nomComplet: string
  role: Role
  actif: boolean
}

export interface LoginResponse {
  token: string
  expiration: string
  utilisateur: UtilisateurResponse
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors?: Record<string, string> | null
}
