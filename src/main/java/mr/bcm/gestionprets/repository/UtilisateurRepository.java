package mr.bcm.gestionprets.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mr.bcm.gestionprets.entity.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByUsername(String username);

    boolean existsByUsername(String username);
}
