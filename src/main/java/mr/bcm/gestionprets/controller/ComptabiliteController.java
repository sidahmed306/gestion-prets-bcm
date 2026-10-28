package mr.bcm.gestionprets.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.comptabilite.ComptabiliteService;
import mr.bcm.gestionprets.dto.PlanAmortissementResponse;
import mr.bcm.gestionprets.dto.SyntheseComptableResponse;

/** Lecture seule : accessible à tous les rôles connectés (règle GET de SecurityConfig). */
@RestController
@RequestMapping("/api/comptabilite")
@RequiredArgsConstructor
public class ComptabiliteController {

    private final ComptabiliteService comptabiliteService;

    /** Totaux du parc. Par défaut à aujourd'hui ; ?date=2025-12-31 pour la valeur à une autre date (ex : clôture). */
    @GetMapping("/synthese")
    public SyntheseComptableResponse synthese(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return comptabiliteService.synthese(date != null ? date : LocalDate.now());
    }

    @GetMapping("/materiels/{id}/plan")
    public PlanAmortissementResponse planAmortissement(@PathVariable Long id) {
        return comptabiliteService.planAmortissement(id);
    }
}
