package mr.bcm.gestionprets.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import mr.bcm.gestionprets.dto.PretRequest;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.exception.ResourceNotFoundException;
import mr.bcm.gestionprets.service.PretService;

@WebMvcTest(PretController.class)
class PretControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PretService pretService;

    private Pret unPret() {
        Categorie categorie = Categorie.builder().id(1L).code("ORDI").libelle("Ordinateur portable").build();
        Materiel materiel = Materiel.builder().id(1L).numeroInventaire("INV001").categorie(categorie).build();
        Employe employe = Employe.builder().id(1L).matricule("MAT001").nom("Ahmed").build();
        return Pret.builder()
                .id(1L)
                .materiel(materiel)
                .employe(employe)
                .datePret(LocalDate.of(2026, 10, 1))
                .dateRetourPrevue(LocalDate.of(2026, 10, 10))
                .statut(StatutPret.EN_COURS)
                .build();
    }

    @Test
    void creer_renvoie201QuandLaDemandeEstValide() throws Exception {
        when(pretService.creerPret(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class), isNull()))
                .thenReturn(unPret());

        PretRequest request = new PretRequest(1L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), null);

        mockMvc.perform(post("/api/prets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.materiel.numeroInventaire").value("INV001"));
    }

    @Test
    void creer_renvoie400QuandMaterielIdEstManquant() throws Exception {
        String corpsInvalide = """
                {"employeId":1,"datePret":"2026-10-01","dateRetourPrevue":"2026-10-10"}
                """;

        mockMvc.perform(post("/api/prets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsInvalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.materielId").exists());
    }

    @Test
    void creer_renvoie409QuandLeMaterielADejaUnPretEnCours() throws Exception {
        when(pretService.creerPret(any(), any(), any(), any(), any()))
                .thenThrow(new BusinessRuleException("Ce matériel a déjà un prêt en cours (prêt #1)"));

        PretRequest request = new PretRequest(1L, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), null);

        mockMvc.perform(post("/api/prets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ce matériel a déjà un prêt en cours (prêt #1)"));
    }

    @Test
    void findById_renvoie404QuandLePretEstInconnu() throws Exception {
        when(pretService.findById(99L)).thenThrow(new ResourceNotFoundException("Prêt introuvable : 99"));

        mockMvc.perform(get("/api/prets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void retourner_renvoie200EtLePretMisAJour() throws Exception {
        Pret pretRetourne = unPret();
        pretRetourne.setStatut(StatutPret.RETOURNE);
        pretRetourne.setDateRetourEffective(LocalDate.of(2026, 10, 7));
        when(pretService.retournerPret(eq(1L), any())).thenReturn(pretRetourne);

        mockMvc.perform(post("/api/prets/1/retour")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateRetourEffective\":\"2026-10-07\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RETOURNE"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void retourner_renvoie409QuandLePretEstDejaRetourne() throws Exception {
        when(pretService.retournerPret(eq(1L), any()))
                .thenThrow(new BusinessRuleException("Le prêt #1 a déjà été retourné"));

        mockMvc.perform(post("/api/prets/1/retour"))
                .andExpect(status().isConflict());
    }

    @Test
    void findAll_renvoie500GeneriqueSansExposerLerreurTechniqueQuandUneExceptionInattendueSurvient() throws Exception {
        when(pretService.findAll()).thenThrow(new RuntimeException("Connexion base de données perdue"));

        mockMvc.perform(get("/api/prets"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Une erreur inattendue est survenue"));
    }

    @Test
    void retourner_renvoie405QuandAppeleAvecLancienVerbePatch() throws Exception {
        mockMvc.perform(patch("/api/prets/1/retour"))
                .andExpect(status().isMethodNotAllowed());
    }
}
