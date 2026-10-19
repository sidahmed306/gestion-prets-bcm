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
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.dto.CategorieRequest;
import mr.bcm.gestionprets.dto.CategorieResponse;
import mr.bcm.gestionprets.entity.Categorie;
import mr.bcm.gestionprets.service.CategorieService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public List<CategorieResponse> findAll() {
        return categorieService.findAll().stream().map(CategorieResponse::of).toList();
    }

    @GetMapping("/{id}")
    public CategorieResponse findById(@PathVariable Long id) {
        return CategorieResponse.of(categorieService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CategorieResponse> creer(@Valid @RequestBody CategorieRequest request) {
        Categorie cree = categorieService.creer(toEntity(request));
        return ResponseEntity.created(URI.create("/api/categories/" + cree.getId()))
                .body(CategorieResponse.of(cree));
    }

    @PutMapping("/{id}")
    public CategorieResponse modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return CategorieResponse.of(categorieService.modifier(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        categorieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    private Categorie toEntity(CategorieRequest request) {
        return Categorie.builder()
                .code(request.code())
                .libelle(request.libelle())
                .build();
    }
}
