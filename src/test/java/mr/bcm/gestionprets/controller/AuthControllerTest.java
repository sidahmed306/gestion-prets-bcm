package mr.bcm.gestionprets.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import mr.bcm.gestionprets.annuaire.ConnexionAnnuaireService;
import mr.bcm.gestionprets.entity.Role;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.security.JwtService;
import mr.bcm.gestionprets.security.JwtUtilisateurConverter;
import mr.bcm.gestionprets.security.SecurityConfig;
import mr.bcm.gestionprets.security.SecurityErrorHandler;
import mr.bcm.gestionprets.service.UtilisateurService;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtUtilisateurConverter.class, SecurityErrorHandler.class, JwtService.class })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ConnexionAnnuaireService connexionAnnuaireService;

    private final Utilisateur ahmed = Utilisateur.builder()
            .id(1L).username("ahmed").nomComplet("Ahmed Sidi").role(Role.GESTIONNAIRE).actif(true).build();

    @BeforeEach
    void setUp() {
        when(userDetailsService.loadUserByUsername("ahmed")).thenReturn(User.withUsername("ahmed")
                .password(passwordEncoder.encode("motdepasse123")).roles("GESTIONNAIRE").build());
        when(userDetailsService.loadUserByUsername("inconnu")).thenThrow(new UsernameNotFoundException("inconnu"));
        when(utilisateurService.enregistrerConnexion("ahmed")).thenReturn(ahmed);
        when(utilisateurService.findByUsername("ahmed")).thenReturn(ahmed);
    }

    @Test
    void login_renvoieUnJetonQuandLesIdentifiantsSontBons() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ahmed\",\"motDePasse\":\"motdepasse123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.utilisateur.role").value("GESTIONNAIRE"))
                .andExpect(jsonPath("$.utilisateur.motDePasse").doesNotExist());
    }

    @Test
    void login_renvoie401QuandLeMotDePasseEstFaux() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ahmed\",\"motDePasse\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiant ou mot de passe incorrect"));
    }

    @Test
    void login_renvoieLeMemeMessageQuandLUtilisateurNExistePas() throws Exception {
        // Même message que pour un mauvais mot de passe : on ne révèle pas quels identifiants existent
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inconnu\",\"motDePasse\":\"peuimporte\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiant ou mot de passe incorrect"));
    }

    @Test
    void login_renvoie400QuandLesChampsSontVides() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists());
    }

    @Test
    void me_renvoieLUtilisateurConnecteAvecLeJetonObtenuAuLogin() throws Exception {
        String reponse = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ahmed\",\"motDePasse\":\"motdepasse123\"}"))
                .andReturn().getResponse().getContentAsString();
        String jeton = JsonPath.read(reponse, "$.token");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ahmed"))
                .andExpect(jsonPath("$.nomComplet").value("Ahmed Sidi"));
    }
}
