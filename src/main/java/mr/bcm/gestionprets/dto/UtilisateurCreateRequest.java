package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mr.bcm.gestionprets.entity.Role;

public record UtilisateurCreateRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String motDePasse,
        @NotBlank String nomComplet,
        @NotNull Role role) {
}
