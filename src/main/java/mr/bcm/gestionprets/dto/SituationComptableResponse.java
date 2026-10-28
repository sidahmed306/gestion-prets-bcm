package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Valeur d'un matériel à une date donnée.
 *
 * @param amortissementAnnuel   perte de valeur sur une année complète (prix / durée)
 * @param amortissementCumule   perte de valeur totale depuis l'achat
 * @param valeurNetteComptable  ce que vaut encore le matériel en comptabilité (prix - cumul)
 * @param pourcentageAmorti     part du prix déjà amortie (0 à 100)
 * @param dateFinAmortissement  dernier jour d'amortissement : le soir de ce jour, la valeur atteint 0
 */
public record SituationComptableResponse(
        BigDecimal amortissementAnnuel,
        BigDecimal amortissementCumule,
        BigDecimal valeurNetteComptable,
        BigDecimal pourcentageAmorti,
        LocalDate dateFinAmortissement,
        boolean totalementAmorti) {
}
