package mr.bcm.gestionprets.notification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.entity.Pret;

/**
 * Décide QUOI envoyer et À QUI (le contenu des messages).
 * L'envoi technique est délégué à {@link EmailService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SIGNATURE = """

            Cordialement,
            Service informatique — Banque Centrale de Mauritanie

            Ce message est envoyé automatiquement, merci de ne pas y répondre.""";

    private final EmailService emailService;

    public void notifierPretCree(Pret pret) {
        Employe employe = pret.getEmploye();
        String texte = """
                Bonjour %s,

                Le matériel suivant vous a été prêté :
                  - Matériel : %s
                  - Date du prêt : %s
                  - Date de retour prévue : %s

                Merci de le rapporter au service informatique au plus tard à cette date.
                """.formatted(employe.getNom(), decrire(pret.getMateriel()),
                pret.getDatePret().format(DATE), pret.getDateRetourPrevue().format(DATE)) + SIGNATURE;
        envoyerApresEnregistrement(employe, "Prêt de matériel : " + pret.getMateriel().getNumeroInventaire(), texte);
    }

    public void notifierRetour(Pret pret) {
        Employe employe = pret.getEmploye();
        String texte = """
                Bonjour %s,

                Nous confirmons le retour du matériel suivant :
                  - Matériel : %s
                  - Rendu le : %s

                Merci.
                """.formatted(employe.getNom(), decrire(pret.getMateriel()),
                pret.getDateRetourEffective().format(DATE)) + SIGNATURE;
        envoyerApresEnregistrement(employe, "Retour de matériel confirmé : " + pret.getMateriel().getNumeroInventaire(), texte);
    }

    /** @return true si un email a été programmé (false si l'employé n'a pas d'adresse email) */
    public boolean notifierRetard(Pret pret) {
        Employe employe = pret.getEmploye();
        long joursDeRetard = ChronoUnit.DAYS.between(pret.getDateRetourPrevue(), LocalDate.now());
        String texte = """
                Bonjour %s,

                Le matériel suivant aurait dû être rendu le %s (%d jour(s) de retard) :
                  - Matériel : %s

                Merci de le rapporter au service informatique dès que possible.
                """.formatted(employe.getNom(), pret.getDateRetourPrevue().format(DATE), joursDeRetard,
                decrire(pret.getMateriel())) + SIGNATURE;
        return envoyerApresEnregistrement(employe, "RAPPEL : matériel à rendre (" + joursDeRetard + " j de retard)", texte);
    }

    /**
     * On attend que la transaction en base soit validée (commit) avant d'envoyer :
     * si l'enregistrement du prêt échoue, aucun email n'est parti pour un prêt qui n'existe pas.
     */
    private boolean envoyerApresEnregistrement(Employe employe, String sujet, String texte) {
        String destinataire = employe.getEmail();
        if (destinataire == null || destinataire.isBlank()) {
            log.info("Pas d'email pour l'employé {} : notification « {} » non envoyée", employe.getMatricule(), sujet);
            return false;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.envoyer(destinataire, sujet, texte);
                }
            });
        } else {
            emailService.envoyer(destinataire, sujet, texte);
        }
        return true;
    }

    private String decrire(Materiel materiel) {
        String nom = materiel.getDesignation() != null && !materiel.getDesignation().isBlank()
                ? materiel.getDesignation()
                : "Matériel";
        return nom + " (n° d'inventaire " + materiel.getNumeroInventaire() + ")";
    }
}
