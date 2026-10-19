package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MaterielRequest(
        @NotBlank String numeroInventaire,
        String designation,
        @NotNull Long categorieId,
        String marque,
        String modele,
        String etatPhysique) {
}
