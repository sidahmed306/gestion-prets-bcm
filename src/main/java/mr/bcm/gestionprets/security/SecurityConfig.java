package mr.bcm.gestionprets.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Configuration centrale de Spring Security.
 * <p>
 * Principe : l'utilisateur se connecte une fois (POST /api/auth/login) et reçoit un jeton JWT.
 * Il renvoie ensuite ce jeton dans l'en-tête {@code Authorization: Bearer <jeton>} de chaque requête.
 * Le serveur ne garde aucune session : il vérifie le jeton à chaque fois (mode "stateless").
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ROLES_ECRITURE = { "ADMIN", "GESTIONNAIRE" };

    /** Qui a le droit d'appeler quelle URL. Les règles sont lues de haut en bas : la première qui correspond gagne. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtUtilisateurConverter jwtUtilisateurConverter,
            SecurityErrorHandler securityErrorHandler) throws Exception {
        http
                // CSRF protège les sessions par cookie ; ici on utilise un jeton dans un en-tête, donc inutile.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public : la connexion, la documentation Swagger et la page d'erreur
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // Tout utilisateur connecté : voir son profil, changer son mot de passe
                        .requestMatchers("/api/auth/**").authenticated()
                        // Gestion des comptes et synchronisation avec l'annuaire : administrateurs uniquement
                        .requestMatchers("/api/utilisateurs/**", "/api/annuaire/**").hasRole("ADMIN")
                        // Lecture : tous les rôles connectés (y compris LECTEUR)
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // Création / modification / suppression : ADMIN ou GESTIONNAIRE
                        .requestMatchers("/api/**").hasAnyRole(ROLES_ECRITURE)
                        .anyRequest().authenticated())
                // Lit le jeton "Authorization: Bearer ..." et vérifie sa signature et sa date d'expiration
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtUtilisateurConverter))
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler));
        return http.build();
    }

    /** BCrypt : hachage lent et salé, la norme pour stocker des mots de passe. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Vérifie un couple identifiant / mot de passe lors de la connexion.
     * Spring essaie les "fournisseurs" dans l'ordre, jusqu'à ce que l'un d'eux accepte :
     * <ol>
     *   <li>les comptes LOCAUX (table utilisateurs, mot de passe BCrypt) ;</li>
     *   <li>puis l'ANNUAIRE d'entreprise, s'il est activé.</li>
     * </ol>
     * Exception : un compte désactivé est refusé tout de suite, sans essayer l'annuaire.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder, ObjectProvider<LdapAuthenticationProvider> annuaireProvider) {
        DaoAuthenticationProvider comptesLocaux = new DaoAuthenticationProvider(userDetailsService);
        comptesLocaux.setPasswordEncoder(passwordEncoder);

        List<AuthenticationProvider> fournisseurs = new ArrayList<>();
        fournisseurs.add(comptesLocaux);
        annuaireProvider.ifAvailable(fournisseurs::add);
        return new ProviderManager(fournisseurs);
    }

    /** Clé secrète partagée qui sert à signer (encoder) et vérifier (decoder) les jetons. */
    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] octets = secret.getBytes(StandardCharsets.UTF_8);
        if (octets.length < 32) {
            throw new IllegalStateException("app.jwt.secret doit contenir au moins 32 caractères");
        }
        return new SecretKeySpec(octets, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
