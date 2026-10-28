package mr.bcm.gestionprets.annuaire;

import java.util.Collection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * Connexion par l'annuaire. N'existe que si app.annuaire.enabled=true.
 * <p>
 * Comment l'annuaire vérifie un mot de passe (méthode "bind") :
 * <ol>
 *   <li>on cherche l'agent dans l'annuaire avec son identifiant (filtre-connexion),
 *       dans toutes les directions (DSI, Études, RH...) ;</li>
 *   <li>on essaie de se connecter à l'annuaire AVEC SON mot de passe : si l'annuaire accepte,
 *       le mot de passe est bon. Notre application ne voit ni ne stocke jamais ce mot de passe ;</li>
 *   <li>on lit sa fiche (nom, matricule, direction, poste) : son rôle en sera déduit.</li>
 * </ol>
 * Le "ContextSource" (adresse de l'annuaire) est créé automatiquement par Spring Boot
 * à partir des propriétés spring.ldap.* (ou spring.ldap.embedded.* en développement).
 */
@Configuration
@ConditionalOnProperty(name = "app.annuaire.enabled", havingValue = "true")
public class AnnuaireConfig {

    @Bean
    public LdapAuthenticationProvider annuaireAuthenticationProvider(BaseLdapPathContextSource contextSource,
            AnnuaireProperties proprietes) {
        // 1. Trouver l'agent : on cherche aussi dans les sous-branches (une branche par direction)
        FilterBasedLdapUserSearch recherche = new FilterBasedLdapUserSearch(
                proprietes.baseUtilisateurs(), proprietes.filtreConnexion(), contextSource);
        recherche.setSearchSubtree(true);

        // 2. Vérifier son mot de passe auprès de l'annuaire
        BindAuthenticator authentificateur = new BindAuthenticator(contextSource);
        authentificateur.setUserSearch(recherche);

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authentificateur);
        // 3. Transformer sa fiche en AgentAnnuaire (nom, matricule, direction, poste, email)
        provider.setUserDetailsContextMapper(new UserDetailsContextMapper() {
            @Override
            public UserDetails mapUserFromContext(DirContextOperations fiche, String identifiant,
                    Collection<? extends GrantedAuthority> autorites) {
                return AgentAnnuaire.depuis(fiche, identifiant, proprietes);
            }

            @Override
            public void mapUserToContext(UserDetails utilisateur, DirContextAdapter fiche) {
                throw new UnsupportedOperationException("L'application ne modifie jamais l'annuaire");
            }
        });
        return provider;
    }
}
