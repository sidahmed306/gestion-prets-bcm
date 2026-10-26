package mr.bcm.gestionprets.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import mr.bcm.gestionprets.entity.Utilisateur;

/** Fabrique les jetons JWT remis à l'utilisateur après une connexion réussie. */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    public record Jeton(String valeur, Instant expiration) {
    }

    public Jeton genererJeton(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        Instant expiration = maintenant.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet contenu = JwtClaimsSet.builder()
                .issuer("gestion-prets-bcm")
                .subject(utilisateur.getUsername()) // qui est l'utilisateur
                .issuedAt(maintenant)
                .expiresAt(expiration)              // après cette date, le jeton est refusé
                .claim("role", utilisateur.getRole().name())
                .build();
        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();
        String valeur = jwtEncoder.encode(JwtEncoderParameters.from(entete, contenu)).getTokenValue();
        return new Jeton(valeur, expiration);
    }
}
