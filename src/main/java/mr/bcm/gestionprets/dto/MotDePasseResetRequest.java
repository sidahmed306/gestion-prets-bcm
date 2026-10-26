package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** L'admin définit un nouveau mot de passe pour un autre utilisateur (mot de passe oublié). */
public record MotDePasseResetRequest(
        @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String nouveauMotDePasse) {
}
