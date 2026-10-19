package mr.bcm.gestionprets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.EmployeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;

    private EmployeService employeService;

    @BeforeEach
    void setUp() {
        employeService = new EmployeService(employeRepository);
    }

    @Test
    void creer_sauvegardeLEmployeQuandLeMatriculeEstLibre() {
        Employe employe = Employe.builder().matricule("MAT001").nom("Ahmed").build();
        when(employeRepository.existsByMatricule("MAT001")).thenReturn(false);
        when(employeRepository.save(employe)).thenReturn(employe);

        Employe resultat = employeService.creer(employe);

        assertThat(resultat).isEqualTo(employe);
    }

    @Test
    void creer_rejetteSiLeMatriculeEstDejaUtilise() {
        Employe employe = Employe.builder().matricule("MAT001").nom("Ahmed").build();
        when(employeRepository.existsByMatricule("MAT001")).thenReturn(true);

        assertThatThrownBy(() -> employeService.creer(employe))
                .isInstanceOf(BusinessRuleException.class);

        verify(employeRepository, never()).save(any());
    }

    @Test
    void modifier_rejetteSiLeNouveauMatriculeEstDejaPrisParUnAutre() {
        Employe existant = Employe.builder().id(1L).matricule("MAT001").nom("Ahmed").build();
        Employe donnees = Employe.builder().matricule("MAT002").nom("Ahmed").build();
        when(employeRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(employeRepository.existsByMatricule("MAT002")).thenReturn(true);

        assertThatThrownBy(() -> employeService.modifier(1L, donnees))
                .isInstanceOf(BusinessRuleException.class);

        verify(employeRepository, never()).save(any());
    }

    @Test
    void modifier_autoriseASauvegarderQuandLeMatriculeNeChangePas() {
        Employe existant = Employe.builder().id(1L).matricule("MAT001").nom("Ahmed").build();
        Employe donnees = Employe.builder().matricule("MAT001").nom("Ahmed Modifie").build();
        when(employeRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(employeRepository.save(existant)).thenReturn(existant);

        Employe resultat = employeService.modifier(1L, donnees);

        assertThat(resultat.getNom()).isEqualTo("Ahmed Modifie");
        verify(employeRepository, never()).existsByMatricule(any());
    }

    @Test
    void findById_rejetteSiLemployeEstInconnu() {
        when(employeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechercherParNom_delegueAuRepository() {
        Employe trouve = Employe.builder().id(1L).matricule("MAT001").nom("Ahmed Sidi").build();
        when(employeRepository.findByNomContainingIgnoreCase("ahmed")).thenReturn(List.of(trouve));

        List<Employe> resultat = employeService.rechercherParNom("ahmed");

        assertThat(resultat).containsExactly(trouve);
    }
}
