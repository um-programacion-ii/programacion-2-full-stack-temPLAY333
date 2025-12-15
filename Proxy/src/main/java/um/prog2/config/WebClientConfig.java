package um.prog2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import um.prog2.service.AuthTokenService;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${app.catedra.base-url}")
    private String catedraBaseUrl;

    @Value("${app.catedra.api-base:/api}")
    private String apiBase;

    @Value("${app.catedra.jwt-token:}")
    private String jwtTokenFromEnv;

    private final AuthTokenService authTokenService;

    public WebClientConfig(@org.springframework.beans.factory.annotation.Autowired(required = false) AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
        if (authTokenService != null) {
            log.info("WebClientConfig: Inicializado con AuthTokenService");
        } else {
            log.info("WebClientConfig: Inicializado SIN AuthTokenService (modo mock)");
        }
    }

    /**
     * WebClient principal hacia la cátedra.
     *
     * - Usa baseUrl = app.catedra.base-url + app.catedra.api-base
     * - Intenta usar token dinámico desde AuthTokenService.
     * - Si no hay token dinámico y hay app.catedra.jwt-token, usa ese como fallback.
     * - Si no hay ninguno, sigue funcionando sin cabecera Authorization.
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        String base = catedraBaseUrl;
        if (apiBase != null && !apiBase.isBlank()) {
            // Evitar dobles "/" al concatenar
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            if (!apiBase.startsWith("/")) {
                base = base + "/" + apiBase;
            } else {
                base = base + apiBase;
            }
        }

        WebClient.Builder b = builder.baseUrl(base);

        final String staticToken = jwtTokenFromEnv; // capturamos el valor actual como efectivamente final
        final boolean hasStaticToken = staticToken != null && !staticToken.isBlank();
        if (hasStaticToken) {
            log.info("WebClientConfig: se encontró app.catedra.jwt-token; se usará como fallback si AuthTokenService no provee token.");
        }

        // Filtro que, en cada request, consulta al AuthTokenService; si no hay token dinámico,
        // usa el token estático (si existe). Si tampoco hay, no agrega Authorization.
        ExchangeFilterFunction authFilter = (request, next) -> {
            String token = null;
            String tokenSource = null;

            // Intentar obtener token dinámico si AuthTokenService está disponible
            if (authTokenService != null) {
                token = authTokenService.getCurrentToken().orElse(null);
                tokenSource = "AuthTokenService";
            }

            // Fallback a token estático
            if (token == null && hasStaticToken) {
                token = staticToken;
                tokenSource = "static (.env)";
            }

            // Sin token disponible
            if (token == null || token.isBlank()) {
                if (authTokenService != null) {
                    log.warn("WebClient Filter: NO HAY TOKEN disponible para request a {}", request.url());
                }
                // En modo mock, esto es esperado y no se loggea como warning
                return next.exchange(request);
            }

            log.debug("WebClient Filter: Aplicando token desde {} (longitud: {}) a request: {}",
                      tokenSource, token.length(), request.url());

            String finalToken = token;
            ClientRequest newRequest = ClientRequest.from(request)
                .headers(headers -> headers.setBearerAuth(finalToken))
                .build();
            return next.exchange(newRequest);
        };

        b.filter(authFilter);

        return b.build();
    }
}
