package mr.bcm.gestionprets.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;

public interface PretRepository extends JpaRepository<Pret, Long> {

    Optional<Pret> findByMaterielIdAndStatut(Long materielId, StatutPret statut);

    List<Pret> findByEmployeId(Long employeId);

    List<Pret> findByMaterielId(Long materielId);

    List<Pret> findByStatut(StatutPret statut);

    List<Pret> findByStatutAndDateRetourPrevueBefore(StatutPret statut, LocalDate date);
}
