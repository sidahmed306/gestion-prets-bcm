package mr.bcm.gestionprets.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import mr.bcm.gestionprets.entity.Materiel;

public interface MaterielRepository extends JpaRepository<Materiel, Long> {

    Optional<Materiel> findByNumeroInventaire(String numeroInventaire);

    boolean existsByNumeroInventaire(String numeroInventaire);

    List<Materiel> findByCategorieId(Long categorieId);

    List<Materiel> findByDesignationContainingIgnoreCase(String designation);

    @Query("""
            SELECT m FROM Materiel m
            WHERE NOT EXISTS (
                SELECT p FROM Pret p
                WHERE p.materiel.id = m.id AND p.statut = mr.bcm.gestionprets.entity.StatutPret.EN_COURS
            )
            """)
    List<Materiel> findDisponibles();
}
