package mr.bcm.gestionprets.dto;

import java.time.LocalDateTime;

import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.Utilisateur;

/** Ce qu'on renvoie au frontend : jamais le mot de passe, même haché. */
public record UtilisateurResponse(
        Long id,
        String username,
        String nomComplet,
        Role role,
        boolean actif,
        LocalDateTime derniereConnexion) {

    public static UtilisateurResponse of(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getUsername(),
                utilisateur.getNomComplet(),
                utilisateur.getRole(),
                utilisateur.isActif(),
                utilisateur.getDerniereConnexion());
    }
}
