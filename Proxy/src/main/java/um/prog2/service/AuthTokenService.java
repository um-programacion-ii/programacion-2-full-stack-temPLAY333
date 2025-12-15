package um.prog2.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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
 *
 * NOTA: Se deshabilita en modo mock usando ConditionalOnExpression
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${spring.profiles.active:default}' != 'mock'")
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

    public AuthTokenService() {
        // Construimos un WebClient básico SIN usar el builder inyectado
        // Esto evita la dependencia circular con WebClientConfig
        this.webClientRaw = WebClient.builder().build();
        log.info("AuthTokenService: Inicializado con WebClient básico (sin filtros)");
    }

    @PostConstruct
    public void init() {
        log.info("AuthTokenService: @PostConstruct ejecutándose...");
        log.info("AuthTokenService: username={}, password={}", username, (password != null && !password.isBlank()) ? "***configurado***" : "NO CONFIGURADO");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.error("AuthTokenService: ❌ NO HAY CREDENCIALES CONFIGURADAS");
            log.error("AuthTokenService: Verifica que app.catedra.username y app.catedra.password estén en .env o como variables de entorno");
            return;
        }

        log.info("AuthTokenService: Credenciales OK, intentando obtener token inicial...");
        try {
            refreshTokenIfNeeded(true);
            log.info("AuthTokenService: ✅ Inicialización completada exitosamente");
        } catch (Exception ex) {
            log.error("AuthTokenService: ❌ Error al obtener token al inicio: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Tarea programada que renueva el token automáticamente cada 30 minutos.
     * Esto asegura que el token nunca expire mientras el Proxy esté corriendo.
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 1800000) // 30 minutos = 1800000 ms
    public void scheduledTokenRefresh() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return; // Sin credenciales no hay nada que renovar
        }

        log.info("AuthTokenService: ejecutando renovación programada del token JWT");
        try {
            refreshToken(false);
        } catch (Exception ex) {
            log.error("AuthTokenService: error en renovación programada del token: {}", ex.getMessage());
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
                .onStatus(
                    status -> status.value() == 401,
                    clientResponse -> Mono.error(new RuntimeException("Credenciales incorrectas (401). Verifica USERNAME y PASSWORD en la configuración."))
                )
                .bodyToMono(LoginResponseDTO.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (response == null || response.getIdToken() == null || response.getIdToken().isBlank()) {
                throw new IllegalStateException("La respuesta de authenticate no contiene id_token");
            }

            String oldToken = currentToken.get();
            currentToken.set(response.getIdToken());
            lastRefresh.set(Instant.now());

            if (oldToken != null && logErrorsStrongly) {
                String oldShort = oldToken.substring(0, Math.min(50, oldToken.length()));
                log.debug("Token anterior (primeros 50 chars): {}...", oldShort);
            }

            String newShort = response.getIdToken().substring(0, Math.min(50, response.getIdToken().length()));
            log.info("AuthTokenService: token obtenido exitosamente (primeros 50 chars): {}... (longitud total: {} caracteres)",
                     newShort, response.getIdToken().length());

        } catch (Exception ex) {
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.contains("401")) {
                log.error("AuthTokenService: CREDENCIALES INCORRECTAS. Verifica app.catedra.username y app.catedra.password");
            } else if (errorMsg != null && (errorMsg.contains("Connection refused") || errorMsg.contains("UnknownHost"))) {
                log.error("AuthTokenService: NO SE PUEDE CONECTAR al servidor de la cátedra ({}). Verifica que esté accesible.", url);
            } else {
                if (logErrorsStrongly) {
                    log.error("AuthTokenService: error al obtener token: {}", errorMsg);
                } else {
                    log.warn("AuthTokenService: no se pudo refrescar token: {}", errorMsg);
                }
            }
        }
    }
}

