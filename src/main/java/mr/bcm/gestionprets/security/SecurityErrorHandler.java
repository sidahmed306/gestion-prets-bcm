package mr.bcm.gestionprets.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.exception.ErrorResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Les erreurs de sécurité arrivent AVANT les controllers, donc le GlobalExceptionHandler ne les voit pas.
 * Cette classe les renvoie dans le même format JSON que les autres erreurs de l'API.
 * <ul>
 *   <li>401 Unauthorized : pas connecté, jeton absent, invalide ou expiré</li>
 *   <li>403 Forbidden : connecté, mais le rôle ne donne pas le droit</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String message;
        if (exception instanceof DisabledException) {
            message = "Ce compte est désactivé";
        } else if (exception instanceof InvalidBearerTokenException) {
            message = "Session expirée ou jeton invalide, veuillez vous reconnecter";
        } else {
            message = "Authentification requise";
        }
        ecrire(response, HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        ecrire(response, HttpStatus.FORBIDDEN, "Forbidden", "Vous n'avez pas les droits pour effectuer cette action");
    }

    private void ecrire(HttpServletResponse response, HttpStatus statut, String erreur, String message)
            throws IOException {
        response.setStatus(statut.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(statut.value(), erreur, message));
    }
}
