package mr.bcm.gestionprets.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mr.bcm.gestionprets.entity.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByCode(String code);
}