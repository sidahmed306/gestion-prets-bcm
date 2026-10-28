package mr.bcm.gestionprets.annuaire;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.SourceCompte;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.exception.AccesRefuseException;
import mr.bcm.gestionprets.repository.UtilisateurRepository;

/**
 * Appelé juste après qu'un agent s'est connecté avec succès via l'annuaire.
 * <ol>
 *   <li>Son rôle est déduit de sa DIRECTION et de son POSTE (règles d'accès de application.properties).</li>
 *   <li>L'application garde une copie de son compte dans la table "utilisateurs"
 *       (création automatique à la 1re connexion = "provisionnement juste-à-temps") :
 *       le reste de l'application (jeton JWT, contrôle des droits à chaque requête,
 *       page Utilisateurs, désactivation par l'admin) fonctionne ainsi exactement comme avant.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnexionAnnuaireService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnnuaireProperties proprietes;

    @Transactional
    public Utilisateur connecter(AgentAnnuaire agent) {
        Role role = roleDeLAgent(agent);

        Utilisateur utilisateur = utilisateurRepository.findByUsername(agent.identifiant()).orElse(null);
        if (utilisateur == null) {
            utilisateur = Utilisateur.builder()
                    .username(agent.identifiant())
                    // Mot de passe aléatoire jamais communiqué : impossible de se connecter en "local" avec ce compte.
                    // Le vrai mot de passe reste dans l'annuaire.
                    .motDePasse(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .source(SourceCompte.ANNUAIRE)
                    .actif(true)
                    .build();
            log.info("Première connexion de {} ({}, {}) via l'annuaire : compte créé avec le rôle {}",
                    agent.identifiant(), agent.poste(), agent.departement(), role);
        } else if (utilisateur.getSource() == SourceCompte.LOCAL) {
            throw new AccesRefuseException("Un compte local « " + agent.identifiant() + " » existe déjà dans "
                    + "l'application. Connectez-vous avec son mot de passe local, ou demandez à l'administrateur de le supprimer.");
        } else if (!utilisateur.isActif()) {
            throw new DisabledException("Ce compte est désactivé");
        }

        // À chaque connexion, on recopie ce que dit l'annuaire : il fait foi (mutation, promotion...)
        utilisateur.setNomComplet(agent.nom());
        utilisateur.setDepartement(agent.departement());
        utilisateur.setPoste(agent.poste());
        utilisateur.setRole(role);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        return utilisateurRepository.save(utilisateur);
    }

    /** La 1re règle qui correspond à la direction et au poste de l'agent donne son rôle. Aucune = pas d'accès. */
    Role roleDeLAgent(AgentAnnuaire agent) {
        return proprietes.regles().stream()
                .filter(regle -> regle.correspond(agent.departement(), agent.poste()))
                .map(AnnuaireProperties.RegleAcces::role)
                .findFirst()
                .orElseThrow(() -> new AccesRefuseException("Votre poste (" + agent.poste() + ", "
                        + agent.departement() + ") ne donne pas accès à cette application. "
                        + "Elle est réservée à la DSI et aux directeurs."));
    }
}
