package mr.bcm.gestionprets.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.SourceCompte;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public Utilisateur findByUsername(String username) {
        return utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + username));
    }

    @Transactional
    public Utilisateur creer(String username, String motDePasse, String nomComplet, Role role) {
        if (utilisateurRepository.existsByUsername(username)) {
            throw new BusinessRuleException("L'identifiant " + username + " est déjà utilisé");
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .username(username)
                .motDePasse(passwordEncoder.encode(motDePasse))
                .nomComplet(nomComplet)
                .role(role)
                .actif(true)
                .build();
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Un admin ne peut ni retirer son propre rôle ADMIN ni désactiver son propre compte :
     * cela garantit qu'il reste toujours au moins un administrateur actif.
     */
    @Transactional
    public Utilisateur modifier(Long id, String nomComplet, Role role, boolean actif, String usernameConnecte) {
        Utilisateur utilisateur = findById(id);
        if (utilisateur.getUsername().equals(usernameConnecte)) {
            if (role != Role.ADMIN) {
                throw new BusinessRuleException("Vous ne pouvez pas retirer votre propre rôle administrateur");
            }
            if (!actif) {
                throw new BusinessRuleException("Vous ne pouvez pas désactiver votre propre compte");
            }
        }
        if (utilisateur.getSource() == SourceCompte.ANNUAIRE && utilisateur.getRole() != role) {
            throw new BusinessRuleException("Le rôle d'un compte de l'annuaire se gère dans l'annuaire "
                    + "(en ajoutant la personne au bon groupe), pas dans l'application");
        }
        utilisateur.setNomComplet(nomComplet);
        utilisateur.setRole(role);
        utilisateur.setActif(actif);
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        Utilisateur utilisateur = findById(id);
        refuserSiAnnuaire(utilisateur);
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void changerMonMotDePasse(String username, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = findByUsername(username);
        refuserSiAnnuaire(utilisateur);
        if (!passwordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new BusinessRuleException("L'ancien mot de passe est incorrect");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void supprimer(Long id, String usernameConnecte) {
        Utilisateur utilisateur = findById(id);
        if (utilisateur.getUsername().equals(usernameConnecte)) {
            throw new BusinessRuleException("Vous ne pouvez pas supprimer votre propre compte");
        }
        utilisateurRepository.delete(utilisateur);
    }

    /** Le mot de passe d'un compte de l'annuaire est celui de l'annuaire (compte Windows) : on n'y touche pas ici. */
    private void refuserSiAnnuaire(Utilisateur utilisateur) {
        if (utilisateur.getSource() == SourceCompte.ANNUAIRE) {
            throw new BusinessRuleException("Le mot de passe d'un compte de l'annuaire se change dans l'annuaire "
                    + "(mot de passe Windows), pas dans l'application");
        }
    }

    @Transactional
    public Utilisateur enregistrerConnexion(String username) {
        Utilisateur utilisateur = findByUsername(username);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        return utilisateurRepository.save(utilisateur);
    }
}
