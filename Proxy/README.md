# Proxy

Proxy intermedio entre BackEnd y servicios externos (Cátedra, Kafka, Redis). Expone controladores REST para usuarios, eventos y ventas, devolviendo DTOs.

## Servicios Externos
- **Cátedra (HTTP)**: http://192.168.194.250:8080
- **Kafka (solo consumer)**: 192.168.194.250:9092, tópico `eventos-actualizacion`
- **Redis (lectura de asientos)**: 192.168.194.250:6379, keys `evento_<ID>`

## Endpoints Implementados (Proxy → Cátedra)

### UserProxyController (`/proxy/users`)

#### POST `/proxy/users/login`
Login de usuario.
- **URL Externa Cátedra**: `POST /api/authenticate`
- **Entrada**: `LoginRequestDTO`
- **Salida**: `LoginResponseDTO`
- **Clases relacionadas**: `UserProxyController`, `WebClient`

### EventoProxyController (`/proxy/eventos`)

#### GET `/proxy/eventos/resumidos`
Listado de eventos (datos resumidos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/eventos-resumidos`
- **Salida**: `List<EventoResumenDTO>`
- **Clases relacionadas**: `EventoProxyController`, `EventoResumenDTO`

#### GET `/proxy/eventos`
Listado completo de eventos (con todos los datos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/eventos`
- **Salida**: `List<EventoDTO>`
- **Clases relacionadas**: `EventoProxyController`, `EventoDTO`

#### GET `/proxy/eventos/{id}`
Datos completos de un evento específico.
- **URL Externa Cátedra**: `GET /api/endpoints/v1/evento/{id}`
- **Salida**: `EventoDetalleDTO`
- **Clases relacionadas**: `EventoProxyController`, `EventoDetalleDTO`

#### POST `/proxy/eventos/bloquear-asientos`
Bloqueo de asientos por evento.
- **URL Externa Cátedra**: `POST /api/endpoints/v1/bloquear-asientos`
- **Entrada**: `BloquearAsientosRequestDTO`
- **Salida**: `BloquearAsientosResponseDTO`
- **Clases relacionadas**: `EventoProxyController`, `BloquearAsientosRequestDTO`, `BloquearAsientosResponseDTO`, `NotificadorBackendService`

#### GET `/proxy/eventos/{id}/asientos-estado`
Estado actual de los asientos de un evento.
- **Origen de datos**: Redis cátedra (`StringRedisTemplate`, key `evento_<ID>`, JSON con campo `asientos`)
- **Salida**: `List<AsientoEstadoDTO>`
- **Clases relacionadas**: `EventoProxyController`, `AsientoRedisService`, `AsientoEstadoDTO`

### VentaProxyController (`/proxy/ventas`)

#### POST `/proxy/ventas/realizar`
Venta de asientos por un evento.
- **URL Externa Cátedra**: `POST /api/endpoints/v1/realizar-venta`
- **Entrada**: `RealizarVentaRequestDTO`
- **Salida**: `RealizarVentaResponseDTO`
- **Clases relacionadas**: `VentaProxyController`, `RealizarVentaRequestDTO`, `RealizarVentaResponseDTO`, `NotificadorBackendService`

#### GET `/proxy/ventas`
Listado completo de ventas por alumno (datos resumidos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/listar-ventas`
- **Salida**: `List<VentaResumenDTO>`
- **Clases relacionadas**: `VentaProxyController`, `VentaResumenDTO`

#### GET `/proxy/ventas/{id}`
Ver datos de una venta particular.
- **URL Externa Cátedra**: `GET /api/endpoints/v1/listar-venta/{id}`
- **Salida**: `VentaDTO`
- **Clases relacionadas**: `VentaProxyController`, `VentaDTO`

## Mensajería Kafka → Backend (Webhook)

- **Listener Kafka**: `EventoCambioConsumer`
  - **Tópico**: `eventos-actualizacion` (propiedad `app.kafka.consumer.topic`)
  - **DTOs de Cátedra duplicados (Plan B)**:
    - `VentaCompletadaEventoDTO`
    - `AsientosBloqueadosEventoDTO`
    - `EventoCambiadoEventoDTO`
  - Detecta el tipo de mensaje según la estructura del JSON y construye un `BackendNotificacionDTO` con:
    - `topic`: tipo lógico (`VENTA_COMPLETADA`, `ASIENTOS_BLOQUEADOS`, `EVENTO_CAMBIADO`, etc.)
    - `payload`: JSON crudo recibido de Kafka
    - `partition`, `offset`, `key`: metadata de Kafka
  - Envía el `BackendNotificacionDTO` al Backend mediante `NotificadorBackendService` (webhook HTTP).

- **Servicio de notificación al Backend**: `NotificadorBackendService`
  - Usa `WebClient` para llamar al Backend propio en:
    - `app.backend.base-url` + `app.backend.webhook-path`
  - Operaciones que lo usan:
    - `EventoCambioConsumer.onMessage(...)` (mensajes Kafka)
    - `UserProxyController.login(...)` (respuesta HTTP de login)
    - `EventoProxyController.bloquearAsientos(...)` (respuesta HTTP de bloqueo)
    - `VentaProxyController.realizarVenta(...)` (respuesta HTTP de venta)
  - DTO de webhook: `BackendNotificacionDTO` (timestamp, topic, payload, partition, offset, key).

## Acceso a Redis de la Cátedra

- **Servicio**: `AsientoRedisService`
  - Usa `StringRedisTemplate` y `ObjectMapper`.
  - Lee la key `evento_<ID>` desde Redis cátedra.
  - Parsea el JSON `{ "eventoId": ..., "asientos": [ { "fila", "columna", "estado", ... } ] }`.
  - Expone al Backend `List<AsientoEstadoDTO>` a través del endpoint `/proxy/eventos/{id}/asientos-estado`.

## Tecnologías
- Spring Boot 3.3.3 (Web, WebFlux, Validation, Kafka, Redis, Actuator)
- `WebClient` para llamadas HTTP reactivas a servicios externos
- `spring-kafka` para consumir mensajes del tópico `eventos-actualizacion`
- `spring-data-redis` (`StringRedisTemplate`) para leer estado de asientos en Redis de la cátedra

## Ejecución
```powershell
./mvnw.cmd spring-boot:run
```

## Notas
- Sin persistencia ni entidades JPA (proxy puro, sin base de datos propia)
- DTOs en paquetes `um.prog2.dto.*` (autenticación, eventos, ventas, notificación)
- Logging configurado en DEBUG para `um.prog2.*` para facilitar la trazabilidad en desarrollo
