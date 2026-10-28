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
import mr.bcm.gestionprets.dto.MaterielRequest;
import mr.bcm.gestionprets.dto.MaterielResponse;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.service.MaterielService;

@RestController
@RequestMapping("/api/materiels")
@RequiredArgsConstructor
public class MaterielController {

    private final MaterielService materielService;

    @GetMapping
    public List<MaterielResponse> findAll() {
        return materielService.findAll().stream().map(MaterielResponse::of).toList();
    }

    @GetMapping("/{id}")
    public MaterielResponse findById(@PathVariable Long id) {
        return MaterielResponse.of(materielService.findById(id));
    }

    @GetMapping("/disponibles")
    public List<MaterielResponse> findDisponibles() {
        return materielService.findDisponibles().stream().map(MaterielResponse::of).toList();
    }

    @GetMapping("/recherche")
    public List<MaterielResponse> rechercher(@RequestParam String designation) {
        return materielService.rechercherParDesignation(designation).stream().map(MaterielResponse::of).toList();
    }

    @PostMapping
    public ResponseEntity<MaterielResponse> creer(@Valid @RequestBody MaterielRequest request) {
        Materiel cree = materielService.creer(toEntity(request));
        return ResponseEntity.created(URI.create("/api/materiels/" + cree.getId()))
                .body(MaterielResponse.of(cree));
    }

    @PutMapping("/{id}")
    public MaterielResponse modifier(@PathVariable Long id, @Valid @RequestBody MaterielRequest request) {
        return MaterielResponse.of(materielService.modifier(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        materielService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    private Materiel toEntity(MaterielRequest request) {
        return Materiel.builder()
                .numeroInventaire(request.numeroInventaire())
                .designation(request.designation())
                .categorie(Categorie.builder().id(request.categorieId()).build())
                .marque(request.marque())
                .modele(request.modele())
                .etatPhysique(request.etatPhysique())
                .prixAchat(request.prixAchat())
                .dateAchat(request.dateAchat())
                .dureeAmortissementAnnees(request.dureeAmortissementAnnees())
                .fournisseur(request.fournisseur())
                .build();
    }
}
