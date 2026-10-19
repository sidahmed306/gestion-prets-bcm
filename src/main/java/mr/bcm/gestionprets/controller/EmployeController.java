package mr.bcm.gestionprets.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.dto.EmployeRequest;
import mr.bcm.gestionprets.dto.EmployeResponse;
import mr.bcm.gestionprets.entity.Employe;
import mr.bcm.gestionprets.service.EmployeService;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @GetMapping
    public List<EmployeResponse> findAll() {
        return employeService.findAll().stream().map(EmployeResponse::of).toList();
    }

    @GetMapping("/{id}")
    public EmployeResponse findById(@PathVariable Long id) {
        return EmployeResponse.of(employeService.findById(id));
    }

    @GetMapping("/recherche")
    public List<EmployeResponse> rechercher(@RequestParam String nom) {
        return employeService.rechercherParNom(nom).stream().map(EmployeResponse::of).toList();
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> creer(@Valid @RequestBody EmployeRequest request) {
        Employe cree = employeService.creer(toEntity(request));
        return ResponseEntity.created(URI.create("/api/employes/" + cree.getId()))
                .body(EmployeResponse.of(cree));
    }

    @PutMapping("/{id}")
    public EmployeResponse modifier(@PathVariable Long id, @Valid @RequestBody EmployeRequest request) {
        return EmployeResponse.of(employeService.modifier(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    private Employe toEntity(EmployeRequest request) {
        return Employe.builder()
                .matricule(request.matricule())
                .nom(request.nom())
                .departement(request.departement())
                .email(request.email())
                .build();
    }
}
