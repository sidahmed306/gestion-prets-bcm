package mr.bcm.gestionprets.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mr.bcm.gestionprets.entity.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

    Optional<Employe> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    List<Employe> findByNomContainingIgnoreCase(String nom);
}