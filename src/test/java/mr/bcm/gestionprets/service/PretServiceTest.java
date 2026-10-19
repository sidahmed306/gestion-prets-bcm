package mr.bcm.gestionprets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.EmployeRepository;
import mr.bcm.gestionprets.repository.MaterielRepository;
import mr.bcm.gestionprets.repository.PretRepository;

@ExtendWith(MockitoExtension.class)
class PretServiceTest {

    @Mock
    private PretRepository pretRepository;

    @Mock
    private MaterielRepository materielRepository;

    @Mock
    private EmployeRepository employeRepository;

    private PretService pretService;

    private Materiel materiel;
    private Employe employe;

    @BeforeEach
    void setUp() {
        pretService = new PretService(pretRepository, materielRepository, employeRepository);
        materiel = Materiel.builder().id(1L).numeroInventaire("INV001").build();
        employe = Employe.builder().id(1L).matricule("MAT001").nom("Ahmed").build();
    }

    @Test
    void creerPret_creeLePretQuandLeMaterielEstDisponible() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
        when(pretRepository.findByMaterielIdAndStatut(1L, StatutPret.EN_COURS)).thenReturn(Optional.empty());
        when(pretRepository.save(any(Pret.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pret pret = pretService.creerPret(1L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), "Mission");

        assertThat(pret.getStatut()).isEqualTo(StatutPret.EN_COURS);
        assertThat(pret.getMateriel()).isEqualTo(materiel);
        assertThat(pret.getEmploye()).isEqualTo(employe);
        assertThat(pret.getCommentaire()).isEqualTo("Mission");
    }

    @Test
    void creerPret_rejetteSiDateRetourPrevueAvantDatePret() {
        assertThatThrownBy(() -> pretService.creerPret(
                1L, 1L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1), null))
                .isInstanceOf(BusinessRuleException.class);

        verify(pretRepository, never()).save(any());
    }

    @Test
    void creerPret_rejetteSiMaterielInconnu() {
        when(materielRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pretService.creerPret(
                99L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creerPret_rejetteSiEmployeInconnu() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(employeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pretService.creerPret(
                1L, 99L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creerPret_rejetteSiLeMaterielAdejaUnPretEnCours() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
        Pret pretExistant = Pret.builder().id(42L).statut(StatutPret.EN_COURS).build();
        when(pretRepository.findByMaterielIdAndStatut(1L, StatutPret.EN_COURS))
                .thenReturn(Optional.of(pretExistant));

        assertThatThrownBy(() -> pretService.creerPret(
                1L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("42");

        verify(pretRepository, never()).save(any());
    }

    @Test
    void retournerPret_marqueLePretRetourneEtFixeLaDate() {
        Pret pret = Pret.builder().id(1L).statut(StatutPret.EN_COURS).build();
        when(pretRepository.findById(1L)).thenReturn(Optional.of(pret));
        when(pretRepository.save(any(Pret.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pret resultat = pretService.retournerPret(1L, LocalDate.of(2026, 10, 7));

        assertThat(resultat.getStatut()).isEqualTo(StatutPret.RETOURNE);
        assertThat(resultat.getDateRetourEffective()).isEqualTo(LocalDate.of(2026, 10, 7));
    }

    @Test
    void retournerPret_utiliseLaDateDuJourQuandNonFournie() {
        Pret pret = Pret.builder().id(1L).statut(StatutPret.EN_COURS).build();
        when(pretRepository.findById(1L)).thenReturn(Optional.of(pret));
        ArgumentCaptor<Pret> captor = ArgumentCaptor.forClass(Pret.class);
        when(pretRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        pretService.retournerPret(1L, null);

        assertThat(captor.getValue().getDateRetourEffective()).isEqualTo(LocalDate.now());
    }

    @Test
    void retournerPret_rejetteSiLePretEstDejaRetourne() {
        Pret pret = Pret.builder().id(1L).statut(StatutPret.RETOURNE).build();
        when(pretRepository.findById(1L)).thenReturn(Optional.of(pret));

        assertThatThrownBy(() -> pretService.retournerPret(1L, LocalDate.now()))
                .isInstanceOf(BusinessRuleException.class);

        verify(pretRepository, never()).save(any());
    }

    @Test
    void retournerPret_rejetteSiLePretEstInconnu() {
        when(pretRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pretService.retournerPret(99L, LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findEnCours_delegueAuRepositoryAvecLeStatutEnCours() {
        Pret pret = Pret.builder().id(1L).statut(StatutPret.EN_COURS).build();
        when(pretRepository.findByStatut(StatutPret.EN_COURS)).thenReturn(List.of(pret));

        List<Pret> resultat = pretService.findEnCours();

        assertThat(resultat).containsExactly(pret);
    }

    @Test
    void findByMateriel_delegueAuRepository() {
        Pret pret = Pret.builder().id(1L).build();
        when(pretRepository.findByMaterielId(1L)).thenReturn(List.of(pret));

        List<Pret> resultat = pretService.findByMateriel(1L);

        assertThat(resultat).containsExactly(pret);
    }
}
