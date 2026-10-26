package mr.bcm.gestionprets.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mr.bcm.gestionprets.service.PretService;

/**
 * Tâche automatique : Spring appelle cette méthode tout seul, selon l'horaire défini
 * par app.notifications.rappel-cron (par défaut du lundi au vendredi à 8h00).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RappelRetardScheduler {

    private final PretService pretService;

    @Scheduled(cron = "${app.notifications.rappel-cron}")
    public void envoyerRappelsQuotidiens() {
        int envoyes = pretService.envoyerRappelsRetard();
        log.info("Rappel automatique des prêts en retard : {} email(s) envoyé(s)", envoyes);
    }
}
