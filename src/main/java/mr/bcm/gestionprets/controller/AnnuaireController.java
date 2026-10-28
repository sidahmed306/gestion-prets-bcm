package mr.bcm.gestionprets.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.annuaire.SynchronisationEmployesService;
import mr.bcm.gestionprets.dto.SynchronisationResponse;

/** Réservé au rôle ADMIN (règle "/api/annuaire/**" dans SecurityConfig). */
@RestController
@RequestMapping("/api/annuaire")
@RequiredArgsConstructor
public class AnnuaireController {

    private final SynchronisationEmployesService synchronisationEmployesService;

    /** Lance tout de suite la synchronisation, sans attendre celle de la nuit. */
    @PostMapping("/synchroniser-employes")
    public SynchronisationResponse synchroniserEmployes() {
        return synchronisationEmployesService.synchroniser();
    }
}
