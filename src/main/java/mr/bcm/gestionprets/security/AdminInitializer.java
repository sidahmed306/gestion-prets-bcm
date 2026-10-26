package mr.bcm.gestionprets.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.repository.UtilisateurRepository;
import mr.bcm.gestionprets.service.UtilisateurService;

/**
 * Au premier démarrage, la table utilisateurs est vide : personne ne pourrait se connecter.
 * On crée donc un premier compte administrateur (identifiants dans application.properties).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (utilisateurRepository.count() == 0) {
            utilisateurService.creer(username, password, "Administrateur", Role.ADMIN);
            log.warn("Aucun utilisateur trouvé : compte administrateur '{}' créé. Changez son mot de passe.", username);
        }
    }
}
