package mr.bcm.gestionprets.annuaire;

import java.util.Collection;
import java.util.List;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * La fiche d'un agent telle qu'elle est lue dans l'annuaire.
 * Sert à la fois à la connexion (c'est l'"utilisateur connecté" renvoyé par Spring Security)
 * et à la synchronisation des employés.
 */
public record AgentAnnuaire(
        String identifiant,
        String nom,
        String matricule,
        String departement,
        String poste,
        String email) implements UserDetails {

    /** Lit les attributs de la fiche LDAP, avec les noms d'attributs configurés (AD et LDAP n'utilisent pas les mêmes). */
    public static AgentAnnuaire depuis(DirContextOperations fiche, String identifiant, AnnuaireProperties proprietes) {
        String nom = fiche.getStringAttribute("displayName") != null
                ? fiche.getStringAttribute("displayName")
                : fiche.getStringAttribute("cn");
        return new AgentAnnuaire(
                identifiant,
                nom,
                fiche.getStringAttribute(proprietes.attributMatricule()),
                fiche.getStringAttribute(proprietes.attributDepartement()),
                fiche.getStringAttribute(proprietes.attributPoste()),
                fiche.getStringAttribute("mail"));
    }

    // --- Ce que Spring Security demande à un "utilisateur connecté" ---

    @Override
    public String getUsername() {
        return identifiant;
    }

    /** Le mot de passe reste dans l'annuaire : l'application ne le connaît pas. */
    @Override
    public String getPassword() {
        return null;
    }

    /** Le rôle n'est pas décidé ici, mais dans ConnexionAnnuaireService (règles d'accès). */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
