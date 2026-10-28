package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record MaterielRequest(
        @NotBlank String numeroInventaire,
        String designation,
        @NotNull Long categorieId,
        String marque,
        String modele,
        String etatPhysique,
        @PositiveOrZero(message = "Le prix d'achat ne peut pas être négatif") BigDecimal prixAchat,
        @PastOrPresent(message = "La date d'achat ne peut pas être dans le futur") LocalDate dateAchat,
        @Min(value = 1, message = "La durée doit être d'au moins 1 an")
        @Max(value = 50, message = "La durée ne peut pas dépasser 50 ans") Integer dureeAmortissementAnnees,
        String fournisseur) {

    /** Prix, date et durée vont ensemble : soit les 3, soit aucun. */
    @AssertTrue(message = "Renseignez le prix, la date d'achat et la durée d'amortissement ensemble (ou aucun des trois)")
    public boolean isInfosComptablesCompletes() {
        boolean aucune = prixAchat == null && dateAchat == null && dureeAmortissementAnnees == null;
        boolean toutes = prixAchat != null && dateAchat != null && dureeAmortissementAnnees != null;
        return aucune || toutes;
    }
}
