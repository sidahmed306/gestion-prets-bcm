package mr.bcm.gestionprets.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import mr.bcm.gestionprets.dto.MotDePasseResetRequest;
import mr.bcm.gestionprets.dto.UtilisateurCreateRequest;
import mr.bcm.gestionprets.dto.UtilisateurResponse;
import mr.bcm.gestionprets.dto.UtilisateurUpdateRequest;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.service.UtilisateurService;

/** Gestion des comptes : réservée au rôle ADMIN (règle définie dans SecurityConfig). */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public List<UtilisateurResponse> findAll() {
        return utilisateurService.findAll().stream().map(UtilisateurResponse::of).toList();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse findById(@PathVariable Long id) {
        return UtilisateurResponse.of(utilisateurService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurCreateRequest request) {
        Utilisateur cree = utilisateurService.creer(request.username(), request.motDePasse(),
                request.nomComplet(), request.role());
        return ResponseEntity.created(URI.create("/api/utilisateurs/" + cree.getId()))
                .body(UtilisateurResponse.of(cree));
    }

    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurUpdateRequest request,
            Authentication authentication) {
        return UtilisateurResponse.of(utilisateurService.modifier(id, request.nomComplet(), request.role(),
                request.actif(), authentication.getName()));
    }

    @PutMapping("/{id}/mot-de-passe")
    public ResponseEntity<Void> reinitialiserMotDePasse(@PathVariable Long id,
            @Valid @RequestBody MotDePasseResetRequest request) {
        utilisateurService.reinitialiserMotDePasse(id, request.nouveauMotDePasse());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication authentication) {
        utilisateurService.supprimer(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
