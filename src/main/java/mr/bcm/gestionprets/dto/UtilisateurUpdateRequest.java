package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mr.bcm.gestionprets.entity.Role;

/** Modification d'un compte par l'admin (le mot de passe se change séparément). */
public record UtilisateurUpdateRequest(
        @NotBlank String nomComplet,
        @NotNull Role role,
        @NotNull Boolean actif) {
}
