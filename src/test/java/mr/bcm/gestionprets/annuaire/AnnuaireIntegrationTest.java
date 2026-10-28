package mr.bcm.gestionprets.annuaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import mr.bcm.gestionprets.dto.SynchronisationResponse;
import mr.bcm.gestionprets.repository.EmployeRepository;

/**
 * Test de bout en bout avec le VRAI serveur LDAP intégré (fichier annuaire-bcm.ldif),
 * les VRAIES règles d'accès de application.properties et la vraie base PostgreSQL.
 * {@code @Transactional} : tout ce que le test écrit en base est annulé à la fin.
 */
@SpringBootTest(properties = "spring.ldap.embedded.port=0")
@AutoConfigureMockMvc
@Transactional
class AnnuaireIntegrationTest {

    private static final String MOT_DE_PASSE_ANNUAIRE = "Bcm@2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SynchronisationEmployesService synchronisationEmployesService;

    @Autowired
    private EmployeRepository employeRepository;

    private ResultActions connexion(String identifiant, String motDePasse) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + identifiant + "\",\"motDePasse\":\"" + motDePasse + "\"}"));
    }

    @Test
    void directeurDeLaDsi_estAdministrateur() throws Exception {
        connexion("ahmed.sidi", MOT_DE_PASSE_ANNUAIRE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateur.role").value("ADMIN"))
                .andExpect(jsonPath("$.utilisateur.source").value("ANNUAIRE"))
                .andExpect(jsonPath("$.utilisateur.poste").value("Directeur des Systèmes d'Information"));
    }

    @Test
    void responsablesDeLaDsi_sontGestionnaires() throws Exception {
        connexion("sidi.cheikh", MOT_DE_PASSE_ANNUAIRE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateur.role").value("GESTIONNAIRE"))
                .andExpect(jsonPath("$.utilisateur.poste").value("Responsable du développement"));
        connexion("mariem.ahmed", MOT_DE_PASSE_ANNUAIRE)
                .andExpect(jsonPath("$.utilisateur.role").value("GESTIONNAIRE"));
    }

    @Test
    void directriceDUneAutreDirection_estLectrice_etNePeutQueConsulter() throws Exception {
        String reponse = connexion("fatma.salem", MOT_DE_PASSE_ANNUAIRE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateur.role").value("LECTEUR"))
                .andExpect(jsonPath("$.utilisateur.departement").value("Direction de la Comptabilité"))
                .andReturn().getResponse().getContentAsString();
        String jeton = JsonPath.read(reponse, "$.token");

        // Le jeton obtenu via l'annuaire fonctionne ensuite comme un jeton de compte local
        mockMvc.perform(get("/api/employes").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/employes").header("Authorization", "Bearer " + jeton)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"matricule\":\"X\",\"nom\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void agentSansPosteDeDirection_horsDsi_accesRefuse() throws Exception {
        connexion("oumar.kane", MOT_DE_PASSE_ANNUAIRE)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Comptable")));
    }

    @Test
    void mauvaisMotDePasse_401() throws Exception {
        connexion("ahmed.sidi", "mauvais")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiant ou mot de passe incorrect"));
    }

    @Test
    void lireLAnnuaire_trouveLes12AgentsDeToutesLesDirections() {
        assertThat(synchronisationEmployesService.lireAgents())
                .hasSize(12)
                .anySatisfy(agent -> {
                    assertThat(agent.matricule()).isEqualTo("MAT006");
                    assertThat(agent.poste()).isEqualTo("Responsable des bases de données");
                    assertThat(agent.departement()).isEqualTo("Direction des Systèmes d'Information");
                });
    }

    @Test
    void synchronisation_creeOuMetAJourChaqueAgentParSonMatricule() {
        SynchronisationResponse bilan = synchronisationEmployesService.synchroniser();

        assertThat(bilan.lus()).isEqualTo(12);
        assertThat(bilan.crees() + bilan.misAJour() + bilan.inchanges()).isEqualTo(12);
        assertThat(employeRepository.findByMatricule("MAT009")).hasValueSatisfying(employe -> {
            assertThat(employe.getNom()).isEqualTo("Cheikh Brahim");
            assertThat(employe.getDepartement()).isEqualTo("Direction des Études");
        });

        // 2e passage : plus rien à faire
        assertThat(synchronisationEmployesService.synchroniser().inchanges()).isEqualTo(12);
    }
}
