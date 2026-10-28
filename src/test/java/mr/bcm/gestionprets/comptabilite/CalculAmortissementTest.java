package mr.bcm.gestionprets.comptabilite;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import mr.bcm.gestionprets.dto.LigneAmortissementResponse;
import mr.bcm.gestionprets.dto.SituationComptableResponse;

class CalculAmortissementTest {

    private static BigDecimal mru(String montant) {
        return new BigDecimal(montant);
    }

    @Test
    void plan_achatLe1erJanvier_memeDotationChaqueAnnee() {
        List<LigneAmortissementResponse> plan = CalculAmortissement.plan(mru("1200000.00"), LocalDate.of(2024, 1, 1), 3);

        assertThat(plan).extracting(LigneAmortissementResponse::exercice).containsExactly(2024, 2025, 2026);
        assertThat(plan).extracting(LigneAmortissementResponse::dotation)
                .containsExactly(mru("400000.00"), mru("400000.00"), mru("400000.00"));
        assertThat(plan.get(2).valeurNette()).isEqualByComparingTo("0");
    }

    @Test
    void plan_achatEnCoursDAnnee_prorataLaPremiereAnneeEtUneAnneeEnPlus() {
        // 2024 est bissextile (366 j). Du 1er juillet au 31 décembre : 184 jours.
        // 1re année : 250 000 × 184 / 366 = 125 683,06
        List<LigneAmortissementResponse> plan = CalculAmortissement.plan(mru("1000000.00"), LocalDate.of(2024, 7, 1), 4);

        assertThat(plan).extracting(LigneAmortissementResponse::exercice).containsExactly(2024, 2025, 2026, 2027, 2028);
        assertThat(plan).extracting(LigneAmortissementResponse::dotation).containsExactly(
                mru("125683.06"), mru("250000.00"), mru("250000.00"), mru("250000.00"), mru("124316.94"));
        assertThat(plan.get(4).cumul()).isEqualByComparingTo("1000000.00");
    }

    @Test
    void plan_laSommeDesDotationsFaitExactementLePrixMalgreLesArrondis() {
        // 1000 / 3 = 333,33 : la dernière année absorbe le centime d'écart
        List<LigneAmortissementResponse> plan = CalculAmortissement.plan(mru("1000.00"), LocalDate.of(2025, 1, 1), 3);

        assertThat(plan).extracting(LigneAmortissementResponse::dotation)
                .containsExactly(mru("333.33"), mru("333.33"), mru("333.34"));
    }

    @Test
    void plan_prixNul_aucuneLigne() {
        assertThat(CalculAmortissement.plan(mru("0.00"), LocalDate.of(2025, 1, 1), 3)).isEmpty();
    }

    @Test
    void situation_auSoirDu31Decembre_valeurDeFinDExerciceDuPlan() {
        SituationComptableResponse situation = CalculAmortissement.situation(
                mru("1200000.00"), LocalDate.of(2024, 1, 1), 3, LocalDate.of(2024, 12, 31));

        assertThat(situation.amortissementAnnuel()).isEqualByComparingTo("400000.00");
        assertThat(situation.amortissementCumule()).isEqualByComparingTo("400000.00");
        assertThat(situation.valeurNetteComptable()).isEqualByComparingTo("800000.00");
        assertThat(situation.pourcentageAmorti()).isEqualByComparingTo("33.3");
        assertThat(situation.dateFinAmortissement()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(situation.totalementAmorti()).isFalse();
    }

    @Test
    void situation_enCoursDAnnee_ajouteLesJoursEcoules() {
        // 2024 : 125 683,06 (année d'achat complète)
        // 2025 : du 1er janvier au 1er juillet inclus = 182 jours → 250 000 × 182 / 365 = 124 657,53
        SituationComptableResponse situation = CalculAmortissement.situation(
                mru("1000000.00"), LocalDate.of(2024, 7, 1), 4, LocalDate.of(2025, 7, 1));

        assertThat(situation.amortissementCumule()).isEqualByComparingTo("250340.59");
        assertThat(situation.valeurNetteComptable()).isEqualByComparingTo("749659.41");
    }

    @Test
    void situation_laVeilleDeLAchat_aucunAmortissement() {
        SituationComptableResponse situation = CalculAmortissement.situation(
                mru("500000.00"), LocalDate.of(2025, 3, 15), 5, LocalDate.of(2025, 3, 14));

        assertThat(situation.amortissementCumule()).isEqualByComparingTo("0");
        assertThat(situation.valeurNetteComptable()).isEqualByComparingTo("500000.00");
    }

    @Test
    void situation_apresLaFin_valeurNulleEtTotalementAmorti() {
        SituationComptableResponse situation = CalculAmortissement.situation(
                mru("500000.00"), LocalDate.of(2020, 3, 15), 3, LocalDate.of(2026, 10, 27));

        assertThat(situation.valeurNetteComptable()).isEqualByComparingTo("0");
        assertThat(situation.pourcentageAmorti()).isEqualByComparingTo("100");
        assertThat(situation.totalementAmorti()).isTrue();
    }
}
