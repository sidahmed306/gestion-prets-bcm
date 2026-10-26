package mr.bcm.gestionprets.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.notification.NotificationService;
import mr.bcm.gestionprets.repository.EmployeRepository;
import mr.bcm.gestionprets.repository.MaterielRepository;
import mr.bcm.gestionprets.repository.PretRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PretService {

    private final PretRepository pretRepository;
    private final MaterielRepository materielRepository;
    private final EmployeRepository employeRepository;
    private final NotificationService notificationService;

    public List<Pret> findAll() {
        return pretRepository.findAll();
    }

    public Pret findById(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable : " + id));
    }

    public List<Pret> findByEmploye(Long employeId) {
        return pretRepository.findByEmployeId(employeId);
    }

    public List<Pret> findByMateriel(Long materielId) {
        return pretRepository.findByMaterielId(materielId);
    }

    public List<Pret> findEnCours() {
        return pretRepository.findByStatut(StatutPret.EN_COURS);
    }

    public List<Pret> findEnRetard() {
        return pretRepository.findByStatutAndDateRetourPrevueBefore(StatutPret.EN_COURS, LocalDate.now());
    }

    @Transactional
    public Pret creerPret(Long materielId, Long employeId, LocalDate datePret, LocalDate dateRetourPrevue,
            String commentaire) {
        if (dateRetourPrevue.isBefore(datePret)) {
            throw new BusinessRuleException("La date de retour prévue doit être postérieure ou égale à la date de prêt");
        }

        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable : " + materielId));
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + employeId));

        pretRepository.findByMaterielIdAndStatut(materielId, StatutPret.EN_COURS)
                .ifPresent(p -> {
                    throw new BusinessRuleException(
                            "Ce matériel a déjà un prêt en cours (prêt #" + p.getId() + ")");
                });

        Pret pret = Pret.builder()
                .materiel(materiel)
                .employe(employe)
                .datePret(datePret)
                .dateRetourPrevue(dateRetourPrevue)
                .statut(StatutPret.EN_COURS)
                .commentaire(commentaire)
                .build();

        Pret enregistre = pretRepository.save(pret);
        notificationService.notifierPretCree(enregistre);
        return enregistre;
    }

    @Transactional
    public Pret retournerPret(Long pretId, LocalDate dateRetourEffective) {
        Pret pret = findById(pretId);

        if (pret.getStatut() == StatutPret.RETOURNE) {
            throw new BusinessRuleException("Le prêt #" + pretId + " a déjà été retourné");
        }

        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetourEffective(dateRetourEffective != null ? dateRetourEffective : LocalDate.now());
        Pret enregistre = pretRepository.save(pret);
        notificationService.notifierRetour(enregistre);
        return enregistre;
    }

    /**
     * Envoie un rappel à chaque employé qui a un prêt en retard.
     * Appelé automatiquement chaque matin (RappelRetardScheduler) ou à la main depuis l'interface.
     *
     * @return le nombre d'emails envoyés (les employés sans adresse email sont ignorés)
     */
    public int envoyerRappelsRetard() {
        int envoyes = 0;
        for (Pret pret : findEnRetard()) {
            if (notificationService.notifierRetard(pret)) {
                envoyes++;
            }
        }
        return envoyes;
    }
}
