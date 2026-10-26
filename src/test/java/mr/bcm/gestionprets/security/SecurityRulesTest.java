package mr.bcm.gestionprets.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import mr.bcm.gestionprets.controller.EmployeController;
import mr.bcm.gestionprets.controller.UtilisateurController;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.service.EmployeService;
import mr.bcm.gestionprets.service.UtilisateurService;

/** Vérifie qui a le droit d'appeler quoi (règles de SecurityConfig). */
@WebMvcTest({ EmployeController.class, UtilisateurController.class })
@Import({ SecurityConfig.class, JwtUtilisateurConverter.class, SecurityErrorHandler.class, JwtService.class })
class SecurityRulesTest {

    private static final String NOUVEL_EMPLOYE = """
            {"matricule":"MAT010","nom":"Test"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private EmployeService employeService;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));
    }

    @Test
    void jetonFalsifie_renvoie401() throws Exception {
        mockMvc.perform(get("/api/employes").header("Authorization", "Bearer faux.jeton.invalide"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Session expirée ou jeton invalide, veuillez vous reconnecter"));
    }

    @Test
    @WithMockUser(roles = "LECTEUR")
    void lecteur_peutConsulter() throws Exception {
        when(employeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/employes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "LECTEUR")
    void lecteur_nePeutPasCreer_renvoie403() throws Exception {
        mockMvc.perform(post("/api/employes").contentType(MediaType.APPLICATION_JSON).content(NOUVEL_EMPLOYE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Vous n'avez pas les droits pour effectuer cette action"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaire_peutCreer() throws Exception {
        when(employeService.creer(any())).thenReturn(Employe.builder().id(10L).matricule("MAT010").nom("Test").build());

        mockMvc.perform(post("/api/employes").contentType(MediaType.APPLICATION_JSON).content(NOUVEL_EMPLOYE))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaire_nePeutPasGererLesUtilisateurs_renvoie403() throws Exception {
        mockMvc.perform(get("/api/utilisateurs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_peutGererLesUtilisateurs() throws Exception {
        when(utilisateurService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/utilisateurs")).andExpect(status().isOk());
    }

    @Test
    void vraiJeton_estAccepte() throws Exception {
        when(userDetailsService.loadUserByUsername("lecteur"))
                .thenReturn(User.withUsername("lecteur").password("x").roles("LECTEUR").build());
        when(employeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/employes").header("Authorization", "Bearer " + jetonPour("lecteur", Role.LECTEUR)))
                .andExpect(status().isOk());
    }

    @Test
    void vraiJetonDUnCompteDesactive_renvoie401() throws Exception {
        when(userDetailsService.loadUserByUsername("ancien"))
                .thenReturn(User.withUsername("ancien").password("x").roles("GESTIONNAIRE").disabled(true).build());

        mockMvc.perform(get("/api/employes").header("Authorization", "Bearer " + jetonPour("ancien", Role.GESTIONNAIRE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Ce compte est désactivé"));
    }

    @Test
    void roleLuEnBase_etNonDansLeJeton() throws Exception {
        // Le jeton a été émis quand l'utilisateur était GESTIONNAIRE, mais l'admin l'a rétrogradé en LECTEUR depuis
        when(userDetailsService.loadUserByUsername("retrograde"))
                .thenReturn(User.withUsername("retrograde").password("x").roles("LECTEUR").build());

        mockMvc.perform(post("/api/employes")
                        .header("Authorization", "Bearer " + jetonPour("retrograde", Role.GESTIONNAIRE))
                        .contentType(MediaType.APPLICATION_JSON).content(NOUVEL_EMPLOYE))
                .andExpect(status().isForbidden());
    }

    private String jetonPour(String username, Role role) {
        return jwtService.genererJeton(Utilisateur.builder().username(username).role(role).build()).valeur();
    }
}
