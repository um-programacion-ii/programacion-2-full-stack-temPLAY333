package um.prog2.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import um.prog2.dto.autenticacion.LoginRequestDTO;
import um.prog2.dto.autenticacion.LoginResponseDTO;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Servicio que se encarga de obtener y refrescar el JWT contra la cátedra
 * de forma automática, similar a lo que hoy hacen los scripts PowerShell.
 *
 * - Al iniciar la app, si hay credenciales configuradas, intenta loguearse.
 * - Mantiene el token en memoria.
 * - Permite refrescar manualmente y exponer el token actual a quien lo necesite.
 *
 * Este servicio NO toca archivos .env ni ejecuta scripts; solo usa HTTP.
 */
@Service
public class AuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);

    private final WebClient webClientRaw; // WebClient sin filtro de Authorization

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    @Value("${app.catedra.api-base:/api}")
    private String apiBase;

    @Value("${app.catedra.username:}")
    private String username;

    @Value("${app.catedra.password:}")
    private String password;

    /**
     * Tiempo aproximado de vida del token (si no hay información real, usamos un valor conservador)
     */
    @Value("${app.catedra.token-ttl-seconds:3600}")
    private long tokenTtlSeconds;

    private final AtomicReference<String> currentToken = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastRefresh = new AtomicReference<>(null);

    public AuthTokenService(WebClient.Builder builder) {
        // Construimos un WebClient "crudo" sin baseUrl preconfigurada ni filtros
        this.webClientRaw = builder.build();
    }

    @PostConstruct
    public void init() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("AuthTokenService: no hay credenciales configuradas (app.catedra.username / app.catedra.password); el token deberá configurarse por otras vías o solo se podrán usar endpoints públicos.");
            return;
        }

        try {
            refreshTokenIfNeeded(true);
        } catch (Exception ex) {
            log.warn("AuthTokenService: no se pudo obtener token al inicio: {}", ex.getMessage());
        }
    }

    /**
     * Devuelve el token actual si está presente y no parece vencido.
     * Si está ausente o viejo, intenta refrescarlo.
     */
    public Optional<String> getCurrentToken() {
        try {
            refreshTokenIfNeeded(false);
        } catch (Exception ex) {
            log.warn("AuthTokenService: error al refrescar token bajo demanda: {}", ex.getMessage());
        }
        return Optional.ofNullable(currentToken.get());
    }

    /**
     * Fuerza un refresh inmediato (útil para endpoints administrativos si quisieras exponerse luego).
     */
    public Optional<String> forceRefresh() {
        try {
            refreshToken(true);
        } catch (Exception ex) {
            log.warn("AuthTokenService: error al forzar refresh: {}", ex.getMessage());
        }
        return Optional.ofNullable(currentToken.get());
    }

    private void refreshTokenIfNeeded(boolean onStartup) {
        Instant last = lastRefresh.get();
        Instant now = Instant.now();

        // Si nunca se refrescó o ya pasó más de la mitad del TTL, intentamos renovar
        if (last == null || Duration.between(last, now).getSeconds() > (tokenTtlSeconds / 2)) {
            refreshToken(onStartup);
        }
    }

    private void refreshToken(boolean logErrorsStrongly) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            if (logErrorsStrongly) {
                log.warn("AuthTokenService: no hay credenciales configuradas, no se puede refrescar token.");
            }
            return;
        }

        String base = catedraBaseUrl;
        if (apiBase != null && !apiBase.isBlank()) {
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            if (!apiBase.startsWith("/")) {
                base = base + "/" + apiBase;
            } else {
                base = base + apiBase;
            }
        }

        String url = base + "/authenticate";

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        loginRequest.setRememberMe(false);

        log.info("AuthTokenService: solicitando nuevo token a {} para usuario {}", url, username);

        try {
            LoginResponseDTO response = webClientRaw.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponseDTO.class)
                .block(Duration.ofSeconds(10));

            if (response == null || response.getIdToken() == null || response.getIdToken().isBlank()) {
                throw new IllegalStateException("La respuesta de authenticate no contiene id_token");
            }

            currentToken.set(response.getIdToken());
            lastRefresh.set(Instant.now());

            log.info("AuthTokenService: token obtenido correctamente (longitud {} caracteres)", response.getIdToken().length());
        } catch (Exception ex) {
            if (logErrorsStrongly) {
                log.error("AuthTokenService: error al obtener token: {}", ex.getMessage());
            } else {
                log.warn("AuthTokenService: no se pudo refrescar token: {}", ex.getMessage());
            }
        }
    }
}

