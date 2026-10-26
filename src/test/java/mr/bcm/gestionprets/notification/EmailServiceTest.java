package mr.bcm.gestionprets.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void envoyer_construitLeMessageAvecLExpediteurConfigure() {
        EmailService emailService = new EmailService(mailSender, true, "gestion-prets@bcm.mr");

        emailService.envoyer("ahmed.sidi@bcm.mr", "Sujet", "Texte");

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("gestion-prets@bcm.mr");
        assertThat(message.getValue().getTo()).containsExactly("ahmed.sidi@bcm.mr");
        assertThat(message.getValue().getSubject()).isEqualTo("Sujet");
        assertThat(message.getValue().getText()).isEqualTo("Texte");
    }

    @Test
    void envoyer_nEnvoieRienQuandLesNotificationsSontDesactivees() {
        EmailService emailService = new EmailService(mailSender, false, "gestion-prets@bcm.mr");

        emailService.envoyer("ahmed.sidi@bcm.mr", "Sujet", "Texte");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void envoyer_neLancePasDErreurQuandLeServeurMailEstInjoignable() {
        EmailService emailService = new EmailService(mailSender, true, "gestion-prets@bcm.mr");
        doThrow(new MailSendException("Connexion refusée")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.envoyer("ahmed.sidi@bcm.mr", "Sujet", "Texte")).doesNotThrowAnyException();
    }
}
