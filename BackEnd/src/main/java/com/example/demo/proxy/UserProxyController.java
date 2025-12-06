package com.example.demo.proxy;

import com.example.demo.service.dto.LoginRequestDTO;
import com.example.demo.service.dto.LoginResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy REST para operaciones de usuario contra la Cátedra.
 */
@RestController
@RequestMapping("/proxy/users")
public class UserProxyController {

    private static final Logger log = LoggerFactory.getLogger(UserProxyController.class);

    private final RestTemplate restTemplate;
    private final NotificadorBackendService notificadorBackendService;

    @Value("${app.catedra.base-url:http://192.168.194.250:8080}")
    private String catedraBaseUrl;

    public UserProxyController(NotificadorBackendService notificadorBackendService) {
        this.restTemplate = new RestTemplate();
        this.notificadorBackendService = notificadorBackendService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.debug("Proxy login contra Cátedra para usuario: {}", request.getUsername());

        String url = catedraBaseUrl + "/api/authenticate";
        LoginResponseDTO response = restTemplate.postForObject(url, request, LoginResponseDTO.class);

        if (response != null) {
            BackendNotificacionDTO notificacion = new BackendNotificacionDTO();
            notificacion.setTopic("LOGIN_EXITOSO");
            notificacion.setPayload("{\"username\":\"" + request.getUsername() + "\"}");
            notificadorBackendService.notificar(notificacion);
        }

        return ResponseEntity.ok(response);
    }
}
