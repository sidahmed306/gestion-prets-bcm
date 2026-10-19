package mr.bcm.gestionprets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.CategorieRepository;
import mr.bcm.gestionprets.repository.MaterielRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterielService {

    private final MaterielRepository materielRepository;
    private final CategorieRepository categorieRepository;

    public List<Materiel> findAll() {
        return materielRepository.findAll();
    }

    public Materiel findById(Long id) {
        return materielRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable : " + id));
    }

    public List<Materiel> findDisponibles() {
        return materielRepository.findDisponibles();
    }

    public List<Materiel> rechercherParDesignation(String designation) {
        return materielRepository.findByDesignationContainingIgnoreCase(designation);
    }

    @Transactional
    public Materiel creer(Materiel materiel) {
        if (materielRepository.existsByNumeroInventaire(materiel.getNumeroInventaire())) {
            throw new BusinessRuleException(
                    "Le numéro d'inventaire " + materiel.getNumeroInventaire() + " est déjà utilisé");
        }
        materiel.setCategorie(resoudreCategorie(materiel.getCategorie()));
        return materielRepository.save(materiel);
    }

    @Transactional
    public Materiel modifier(Long id, Materiel donnees) {
        Materiel materiel = findById(id);
        if (!materiel.getNumeroInventaire().equals(donnees.getNumeroInventaire())
                && materielRepository.existsByNumeroInventaire(donnees.getNumeroInventaire())) {
            throw new BusinessRuleException(
                    "Le numéro d'inventaire " + donnees.getNumeroInventaire() + " est déjà utilisé");
        }
        materiel.setNumeroInventaire(donnees.getNumeroInventaire());
        materiel.setDesignation(donnees.getDesignation());
        materiel.setMarque(donnees.getMarque());
        materiel.setModele(donnees.getModele());
        materiel.setEtatPhysique(donnees.getEtatPhysique());
        materiel.setCategorie(resoudreCategorie(donnees.getCategorie()));
        return materielRepository.save(materiel);
    }

    @Transactional
    public void supprimer(Long id) {
        materielRepository.delete(findById(id));
    }

    private Categorie resoudreCategorie(Categorie reference) {
        if (reference == null || reference.getId() == null) {
            throw new BusinessRuleException("La catégorie est obligatoire");
        }
        return categorieRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + reference.getId()));
    }
}
