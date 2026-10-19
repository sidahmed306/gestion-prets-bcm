package mr.bcm.gestionprets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.CategorieRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public List<Categorie> findAll() {
        return categorieRepository.findAll();
    }

    public Categorie findById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    @Transactional
    public Categorie creer(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    @Transactional
    public Categorie modifier(Long id, Categorie donnees) {
        Categorie categorie = findById(id);
        categorie.setCode(donnees.getCode());
        categorie.setLibelle(donnees.getLibelle());
        return categorieRepository.save(categorie);
    }

    @Transactional
    public void supprimer(Long id) {
        categorieRepository.delete(findById(id));
    }
}