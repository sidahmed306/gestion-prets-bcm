package mr.bcm.gestionprets.annuaire;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Synchronisation automatique des employés, chaque nuit (app.annuaire.synchro-cron). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.annuaire.enabled", havingValue = "true")
public class SynchronisationAnnuaireScheduler {

    private final SynchronisationEmployesService synchronisationEmployesService;

    @Scheduled(cron = "${app.annuaire.synchro-cron}")
    public void synchroniserChaqueNuit() {
        synchronisationEmployesService.synchroniser();
    }
}
