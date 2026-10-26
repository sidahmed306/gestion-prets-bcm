package mr.bcm.gestionprets.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.repository.UtilisateurRepository;

/**
 * Le "pont" entre notre table utilisateurs et Spring Security :
 * Spring appelle cette classe pour savoir qui est un utilisateur, son mot de passe haché,
 * son rôle et si son compte est actif.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + username));
        return User.withUsername(utilisateur.getUsername())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().name()) // devient l'autorité "ROLE_ADMIN", "ROLE_GESTIONNAIRE"...
                .disabled(!utilisateur.isActif())
                .build();
    }
}
