package mr.bcm.gestionprets.comptabilite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mr.bcm.gestionprets.dto.PlanAmortissementResponse;
import mr.bcm.gestionprets.dto.SyntheseComptableResponse;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.repository.MaterielRepository;
import mr.bcm.gestionprets.service.MaterielService;

@ExtendWith(MockitoExtension.class)
class ComptabiliteServiceTest {

    private static final LocalDate DATE_CALCUL = LocalDate.of(2025, 12, 31); // clôture de l'exercice 2025

    @Mock
    private MaterielRepository materielRepository;

    @Mock
    private MaterielService materielService;

    private ComptabiliteService comptabiliteService;

    private final Categorie ordinateurs = Categorie.builder().id(1L).libelle("Ordinateur portable").build();
    private final Categorie ecrans = Categorie.builder().id(2L).libelle("Écran").build();

    @BeforeEach
    void setUp() {
        comptabiliteService = new ComptabiliteService(materielRepository, materielService);
    }

    private Materiel materiel(String inventaire, Categorie categorie, String prix, LocalDate dateAchat, Integer duree) {
        return Materiel.builder().numeroInventaire(inventaire).categorie(categorie)
                .prixAchat(prix != null ? new BigDecimal(prix) : null).dateAchat(dateAchat)
                .dureeAmortissementAnnees(duree).build();
    }

    @Test
    void synthese_additionneLesMaterielsValorisesEtIgnoreLesAutres() {
        when(materielRepository.findAll()).thenReturn(List.of(
                // 1 an écoulé sur 3 : vaut encore 800 000
                materiel("INV001", ordinateurs, "1200000.00", LocalDate.of(2025, 1, 1), 3),
                // totalement amorti : vaut 0
                materiel("INV002", ordinateurs, "600000.00", LocalDate.of(2020, 1, 1), 3),
                // 1 an écoulé sur 5 : vaut encore 200 000
                materiel("INV003", ecrans, "250000.00", LocalDate.of(2025, 1, 1), 5),
                // pas d'infos comptables : ignoré
                materiel("INV004", ecrans, null, null, null)));

        SyntheseComptableResponse synthese = comptabiliteService.synthese(DATE_CALCUL);

        assertThat(synthese.nombreMateriels()).isEqualTo(4);
        assertThat(synthese.nombreValorises()).isEqualTo(3);
        assertThat(synthese.nombreNonValorises()).isEqualTo(1);
        assertThat(synthese.nombreTotalementAmortis()).isEqualTo(1);
        assertThat(synthese.valeurAchatTotale()).isEqualByComparingTo("2050000.00");
        assertThat(synthese.valeurNetteTotale()).isEqualByComparingTo("1000000.00");
        assertThat(synthese.amortissementCumuleTotal()).isEqualByComparingTo("1050000.00");
        // Dotations 2025 : 400 000 (INV001) + 0 (INV002 terminé) + 50 000 (INV003)
        assertThat(synthese.dotationExerciceEnCours()).isEqualByComparingTo("450000.00");

        assertThat(synthese.parCategorie()).extracting(SyntheseComptableResponse.ParCategorie::categorie)
                .containsExactly("Ordinateur portable", "Écran");
        SyntheseComptableResponse.ParCategorie portables = synthese.parCategorie().get(0);
        assertThat(portables.nombre()).isEqualTo(2);
        assertThat(portables.valeurAchat()).isEqualByComparingTo("1800000.00");
        assertThat(portables.valeurNette()).isEqualByComparingTo("800000.00");
    }

    @Test
    void synthese_parcVide() {
        when(materielRepository.findAll()).thenReturn(List.of());

        SyntheseComptableResponse synthese = comptabiliteService.synthese(DATE_CALCUL);

        assertThat(synthese.valeurAchatTotale()).isEqualByComparingTo("0");
        assertThat(synthese.parCategorie()).isEmpty();
    }

    @Test
    void planAmortissement_renvoieUneLigneParAnnee() {
        Materiel m = materiel("INV001", ordinateurs, "1200000.00", LocalDate.of(2025, 1, 1), 3);
        m.setId(1L);
        when(materielService.findById(1L)).thenReturn(m);

        PlanAmortissementResponse plan = comptabiliteService.planAmortissement(1L);

        assertThat(plan.annuite()).isEqualByComparingTo("400000.00");
        assertThat(plan.lignes()).hasSize(3);
    }

    @Test
    void planAmortissement_refuseUnMaterielSansInfosComptables() {
        when(materielService.findById(4L)).thenReturn(materiel("INV004", ecrans, null, null, null));

        assertThatThrownBy(() -> comptabiliteService.planAmortissement(4L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("INV004");
    }
}
