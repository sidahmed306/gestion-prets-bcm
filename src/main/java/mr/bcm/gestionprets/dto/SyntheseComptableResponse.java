package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Vue d'ensemble de la valeur du parc matériel à une date donnée.
 *
 * @param nombreNonValorises matériels sans prix / date / durée : exclus des totaux
 * @param valeurAchatTotale  ce que le parc a coûté
 * @param valeurNetteTotale  ce que le parc vaut encore en comptabilité
 */
public record SyntheseComptableResponse(
        LocalDate dateCalcul,
        int nombreMateriels,
        int nombreValorises,
        int nombreNonValorises,
        int nombreTotalementAmortis,
        BigDecimal valeurAchatTotale,
        BigDecimal amortissementCumuleTotal,
        BigDecimal valeurNetteTotale,
        BigDecimal dotationExerciceEnCours,
        List<ParCategorie> parCategorie) {

    public record ParCategorie(
            String categorie,
            int nombre,
            BigDecimal valeurAchat,
            BigDecimal valeurNette) {
    }
}
