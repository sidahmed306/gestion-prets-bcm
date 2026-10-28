package mr.bcm.gestionprets.dto;

/**
 * Bilan d'une synchronisation des employés avec l'annuaire.
 *
 * @param ignores personnes de l'annuaire sans matricule (comptes de service, stagiaires...)
 */
public record SynchronisationResponse(int lus, int crees, int misAJour, int inchanges, int ignores) {
}
