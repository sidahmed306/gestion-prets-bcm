package mr.bcm.gestionprets.entity;

/**
 * Rôles des utilisateurs de l'application.
 * <ul>
 *   <li>ADMIN : tout faire, y compris gérer les comptes utilisateurs</li>
 *   <li>GESTIONNAIRE : gérer les employés, matériels, catégories et prêts</li>
 *   <li>LECTEUR : consulter uniquement (aucune modification)</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    GESTIONNAIRE,
    LECTEUR
}
