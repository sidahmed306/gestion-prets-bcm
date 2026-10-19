package mr.bcm.gestionprets.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.dto.PretRequest;
import mr.bcm.gestionprets.dto.PretResponse;
import mr.bcm.gestionprets.dto.PretRetourRequest;
import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.service.PretService;

@RestController
@RequestMapping("/api/prets")
@RequiredArgsConstructor
public class PretController {

    private final PretService pretService;

    @GetMapping
    public List<PretResponse> findAll() {
        return pretService.findAll().stream().map(PretResponse::of).toList();
    }

    @GetMapping("/{id}")
    public PretResponse findById(@PathVariable Long id) {
        return PretResponse.of(pretService.findById(id));
    }

    @GetMapping("/employe/{employeId}")
    public List<PretResponse> findByEmploye(@PathVariable Long employeId) {
        return pretService.findByEmploye(employeId).stream().map(PretResponse::of).toList();
    }

    @GetMapping("/materiel/{materielId}")
    public List<PretResponse> findByMateriel(@PathVariable Long materielId) {
        return pretService.findByMateriel(materielId).stream().map(PretResponse::of).toList();
    }

    @GetMapping("/en-cours")
    public List<PretResponse> findEnCours() {
        return pretService.findEnCours().stream().map(PretResponse::of).toList();
    }

    @GetMapping("/en-retard")
    public List<PretResponse> findEnRetard() {
        return pretService.findEnRetard().stream().map(PretResponse::of).toList();
    }

    @PostMapping
    public ResponseEntity<PretResponse> creer(@Valid @RequestBody PretRequest request) {
        Pret cree = pretService.creerPret(
                request.materielId(),
                request.employeId(),
                request.datePret(),
                request.dateRetourPrevue(),
                request.commentaire());
        return ResponseEntity.created(URI.create("/api/prets/" + cree.getId())).body(PretResponse.of(cree));
    }

    @PostMapping("/{id}/retour")
    public PretResponse retourner(@PathVariable Long id, @RequestBody(required = false) PretRetourRequest request) {
        LocalDate dateRetourEffective = request != null ? request.dateRetourEffective() : null;
        return PretResponse.of(pretService.retournerPret(id, dateRetourEffective));
    }
}
