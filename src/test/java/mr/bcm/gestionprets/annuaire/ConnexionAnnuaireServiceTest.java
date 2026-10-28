package mr.bcm.gestionprets.annuaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import mr.bcm.gestionprets.annuaire.AnnuaireProperties.RegleAcces;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.SourceCompte;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.exception.AccesRefuseException;
import mr.bcm.gestionprets.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class ConnexionAnnuaireServiceTest {

    private static final String DSI = "Direction des Systèmes d'Information";
    private static final List<String> DIRECTEURS = List.of("Directeur", "Directrice");

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private ConnexionAnnuaireService service;

    @BeforeEach
    void setUp() {
        // Les mêmes règles que dans application.properties
        List<RegleAcces> regles = List.of(
                new RegleAcces(Role.ADMIN, DSI, DIRECTEURS),
                new RegleAcces(Role.GESTIONNAIRE, DSI, null),
                new RegleAcces(Role.LECTEUR, null, DIRECTEURS));
        AnnuaireProperties proprietes = new AnnuaireProperties(true, "ou=personnes,dc=bcm,dc=mr", "(uid={0})",
                "employeeNumber", "departmentNumber", "title", regles);
        service = new ConnexionAnnuaireService(utilisateurRepository, new BCryptPasswordEncoder(), proprietes);
    }

    private AgentAnnuaire agent(String identifiant, String departement, String poste) {
        return new AgentAnnuaire(identifiant, "Nom " + identifiant, "MAT999", departement, poste, null);
    }

    @Test
    void leDirecteurDeLaDsiEstAdministrateur() {
        assertThat(service.roleDeLAgent(agent("a", DSI, "Directeur des Systèmes d'Information"))).isEqualTo(Role.ADMIN);
    }

    @Test
    void lesResponsablesDeLaDsiSontGestionnaires() {
        assertThat(service.roleDeLAgent(agent("a", DSI, "Responsable du développement"))).isEqualTo(Role.GESTIONNAIRE);
        assertThat(service.roleDeLAgent(agent("b", DSI, "Responsable des bases de données"))).isEqualTo(Role.GESTIONNAIRE);
        assertThat(service.roleDeLAgent(agent("c", DSI, "Technicienne support informatique"))).isEqualTo(Role.GESTIONNAIRE);
    }

    @Test
    void lesDirecteursEtDirectricesDesAutresDirectionsSontLecteurs() {
        assertThat(service.roleDeLAgent(agent("a", "Direction des Études", "Directeur des Études"))).isEqualTo(Role.LECTEUR);
        assertThat(service.roleDeLAgent(agent("b", "Direction de la Comptabilité", "Directrice de la Comptabilité")))
                .isEqualTo(Role.LECTEUR);
    }

    @Test
    void lesAutresAgentsNOntPasAcces() {
        assertThatThrownBy(() -> service.roleDeLAgent(agent("a", "Direction de la Comptabilité", "Comptable")))
                .isInstanceOf(AccesRefuseException.class)
                .hasMessageContaining("Comptable");
    }

    @Test
    void premiereConnexion_creeLeCompteAvecLaDirectionEtLePoste() {
        when(utilisateurRepository.findByUsername("sidi.cheikh")).thenReturn(Optional.empty());
        when(utilisateurRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur cree = service.connecter(agent("sidi.cheikh", DSI, "Responsable du développement"));

        assertThat(cree.getSource()).isEqualTo(SourceCompte.ANNUAIRE);
        assertThat(cree.getRole()).isEqualTo(Role.GESTIONNAIRE);
        assertThat(cree.getDepartement()).isEqualTo(DSI);
        assertThat(cree.getPoste()).isEqualTo("Responsable du développement");
        assertThat(cree.isActif()).isTrue();
        assertThat(cree.getMotDePasse()).startsWith("$2a$"); // mot de passe aléatoire haché, jamais utilisable
    }

    @Test
    void promotion_leNouveauPosteDonneLeNouveauRoleALaConnexionSuivante() {
        Utilisateur existant = Utilisateur.builder().id(5L).username("sidi.cheikh").role(Role.GESTIONNAIRE)
                .actif(true).source(SourceCompte.ANNUAIRE).motDePasse("x").build();
        when(utilisateurRepository.findByUsername("sidi.cheikh")).thenReturn(Optional.of(existant));
        when(utilisateurRepository.save(existant)).thenReturn(existant);

        service.connecter(agent("sidi.cheikh", DSI, "Directeur des Systèmes d'Information"));

        assertThat(existant.getRole()).isEqualTo(Role.ADMIN);
        assertThat(existant.getPoste()).isEqualTo("Directeur des Systèmes d'Information");
    }

    @Test
    void compteDesactiveParLAdmin_refuseMemeSiLAnnuaireAccepte() {
        Utilisateur desactive = Utilisateur.builder().username("sidi.cheikh").role(Role.GESTIONNAIRE)
                .actif(false).source(SourceCompte.ANNUAIRE).motDePasse("x").build();
        when(utilisateurRepository.findByUsername("sidi.cheikh")).thenReturn(Optional.of(desactive));

        assertThatThrownBy(() -> service.connecter(agent("sidi.cheikh", DSI, "Responsable du développement")))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void compteLocalDuMemeNom_neDoitPasEtreRepris() {
        Utilisateur local = Utilisateur.builder().username("ahmed.sidi").role(Role.LECTEUR)
                .actif(true).source(SourceCompte.LOCAL).motDePasse("x").build();
        when(utilisateurRepository.findByUsername("ahmed.sidi")).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> service.connecter(agent("ahmed.sidi", DSI, "Directeur des Systèmes d'Information")))
                .isInstanceOf(AccesRefuseException.class);
        verify(utilisateurRepository, never()).save(any());
    }
}
