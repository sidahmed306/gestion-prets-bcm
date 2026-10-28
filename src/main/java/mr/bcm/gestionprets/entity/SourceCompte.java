package mr.bcm.gestionprets.entity;

/** D'où vient un compte utilisateur. */
public enum SourceCompte {
    /** Créé dans l'application par un admin ; mot de passe stocké (haché) dans notre base. */
    LOCAL,
    /** Vient de l'annuaire d'entreprise ; mot de passe et rôle gérés dans l'annuaire. */
    ANNUAIRE
}
