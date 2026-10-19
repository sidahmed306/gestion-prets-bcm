package mr.bcm.gestionprets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.EmployeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeService {

    private final EmployeRepository employeRepository;

    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + id));
    }

    public List<Employe> rechercherParNom(String nom) {
        return employeRepository.findByNomContainingIgnoreCase(nom);
    }

    @Transactional
    public Employe creer(Employe employe) {
        if (employeRepository.existsByMatricule(employe.getMatricule())) {
            throw new BusinessRuleException("Le matricule " + employe.getMatricule() + " est déjà utilisé");
        }
        return employeRepository.save(employe);
    }

    @Transactional
    public Employe modifier(Long id, Employe donnees) {
        Employe employe = findById(id);
        if (!employe.getMatricule().equals(donnees.getMatricule())
                && employeRepository.existsByMatricule(donnees.getMatricule())) {
            throw new BusinessRuleException("Le matricule " + donnees.getMatricule() + " est déjà utilisé");
        }
        employe.setMatricule(donnees.getMatricule());
        employe.setNom(donnees.getNom());
        employe.setDepartement(donnees.getDepartement());
        employe.setEmail(donnees.getEmail());
        return employeRepository.save(employe);
    }

    @Transactional
    public void supprimer(Long id) {
        employeRepository.delete(findById(id));
    }
}
