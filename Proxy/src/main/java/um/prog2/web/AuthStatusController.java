package um.prog2.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.prog2.service.AuthTokenService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador de administración para verificar el estado del token JWT
 * y forzar su renovación si es necesario.
 */
@RestController
@RequestMapping("/actuator/auth")
public class AuthStatusController {

    private final AuthTokenService authTokenService;

    @Autowired
    public AuthStatusController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    /**
     * Verifica el estado actual del token JWT.
     *
     * GET http://localhost:8080/actuator/auth/status
     *
     * Respuesta:
     * {
     *   "hasToken": true,
     *   "tokenLength": 206,
     *   "tokenPreview": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZW1wbGF5MzMz...",
     *   "message": "Token disponible"
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTokenStatus() {
        Map<String, Object> status = new HashMap<>();
        Optional<String> token = authTokenService.getCurrentToken();

        if (token.isPresent()) {
            String tokenValue = token.get();
            status.put("hasToken", true);
            status.put("tokenLength", tokenValue.length());

            // Mostrar solo los primeros 50 caracteres por seguridad
            String preview = tokenValue.substring(0, Math.min(50, tokenValue.length()));
            status.put("tokenPreview", preview + "...");
            status.put("message", "Token disponible y válido");

            return ResponseEntity.ok(status);
        } else {
            status.put("hasToken", false);
            status.put("tokenLength", 0);
            status.put("tokenPreview", null);
            status.put("message", "No hay token disponible. Verifica las credenciales.");

            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Fuerza la renovación inmediata del token JWT.
     *
     * POST http://localhost:8080/actuator/auth/refresh
     *
     * Respuesta:
     * {
     *   "success": true,
     *   "tokenLength": 206,
     *   "tokenPreview": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZW1wbGF5MzMz...",
     *   "message": "Token renovado exitosamente"
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken() {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<String> token = authTokenService.forceRefresh();

            if (token.isPresent()) {
                String tokenValue = token.get();
                result.put("success", true);
                result.put("tokenLength", tokenValue.length());

                String preview = tokenValue.substring(0, Math.min(50, tokenValue.length()));
                result.put("tokenPreview", preview + "...");
                result.put("message", "Token renovado exitosamente");

                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "No se pudo obtener el token. Verifica logs para más detalles.");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "Error al renovar token: " + ex.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}

