package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;

/**
 * Une ligne du plan d'amortissement : un exercice comptable (une année civile).
 *
 * @param dotation          perte de valeur enregistrée cette année-là
 * @param cumul             total des dotations depuis l'achat, à la fin de l'année
 * @param valeurNette       valeur restante à la fin de l'année
 */
public record LigneAmortissementResponse(int exercice, BigDecimal dotation, BigDecimal cumul, BigDecimal valeurNette) {
}
