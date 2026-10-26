package mr.bcm.gestionprets.dto;

import java.time.Instant;

/** Réponse à une connexion réussie : le jeton JWT à renvoyer dans chaque requête suivante. */
public record LoginResponse(String token, Instant expiration, UtilisateurResponse utilisateur) {
}
