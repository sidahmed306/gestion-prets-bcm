package mr.bcm.gestionprets.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Envoie un email texte. C'est la seule classe qui parle au serveur mail (SMTP).
 * <p>
 * {@code @Async} : l'envoi se fait dans un autre fil d'exécution (thread). L'utilisateur
 * n'attend pas le serveur mail, et si l'envoi échoue, le prêt reste bien enregistré.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean active;
    private final String expediteur;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.notifications.enabled}") boolean active,
            @Value("${app.notifications.expediteur}") String expediteur) {
        this.mailSender = mailSender;
        this.active = active;
        this.expediteur = expediteur;
    }

    @Async
    public void envoyer(String destinataire, String sujet, String texte) {
        if (!active) {
            log.info("[Notifications désactivées] Email non envoyé à {} : {}", destinataire, sujet);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(texte);
        try {
            mailSender.send(message);
            log.info("Email envoyé à {} : {}", destinataire, sujet);
        } catch (MailException e) {
            // Un email raté ne doit jamais faire planter l'application : on l'écrit dans les logs
            log.error("Échec de l'envoi de l'email à {} ({}) : {}", destinataire, sujet, e.getMessage());
        }
    }
}
