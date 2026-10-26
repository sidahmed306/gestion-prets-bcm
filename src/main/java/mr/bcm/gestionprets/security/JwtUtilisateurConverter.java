package mr.bcm.gestionprets.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Appelé à chaque requête, une fois le jeton vérifié : transforme le jeton en "utilisateur connecté".
 * <p>
 * On relit l'utilisateur en base au lieu de faire confiance au rôle écrit dans le jeton.
 * Ainsi, si un admin désactive un compte ou change son rôle, l'effet est immédiat,
 * sans attendre l'expiration du jeton.
 */
@Component
@RequiredArgsConstructor
public class JwtUtilisateurConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserDetailsService userDetailsService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UserDetails utilisateur = userDetailsService.loadUserByUsername(jwt.getSubject());
        if (!utilisateur.isEnabled()) {
            throw new DisabledException("Ce compte est désactivé");
        }
        return new JwtAuthenticationToken(jwt, utilisateur.getAuthorities(), utilisateur.getUsername());
    }
}
