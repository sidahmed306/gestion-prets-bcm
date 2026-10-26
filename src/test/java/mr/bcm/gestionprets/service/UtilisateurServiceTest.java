package mr.bcm.gestionprets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurService(utilisateurRepository, passwordEncoder);
    }

    private Utilisateur admin() {
        return Utilisateur.builder().id(1L).username("admin").nomComplet("Admin").role(Role.ADMIN).actif(true)
                .motDePasse(passwordEncoder.encode("ancien-mdp")).build();
    }

    @Test
    void creer_hacheLeMotDePasse() {
        when(utilisateurRepository.existsByUsername("fatma")).thenReturn(false);
        when(utilisateurRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur cree = utilisateurService.creer("fatma", "motdepasse123", "Fatma", Role.GESTIONNAIRE);

        assertThat(cree.getMotDePasse()).isNotEqualTo("motdepasse123");
        assertThat(passwordEncoder.matches("motdepasse123", cree.getMotDePasse())).isTrue();
        assertThat(cree.isActif()).isTrue();
    }

    @Test
    void creer_rejetteUnIdentifiantDejaUtilise() {
        when(utilisateurRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.creer("admin", "motdepasse123", "X", Role.LECTEUR))
                .isInstanceOf(BusinessRuleException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void modifier_unAdminNePeutPasRetirerSonPropreRoleAdmin() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> utilisateurService.modifier(1L, "Admin", Role.LECTEUR, true, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("rôle administrateur");
    }

    @Test
    void modifier_unAdminNePeutPasDesactiverSonPropreCompte() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> utilisateurService.modifier(1L, "Admin", Role.ADMIN, false, "admin"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void modifier_unAdminPeutDesactiverUnAutreCompte() {
        Utilisateur autre = Utilisateur.builder().id(2L).username("ali").role(Role.GESTIONNAIRE).actif(true).build();
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(autre));
        when(utilisateurRepository.save(autre)).thenReturn(autre);

        Utilisateur modifie = utilisateurService.modifier(2L, "Ali", Role.GESTIONNAIRE, false, "admin");

        assertThat(modifie.isActif()).isFalse();
    }

    @Test
    void supprimer_unUtilisateurNePeutPasSeSupprimerLuiMeme() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> utilisateurService.supprimer(1L, "admin"))
                .isInstanceOf(BusinessRuleException.class);

        verify(utilisateurRepository, never()).delete(any());
    }

    @Test
    void changerMonMotDePasse_rejetteSiLAncienEstFaux() {
        when(utilisateurRepository.findByUsername("admin")).thenReturn(Optional.of(admin()));

        assertThatThrownBy(() -> utilisateurService.changerMonMotDePasse("admin", "faux", "nouveau-mdp"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("L'ancien mot de passe est incorrect");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerMonMotDePasse_enregistreLeNouveauMotDePasseHache() {
        Utilisateur admin = admin();
        when(utilisateurRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        utilisateurService.changerMonMotDePasse("admin", "ancien-mdp", "nouveau-mdp");

        assertThat(passwordEncoder.matches("nouveau-mdp", admin.getMotDePasse())).isTrue();
        verify(utilisateurRepository).save(admin);
    }
}
