package mr.bcm.gestionprets.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    private NotificationService notificationService;

    private Pret pret;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(emailService);
        Employe employe = Employe.builder().matricule("MAT001").nom("Ahmed Sidi").email("ahmed.sidi@bcm.mr").build();
        Materiel materiel = Materiel.builder().numeroInventaire("INV001").designation("Ordinateur portable Dell").build();
        pret = Pret.builder().employe(employe).materiel(materiel)
                .datePret(LocalDate.of(2026, 10, 1)).dateRetourPrevue(LocalDate.of(2026, 10, 10))
                .statut(StatutPret.EN_COURS).build();
    }

    @Test
    void notifierPretCree_envoieUnEmailALEmployeAvecLesInfosDuPret() {
        notificationService.notifierPretCree(pret);

        ArgumentCaptor<String> texte = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyer(eq("ahmed.sidi@bcm.mr"), eq("Prêt de matériel : INV001"), texte.capture());
        assertThat(texte.getValue())
                .contains("Bonjour Ahmed Sidi")
                .contains("Ordinateur portable Dell (n° d'inventaire INV001)")
                .contains("01/10/2026")
                .contains("10/10/2026");
    }

    @Test
    void notifierRetour_confirmeLaDateDeRetour() {
        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetourEffective(LocalDate.of(2026, 10, 8));

        notificationService.notifierRetour(pret);

        ArgumentCaptor<String> texte = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyer(eq("ahmed.sidi@bcm.mr"), anyString(), texte.capture());
        assertThat(texte.getValue()).contains("Rendu le : 08/10/2026");
    }

    @Test
    void notifierRetard_indiqueLeNombreDeJoursDeRetard() {
        pret.setDateRetourPrevue(LocalDate.now().minusDays(3));

        boolean envoye = notificationService.notifierRetard(pret);

        assertThat(envoye).isTrue();
        verify(emailService).envoyer(eq("ahmed.sidi@bcm.mr"), eq("RAPPEL : matériel à rendre (3 j de retard)"), anyString());
    }

    @Test
    void aucunEmailSiLEmployeNAPasDAdresse() {
        pret.getEmploye().setEmail(null);

        boolean envoye = notificationService.notifierRetard(pret);
        notificationService.notifierPretCree(pret);

        assertThat(envoye).isFalse();
        verify(emailService, never()).envoyer(any(), any(), any());
    }

    @Test
    void dansUneTransaction_lEmailPartSeulementApresLeCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            notificationService.notifierPretCree(pret);

            // Rien n'est envoyé tant que la transaction n'est pas validée
            verify(emailService, never()).envoyer(any(), any(), any());

            // Simulation du commit
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(emailService).envoyer(eq("ahmed.sidi@bcm.mr"), anyString(), anyString());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
