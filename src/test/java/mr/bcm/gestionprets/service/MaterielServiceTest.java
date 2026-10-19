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

import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.CategorieRepository;
import mr.bcm.gestionprets.repository.MaterielRepository;

@ExtendWith(MockitoExtension.class)
class MaterielServiceTest {

    @Mock
    private MaterielRepository materielRepository;

    @Mock
    private CategorieRepository categorieRepository;

    private MaterielService materielService;

    @BeforeEach
    void setUp() {
        materielService = new MaterielService(materielRepository, categorieRepository);
    }

    @Test
    void creer_resoutLaCategorieEtSauvegardeQuandLeNumeroEstLibre() {
        Categorie categorie = Categorie.builder().id(1L).code("ORDI").build();
        Materiel materiel = Materiel.builder()
                .numeroInventaire("INV001")
                .categorie(Categorie.builder().id(1L).build())
                .build();
        when(materielRepository.existsByNumeroInventaire("INV001")).thenReturn(false);
        when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        when(materielRepository.save(materiel)).thenReturn(materiel);

        Materiel resultat = materielService.creer(materiel);

        assertThat(resultat.getCategorie()).isEqualTo(categorie);
    }

    @Test
    void creer_rejetteSiLeNumeroInventaireEstDejaUtilise() {
        Materiel materiel = Materiel.builder()
                .numeroInventaire("INV001")
                .categorie(Categorie.builder().id(1L).build())
                .build();
        when(materielRepository.existsByNumeroInventaire("INV001")).thenReturn(true);

        assertThatThrownBy(() -> materielService.creer(materiel))
                .isInstanceOf(BusinessRuleException.class);

        verify(materielRepository, never()).save(any());
    }

    @Test
    void creer_rejetteSiLaCategorieEstInconnue() {
        Materiel materiel = Materiel.builder()
                .numeroInventaire("INV001")
                .categorie(Categorie.builder().id(99L).build())
                .build();
        when(materielRepository.existsByNumeroInventaire("INV001")).thenReturn(false);
        when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materielService.creer(materiel))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creer_rejetteSiAucuneCategorieNestFournie() {
        Materiel materiel = Materiel.builder().numeroInventaire("INV001").build();
        when(materielRepository.existsByNumeroInventaire("INV001")).thenReturn(false);

        assertThatThrownBy(() -> materielService.creer(materiel))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findDisponibles_delegueAuRepository() {
        Materiel disponible = Materiel.builder().id(1L).numeroInventaire("INV001").build();
        when(materielRepository.findDisponibles()).thenReturn(List.of(disponible));

        List<Materiel> resultat = materielService.findDisponibles();

        assertThat(resultat).containsExactly(disponible);
    }

    @Test
    void rechercherParDesignation_delegueAuRepository() {
        Materiel trouve = Materiel.builder().id(1L).designation("Dell Latitude").build();
        when(materielRepository.findByDesignationContainingIgnoreCase("latitude")).thenReturn(List.of(trouve));

        List<Materiel> resultat = materielService.rechercherParDesignation("latitude");

        assertThat(resultat).containsExactly(trouve);
    }
}
