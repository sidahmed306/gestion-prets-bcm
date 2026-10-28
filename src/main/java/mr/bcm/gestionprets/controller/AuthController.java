package mr.bcm.gestionprets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.annuaire.AgentAnnuaire;
import mr.bcm.gestionprets.annuaire.ConnexionAnnuaireService;
import mr.bcm.gestionprets.dto.ChangementMotDePasseRequest;
import mr.bcm.gestionprets.dto.LoginRequest;
import mr.bcm.gestionprets.dto.LoginResponse;
import mr.bcm.gestionprets.dto.UtilisateurResponse;
import mr.bcm.gestionprets.entity.Utilisateur;
import mr.bcm.gestionprets.security.JwtService;
import mr.bcm.gestionprets.service.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final ConnexionAnnuaireService connexionAnnuaireService;

    /**
     * Connexion : vérifie l'identifiant et le mot de passe (compte local OU annuaire), puis renvoie un jeton JWT.
     * En cas d'échec, Spring lève BadCredentialsException ou DisabledException (gérées en 401).
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.motDePasse()));

        // Accepté par l'annuaire → on crée / met à jour le compte à partir de la fiche de l'agent
        // Accepté comme compte local → on note simplement la date de connexion
        Utilisateur utilisateur = authentication.getPrincipal() instanceof AgentAnnuaire agent
                ? connexionAnnuaireService.connecter(agent)
                : utilisateurService.enregistrerConnexion(authentication.getName());

        JwtService.Jeton jeton = jwtService.genererJeton(utilisateur);
        return new LoginResponse(jeton.valeur(), jeton.expiration(), UtilisateurResponse.of(utilisateur));
    }

    /** Qui suis-je ? Spring injecte l'utilisateur connecté (lu depuis le jeton) dans "authentication". */
    @GetMapping("/me")
    public UtilisateurResponse moi(Authentication authentication) {
        return UtilisateurResponse.of(utilisateurService.findByUsername(authentication.getName()));
    }

    @PutMapping("/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(Authentication authentication,
            @Valid @RequestBody ChangementMotDePasseRequest request) {
        utilisateurService.changerMonMotDePasse(authentication.getName(),
                request.ancienMotDePasse(), request.nouveauMotDePasse());
        return ResponseEntity.noContent().build();
    }
}
