# Proxy

Proxy intermedio entre BackEnd y servicios externos (Cátedra, Kafka, Redis). Expone controladores REST para usuarios, eventos y ventas, devolviendo DTOs.

## Servicios Externos
- **Cátedra**: http://192.168.194.250:8080
- **Kafka**: 192.168.194.250:9092
- **Redis**: 192.168.194.250:6379

## Endpoints Implementados

### UserProxyController (`/proxy/users`)

#### POST `/proxy/users/login`
Login de usuario.
- **URL Externa**: http://192.168.194.250:8080/api/authenticate
- **Entrada**: `LoginRequestDTO`
- **Salida**: `LoginResponseDTO`

### EventoProxyController (`/proxy/eventos`)

#### GET `/proxy/eventos/resumidos`
Listado completo de eventos (datos resumidos).
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/eventos-resumidos
- **Salida**: `List<EventoResumenDTO>`

#### GET `/proxy/eventos`
Listado completo de eventos (con todos los datos).
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/eventos
- **Salida**: `List<EventoDTO>`

#### GET `/proxy/eventos/{id}`
Datos completos de un evento específico.
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/evento/{id}
- **Salida**: `EventoDetalleDTO`

#### POST `/proxy/eventos/bloquear-asientos`
Bloqueo de asientos por evento.
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/bloquear-asientos
- **Entrada**: `BloquearAsientosRequestDTO`
- **Salida**: `BloquearAsientosResponseDTO`

### VentaProxyController (`/proxy/ventas`)

#### POST `/proxy/ventas/realizar`
Venta de asientos por un evento.
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/realizar-venta
- **Entrada**: `RealizarVentaRequestDTO`
- **Salida**: `RealizarVentaResponseDTO`

#### GET `/proxy/ventas`
Listado completo de ventas por alumno (datos resumidos).
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/listar-ventas
- **Salida**: `List<VentaResumenDTO>`

#### GET `/proxy/ventas/{id}`
Ver datos de una venta particular.
- **URL Externa**: http://192.168.194.250:8080/api/endpoints/v1/listar-venta/{id}
- **Salida**: `VentaDetalleDTO`

## Tecnologías
- Spring Boot 3.3.3 (Web, WebFlux, Validation, Kafka, Redis, Actuator)
- WebClient para llamadas HTTP reactivas a servicios externos
- KafkaTemplate para publicar mensajes (configurado)
- RedisTemplate para caché simple (configurado)

## Ejecución
```powershell
.\mvnw.cmd spring-boot:run
```

## Próximos pasos
1. ✅ Endpoints implementados con URLs reales
2. Añadir manejo de errores personalizado (WebClientResponseException)
3. Implementar retry y circuit breaker (Resilience4j)
4. Añadir interceptores para trazabilidad (headers de correlación)
5. Implementar caché con Redis para endpoints de consulta
6. Publicar eventos a Kafka en operaciones críticas (ventas, bloqueos)
7. Añadir métricas personalizadas y health checks

## Notas
- Sin persistencia ni entidades JPA (proxy puro)
- Todos los DTOs en paquete `um.prog2.dto`
- Logging configurado en DEBUG para trazabilidad

