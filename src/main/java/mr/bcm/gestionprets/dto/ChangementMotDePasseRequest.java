package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Un utilisateur connecté change son propre mot de passe : il doit donner l'ancien. */
public record ChangementMotDePasseRequest(
        @NotBlank String ancienMotDePasse,
        @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String nouveauMotDePasse) {
}
