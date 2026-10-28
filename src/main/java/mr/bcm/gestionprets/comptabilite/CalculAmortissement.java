package mr.bcm.gestionprets.comptabilite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import mr.bcm.gestionprets.dto.LigneAmortissementResponse;
import mr.bcm.gestionprets.dto.SituationComptableResponse;

/**
 * Amortissement LINÉAIRE avec PRORATA TEMPORIS.
 * <ul>
 *   <li>Linéaire : le matériel perd la même somme chaque année (l'annuité = prix / durée).</li>
 *   <li>Prorata temporis : l'année d'achat, on ne compte que les jours où le matériel a été possédé.
 *       Exemple : acheté le 1er juillet → seulement la moitié de l'annuité la 1re année,
 *       et le reste est reporté sur une année supplémentaire à la fin.</li>
 *   <li>Exercice comptable = année civile (1er janvier au 31 décembre).</li>
 * </ul>
 * Classe de calcul pur (pas de base de données) : facile à tester avec des chiffres exacts.
 */
public final class CalculAmortissement {

    private static final int DECIMALES = 2;
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private CalculAmortissement() {
    }

    public static BigDecimal annuite(BigDecimal prix, int dureeAnnees) {
        return prix.divide(BigDecimal.valueOf(dureeAnnees), DECIMALES, RoundingMode.HALF_UP);
    }

    public static LocalDate dateFin(LocalDate dateAchat, int dureeAnnees) {
        return dateAchat.plusYears(dureeAnnees);
    }

    /** Le plan d'amortissement : une ligne par année, de l'achat jusqu'à ce que la valeur atteigne 0. */
    public static List<LigneAmortissementResponse> plan(BigDecimal prix, LocalDate dateAchat, int dureeAnnees) {
        List<LigneAmortissementResponse> lignes = new ArrayList<>();
        if (prix.signum() == 0) {
            return lignes;
        }
        BigDecimal annuite = annuite(prix, dureeAnnees);
        LocalDate fin = dateFin(dateAchat, dureeAnnees);
        BigDecimal cumul = BigDecimal.ZERO;
        int exercice = dateAchat.getYear();
        boolean derniere = false;

        while (!derniere) {
            BigDecimal dotation = prorata(annuite, debutExercice(exercice, dateAchat), premierJanvier(exercice + 1));
            // Dernière année : on prend exactement ce qui reste, pour que la somme fasse le prix (pas d'écart d'arrondi)
            derniere = !premierJanvier(exercice + 1).isBefore(fin) || cumul.add(dotation).compareTo(prix) >= 0;
            if (derniere) {
                dotation = prix.subtract(cumul);
            }
            cumul = cumul.add(dotation);
            lignes.add(new LigneAmortissementResponse(exercice, dotation, cumul, prix.subtract(cumul)));
            exercice++;
        }
        return lignes;
    }

    /**
     * Ce que vaut le matériel À LA FIN de la journée donnée (en général : aujourd'hui).
     * La journée est comptée : la valeur « au 31 décembre » est bien celle de fin d'exercice du plan.
     */
    public static SituationComptableResponse situation(BigDecimal prix, LocalDate dateAchat, int dureeAnnees,
            LocalDate date) {
        BigDecimal annuite = annuite(prix, dureeAnnees);
        LocalDate dernierJour = dateFin(dateAchat, dureeAnnees).minusDays(1);
        LocalDate lendemain = date.plusDays(1);
        BigDecimal cumul = BigDecimal.ZERO;

        if (!date.isBefore(dernierJour)) {
            cumul = prix;
        } else if (!date.isBefore(dateAchat)) {
            for (LigneAmortissementResponse ligne : plan(prix, dateAchat, dureeAnnees)) {
                if (ligne.exercice() < date.getYear()) {
                    cumul = ligne.cumul(); // années terminées : dotation complète
                } else if (ligne.exercice() == date.getYear()) {
                    // année en cours : les jours écoulés depuis le 1er janvier (ou depuis l'achat), jour J inclus
                    BigDecimal partiel = prorata(annuite, debutExercice(ligne.exercice(), dateAchat), lendemain);
                    cumul = cumul.add(partiel.min(ligne.dotation()));
                }
            }
        }

        BigDecimal pourcentage = prix.signum() == 0
                ? CENT
                : cumul.multiply(CENT).divide(prix, 1, RoundingMode.HALF_UP);
        return new SituationComptableResponse(annuite, cumul, prix.subtract(cumul), pourcentage, dernierJour,
                cumul.compareTo(prix) >= 0);
    }

    /** annuité × (jours de la période / jours de l'année). */
    private static BigDecimal prorata(BigDecimal annuite, LocalDate debut, LocalDate finExclue) {
        long jours = ChronoUnit.DAYS.between(debut, finExclue);
        int joursAnnee = Year.of(debut.getYear()).length(); // 365 ou 366 (année bissextile)
        return annuite.multiply(BigDecimal.valueOf(jours))
                .divide(BigDecimal.valueOf(joursAnnee), DECIMALES, RoundingMode.HALF_UP);
    }

    private static LocalDate debutExercice(int exercice, LocalDate dateAchat) {
        return exercice == dateAchat.getYear() ? dateAchat : premierJanvier(exercice);
    }

    private static LocalDate premierJanvier(int annee) {
        return LocalDate.of(annee, 1, 1);
    }
}
