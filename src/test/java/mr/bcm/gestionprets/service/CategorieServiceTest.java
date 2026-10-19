package mr.bcm.gestionprets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.CategorieRepository;

@ExtendWith(MockitoExtension.class)
class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    private CategorieService categorieService;

    @BeforeEach
    void setUp() {
        categorieService = new CategorieService(categorieRepository);
    }

    @Test
    void modifier_metAJourLesChampsDeLaCategorieExistante() {
        Categorie existante = Categorie.builder().id(1L).code("ORDI").libelle("Ordinateur").build();
        Categorie donnees = Categorie.builder().code("ECR").libelle("Écran").build();
        when(categorieRepository.findById(1L)).thenReturn(Optional.of(existante));
        when(categorieRepository.save(existante)).thenReturn(existante);

        Categorie resultat = categorieService.modifier(1L, donnees);

        assertThat(resultat.getCode()).isEqualTo("ECR");
        assertThat(resultat.getLibelle()).isEqualTo("Écran");
    }

    @Test
    void findById_rejetteSiLaCategorieEstInconnue() {
        when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categorieService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
