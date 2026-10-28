package mr.bcm.gestionprets.annuaire;

import java.util.List;
import java.util.Objects;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mr.bcm.gestionprets.dto.SynchronisationResponse;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.repository.EmployeRepository;

/**
 * Copie les agents de l'annuaire dans notre table "employes" (toutes les directions).
 * <p>
 * Règles :
 * <ul>
 *   <li>Le MATRICULE fait le lien entre l'annuaire et notre base.</li>
 *   <li>Matricule inconnu chez nous → employé créé. Déjà connu → nom, direction et email mis à jour.</li>
 *   <li>On ne supprime jamais un employé parti de l'annuaire : il peut avoir un historique de prêts.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SynchronisationEmployesService {

    private final LdapTemplate ldapTemplate;
    private final EmployeRepository employeRepository;
    private final AnnuaireProperties proprietes;

    @Transactional
    public SynchronisationResponse synchroniser() {
        if (!proprietes.enabled()) {
            throw new BusinessRuleException("L'annuaire d'entreprise est désactivé (app.annuaire.enabled=false)");
        }
        List<AgentAnnuaire> agents = lireAgents();
        int crees = 0;
        int misAJour = 0;
        int inchanges = 0;
        int ignores = 0;

        for (AgentAnnuaire agent : agents) {
            if (agent.matricule() == null || agent.matricule().isBlank()) {
                ignores++;
                continue;
            }
            Employe employe = employeRepository.findByMatricule(agent.matricule()).orElse(null);
            if (employe == null) {
                employeRepository.save(Employe.builder()
                        .matricule(agent.matricule())
                        .nom(agent.nom())
                        .departement(agent.departement())
                        .email(agent.email())
                        .build());
                crees++;
            } else if (aChange(employe, agent)) {
                employe.setNom(agent.nom());
                employe.setDepartement(agent.departement());
                employe.setEmail(agent.email());
                employeRepository.save(employe);
                misAJour++;
            } else {
                inchanges++;
            }
        }
        log.info("Synchronisation annuaire : {} lus, {} créés, {} mis à jour, {} inchangés, {} ignorés",
                agents.size(), crees, misAJour, inchanges, ignores);
        return new SynchronisationResponse(agents.size(), crees, misAJour, inchanges, ignores);
    }

    /** Recherche LDAP : tous les agents (objectClass=person) dans toutes les directions sous la base. */
    List<AgentAnnuaire> lireAgents() {
        ContextMapper<AgentAnnuaire> versAgent = resultat -> {
            DirContextOperations fiche = (DirContextOperations) resultat;
            return AgentAnnuaire.depuis(fiche, null, proprietes);
        };
        return ldapTemplate.search(
                LdapQueryBuilder.query().base(proprietes.baseUtilisateurs()).where("objectClass").is("person"),
                versAgent);
    }

    private static boolean aChange(Employe employe, AgentAnnuaire agent) {
        return !Objects.equals(employe.getNom(), agent.nom())
                || !Objects.equals(employe.getDepartement(), agent.departement())
                || !Objects.equals(employe.getEmail(), agent.email());
    }
}
