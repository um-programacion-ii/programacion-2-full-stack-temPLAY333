# Proxy - Servicio Intermedio

Servicio proxy intermedio entre el **BackEnd** y los servicios externos de la cátedra (API HTTP, Kafka, Redis).

## 🎯 Arquitectura: Híbrida Síncrona/Asíncrona

El Proxy opera en **dos modos simultáneos**:

### 1. **Modo Síncrono** (HTTP → HTTP)
- Los endpoints REST del Proxy **forwardean** llamadas HTTP a la API de la cátedra
- Devuelven respuestas **síncronas inmediatas** al cliente
- Opcionalmente notifican al Backend vía webhook con la respuesta HTTP

### 2. **Modo Asíncrono** (Kafka → Webhook)
- Escucha el topic de Kafka de la cátedra (`eventos-actualizacion`)
- Detecta eventos asíncronos (ventas completadas, asientos bloqueados, etc.)
- Notifica al Backend vía webhook HTTP con el evento de Kafka

**⚠️ IMPORTANTE PARA EL BACKEND**: Recibirás notificaciones de **ambos canales**:
- Respuestas HTTP síncronas (opcional, si el Proxy las reenvía)
- Eventos asíncronos de Kafka (siempre, cuando Kafka está habilitado)

## 🔌 Servicios Externos (Cátedra)

Registro de sesión del alumno. http://SERVIDOR:PUERTO/api/v1/agregar_usuario .
Ver payload 1.
● Login de usuario. http://localhost:8080/api/authenticate . Ver payload 2.
● Listado
completo
de
eventos
(datos
resumidos).
http://SERVIDOR:PUERTO/api/endpoints/v1/eventos-resumidos. Ver payload 3.
● Listado
completo
de
eventos
(con
todos
http://SERVIDOR:PUERTO/api/endpoints/v1/eventos. Ver payload 4.
● Datos
completos
de
un
los
http://SERVIDOR:PUERTO/api/endpoints/v1/evento/{id} . Ver payload 5.
● Bloqueo
de
asiento
por
datos).
evento.
evento.
http://SERVIDOR:PUERTO/api/endpoints/v1/bloquear-asientos . Ver payload 6.
● Venta
de
asientos
por
http://SERVIDOR:PUERTO/api/endpoints/v1/realizar-venta . Ver payload 7.
evento.
● Listado completo de ventas por cada alumno (datos resumidos).
http://SERVIDOR:PUERTO/api/endpoints/v1/listar-ventas . Ver payload 8.
● Ver
datos
de
una
venta
particular.
http://SERVIDOR:PUERTO/api/endpoints/v1/listar-venta/{id}. Ver payload 9.

### API HTTP
- **Base URL**: `http://192.168.194.250:8080`
- **Autenticación**: JWT Bearer Token Automática ✨
  - ✅ **Autenticación automática al iniciar**: Obtiene el token usando credenciales configuradas
  - ✅ **Renovación automática cada 30 minutos**: `@Scheduled` renueva el token antes de que expire
  - ✅ **Inyección automática en requests**: Cada HTTP request incluye `Authorization: Bearer {token}`
  - ✅ **Endpoints de administración**: `/actuator/auth/status` y `/actuator/auth/refresh`
  - ✅ **Sin intervención manual**: No necesitas scripts externos
  - Config: `app.catedra.username` y `app.catedra.password`
  - Ver: [VERIFICACION-TOKEN.md](VERIFICACION-TOKEN.md) para detalles completos

### Kafka (Consumer)
- **Bootstrap Servers**: `192.168.194.250:9092`
- **Topic**: `eventos-actualizacion`
- **Group ID**: `proxy-grupo`
- **⚠️ Puede deshabilitarse**: `spring.kafka.enabled=false` (útil para desarrollo local)
- **Consumer**: `EventoCambioConsumer` detecta tipo de evento y notifica al Backend

### Redis (Solo Lectura)
- **Host**: `192.168.194.250:6379`
- **Keys**: `evento_<ID>` con JSON `{"eventoId": ..., "asientos": [...]}`
- **Uso**: Cache de estado de asientos (bloqueados/vendidos)
- **⚠️ Puede deshabilitarse**: `spring.data.redis.enabled=false`

## 🔐 Autenticación JWT Automática

### Sistema de Token Automático

El Proxy implementa un sistema completamente automático de autenticación JWT:

#### 🚀 Al Iniciar
```java
AuthTokenService: solicitando nuevo token a http://192.168.194.250:8080/api/authenticate para usuario templay333
AuthTokenService: token obtenido exitosamente (primeros 50 chars): eyJhbG... (longitud total: 206 caracteres)
```

#### ⏰ Cada 30 Minutos (Automático)
```java
AuthTokenService: ejecutando renovación programada del token JWT
AuthTokenService: token obtenido exitosamente...
```

#### 🔧 Endpoints de Administración

**Verificar estado del token:**
```bash
GET /actuator/auth/status
```
Respuesta: `{ "hasToken": true, "tokenLength": 206, "tokenPreview": "eyJ...", "message": "..." }`

**Forzar renovación del token:**
```bash
POST /actuator/auth/refresh
```
Respuesta: `{ "success": true, "tokenLength": 206, "tokenPreview": "eyJ...", "message": "..." }`

#### ✅ Verificación Completa

```powershell
# Script automático de verificación
.\verificar-token.ps1
```

Ver documentación completa: **[VERIFICACION-TOKEN.md](VERIFICACION-TOKEN.md)**

---

## 📡 Endpoints Implementados (Proxy → Cátedra)

**Base URL del Proxy**: `http://localhost:8080`

### UserProxyController (`/api/users`)

#### POST `/api/users/login`
Login de usuario.
- **URL Externa Cátedra**: `POST /api/authenticate`
- **Entrada**: `LoginRequestDTO { username, password }`
- **Salida**: `LoginResponseDTO { id_token }`
- **Respuesta Síncrona**: ✅ Inmediata

---

### EventoProxyController (`/api/eventos`)

#### GET `/api/eventos/resumidos`
Listado de eventos (datos resumidos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/eventos-resumidos`
- **Salida**: `List<EventoResumenDTO>`
- **Respuesta Síncrona**: ✅ Inmediata

#### GET `/api/eventos`
Listado completo de eventos (con todos los datos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/eventos`
- **Salida**: `List<EventoDTO>`
- **Respuesta Síncrona**: ✅ Inmediata

#### GET `/api/eventos/{id}`
Datos completos de un evento específico.
- **URL Externa Cátedra**: `GET /api/endpoints/v1/evento/{id}`
- **Salida**: `EventoDetalleDTO`
- **Respuesta Síncrona**: ✅ Inmediata

#### POST `/api/eventos/{id}/bloquear-asientos`
Bloqueo de asientos por evento.
- **URL Externa Cátedra**: `POST /api/endpoints/v1/bloquear-asientos`
- **Entrada**: `BloquearAsientosRequestDTO { eventoId, asientos[] }`
- **Salida**: `BloquearAsientosResponseDTO { resultado, mensaje }`
- **Respuesta Síncrona**: ✅ Inmediata
- **Notificación Asíncrona**: ⚡ Puede llegar evento `ASIENTOS_BLOQUEADOS` vía Kafka después

#### GET `/api/eventos/{id}/asientos-estado`
Estado actual de los asientos de un evento (desde Redis).
- **Origen**: Redis cátedra (key `evento_{id}`)
- **Salida**: `List<AsientoEstadoDTO> { fila, columna, estado }`
- **Respuesta Síncrona**: ✅ Inmediata
- **Nota**: Solo retorna asientos bloqueados/vendidos (el resto se asume disponible)

---

### VentaProxyController (`/api/ventas`)

#### POST `/api/ventas/realizar`
Venta de asientos por un evento.
- **URL Externa Cátedra**: `POST /api/endpoints/v1/realizar-venta`
- **Entrada**: `RealizarVentaRequestDTO { eventoId, asientos[], username }`
- **Salida**: `RealizarVentaResponseDTO { resultado, mensaje }`
- **Respuesta Síncrona**: ✅ Inmediata (éxito/fallo del request)
- **Notificación Asíncrona**: ⚡ Cuando la venta se completa, llega evento `VENTA_COMPLETADA` vía Kafka

#### GET `/api/ventas`
Listado completo de ventas por alumno (datos resumidos).
- **URL Externa Cátedra**: `GET /api/endpoints/v1/listar-ventas`
- **Salida**: `List<VentaResumenDTO>`
- **Respuesta Síncrona**: ✅ Inmediata

#### GET `/api/ventas/{id}`
Ver datos de una venta particular.
- **URL Externa Cátedra**: `GET /api/endpoints/v1/listar-venta/{id}`
- **Salida**: `VentaDTO`
- **Respuesta Síncrona**: ✅ Inmediata

---

## 🔔 Notificaciones al Backend (Webhook)

### ⚠️ ENDPOINT QUE EL BACKEND DEBE IMPLEMENTAR

El Backend **DEBE** implementar el siguiente endpoint para recibir notificaciones del Proxy:

```
POST /api/webhooks/evento-cambio
Content-Type: application/json
Authorization: Bearer {token}  (opcional)
```

**Request Body**: `BackendNotificacionDTO` (ver estructura abajo)

### Flujo de Notificación

```
[Kafka Cátedra] → [EventoCambioConsumer] → [NotificadorBackendService] → [Backend Webhook]
```

### Configuración del Endpoint

El Proxy construye la URL completa concatenando:
- `app.backend.base-url` (default: `http://localhost:8081`)
- `app.backend.webhook-path` (default: `/api/webhooks/evento-cambio`)

**URL Completa por Defecto**: `http://localhost:8081/api/webhooks/evento-cambio`

**Headers**:
- `Content-Type: application/json`
- `Authorization: Bearer {app.backend.token}` (solo si está configurado)

**Método**: `POST`

### DTO de Notificación: `BackendNotificacionDTO`

```json
{
  "timestamp": "2025-12-10T19:30:00Z",
  "topic": "VENTA_COMPLETADA",
  "partition": 0,
  "offset": 12345,
  "key": "evento-1",
  "payload": "{\"ventaId\":123,\"eventoId\":1,\"asientos\":[...],\"fechaVenta\":\"...\"}"
}
```

**Campos**:
- `timestamp`: Momento en que el Proxy recibe/procesa el evento
- `topic`: Tipo lógico del evento (ver tipos soportados abajo)
- `partition`, `offset`, `key`: Metadata de Kafka (null si viene de HTTP)
- `payload`: JSON **crudo** del evento (string, debe parsearse en el Backend)

### Tipos de Eventos Soportados

El Proxy **detecta automáticamente** el tipo analizando la estructura del JSON:

| `topic` | Estructura JSON | Origen | Descripción |
|---------|----------------|--------|-------------|
| `VENTA_COMPLETADA` | `{ventaId, eventoId, asientos, fechaVenta}` | Kafka | Venta procesada exitosamente |
| `ASIENTOS_BLOQUEADOS` | `{eventoId, asientos, bloqueadoHasta}` | Kafka | Asientos reservados temporalmente |
| `EVENTO_CAMBIADO` | `{eventoId, tipoCambio}` | Kafka | Modificación en evento (nombre, fecha, etc.) |
| `UNKNOWN` | Otros | Kafka | Estructura no reconocida |

### Ejemplo de Implementación en el Backend

```java
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    @PostMapping("/evento-cambio")
    public ResponseEntity<Void> recibirEventoCambio(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BackendNotificacionDTO notificacion) {
        
        // 1. Validar token si está configurado
        // if (authHeader != null && !validarToken(authHeader)) {
        //     return ResponseEntity.status(401).build();
        // }
        
        // 2. Procesar según el tipo de evento
        switch (notificacion.getTopic()) {
            case "VENTA_COMPLETADA":
                procesarVentaCompletada(notificacion.getPayload());
                break;
            case "ASIENTOS_BLOQUEADOS":
                procesarAsientosBloqueados(notificacion.getPayload());
                break;
            case "EVENTO_CAMBIADO":
                procesarEventoCambiado(notificacion.getPayload());
                break;
            default:
                log.warn("Tipo de evento desconocido: {}", notificacion.getTopic());
        }
        
        return ResponseEntity.ok().build();
    }
    
    private void procesarVentaCompletada(String payloadJson) {
        // El payload es un JSON string, debe parsearse
        // Ejemplo: {"ventaId":123,"eventoId":1,"asientos":[...],"fechaVenta":"..."}
        ObjectMapper mapper = new ObjectMapper();
        VentaCompletadaEventoDTO venta = mapper.readValue(payloadJson, VentaCompletadaEventoDTO.class);
        // ... tu lógica de negocio
    }
}
```

**Importante**: 
- El endpoint **NO debe devolver error** para evitar que el Proxy reintente
- Es responsabilidad del Backend manejar errores internamente
- El `payload` es un **String JSON** que debe parsearse, no un objeto

### Componentes del Proxy

#### `EventoCambioConsumer`
- **Listener Kafka**: Escucha el topic `eventos-actualizacion`
- **Group ID**: `proxy-grupo`
- **Detección de tipo**: Analiza estructura JSON (fallback si no hay header de tipo)
- **Manejo de errores**: Log y continúa (no rompe el consumidor)

#### `NotificadorBackendService`
- **WebClient**: Llamadas HTTP reactivas al Backend
- **Fire-and-Forget**: No espera respuesta del Backend ni reintenta
- **Timeout**: Default de WebClient (30s)
- **Error handling**: Log de error, no propaga excepción

---

## 🗄️ Acceso a Redis (Estado de Asientos)

### `AsientoRedisService`
- **Propósito**: Leer estado actual de asientos desde cache de la cátedra
- **Template**: `StringRedisTemplate`
- **Key pattern**: `evento_{eventoId}`
- **Formato JSON**:
  ```json
  {
    "eventoId": 1,
    "asientos": [
      {"fila": 1, "columna": 3, "estado": "Bloqueado"},
      {"fila": 2, "columna": 5, "estado": "Vendido"}
    ]
  }
  ```
- **Endpoint**: `GET /api/eventos/{id}/asientos-estado`
- **Salida**: `List<AsientoEstadoDTO>` ordenada por fila y columna
- **Nota**: Solo se guardan asientos **bloqueados o vendidos**, el resto se asume disponible

---

## 🛠️ Tecnologías

- **Spring Boot 3.3.3**
  - `spring-boot-starter-web`: REST controllers
  - `spring-boot-starter-webflux`: WebClient reactivo
  - `spring-boot-starter-validation`: Validación de DTOs
  - `spring-kafka`: Consumer de Kafka
  - `spring-data-redis`: Acceso a Redis
  - `spring-boot-starter-actuator`: Health checks y métricas
- **Jackson**: Serialización/deserialización JSON
- **Lettuce**: Cliente Redis (async)
- **Maven**: Build y gestión de dependencias

---

## ⚙️ Configuración

### Variables de Entorno Principales

```properties
# Cátedra - API HTTP
APP_CATEDRA_BASE_URL=http://192.168.194.250:8080
APP_CATEDRA_USERNAME=usuario
APP_CATEDRA_PASSWORD=contraseña

# Backend - Webhook
APP_BACKEND_BASE_URL=http://localhost:8081
APP_BACKEND_WEBHOOK_PATH=/api/webhooks/evento-cambio
APP_BACKEND_TOKEN=opcional-bearer-token

# Kafka
APP_KAFKA_BOOTSTRAP_SERVERS=192.168.194.250:9092
APP_KAFKA_CONSUMER_GROUP=proxy-grupo
APP_KAFKA_CONSUMER_TOPIC=eventos-actualizacion
SPRING_KAFKA_ENABLED=true  # false para deshabilitar

# Redis
APP_REDIS_HOST=192.168.194.250
APP_REDIS_PORT=6379
```

### Perfiles de Spring

- **default**: Producción (todos los servicios habilitados)
- **dev**: Desarrollo local (Kafka y Redis deshabilitados)
- **mock**: Simulación completa sin servicios externos (ver [PROFILE-MOCK.md](PROFILE-MOCK.md)) ✨
- **integration**: Tests de integración (servicios reales)
- **test**: Tests unitarios (todo mockeado)

---

## 🚀 Ejecución

### Modo Producción (con todos los servicios)
```powershell
mvn spring-boot:run
```

### Modo Desarrollo (sin Kafka/Redis)
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O usar el script:
```powershell
.\ejecutar-proxy-dev.bat
```

### Modo Mock (simula todos los servicios) ✨
```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=mock"
```

**Ideal para**: Desarrollo sin servicios de la cátedra, demos, testing del flujo completo Mobile→Backend→Proxy.

Ver documentación completa: **[PROFILE-MOCK.md](PROFILE-MOCK.md)**

### Deshabilitar solo Kafka
```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.kafka.enabled=false"
```

### Health Check
```powershell
curl http://localhost:8080/actuator/health
```

---

## 🧪 Testing

### Tests Unitarios (sin servicios externos)
```powershell
mvn test
```
- **Resultados**: 21 tests (incluye 4 tests de AuthTokenService)
  - Controllers: EventoProxyController, UserProxyController, VentaProxyController
  - Services: AuthTokenService ✨, AsientoRedisService, NotificadorBackendService
  - Messaging: EventoCambioConsumer
- **Mocks**: WebClient, Redis, NotificadorBackendService

**Tests específicos de autenticación JWT:**
```powershell
mvn test -Dtest=AuthTokenServiceTest
```
Verifica: obtención inicial, renovación forzada, cache de token, renovación programada

### Tests de Integración (requiere servicios de la cátedra)
```powershell
$env:APP_CATEDRA_USERNAME='usuario'
$env:APP_CATEDRA_PASSWORD='contraseña'
$env:APP_KAFKA_BOOTSTRAP_SERVERS='192.168.194.250:9092'
mvn verify -Pit-tests
```
- **Resultados**: 2 tests (ProxyIT: GET eventos resumidos, GET asientos estado)
- **Requiere**: Servicios de la cátedra accesibles desde la red

---

## 🐛 Solución de Problemas

### ✅ Verificar Sistema de Autenticación

```powershell
# Verificación automática completa
.\verificar-token.ps1

# O verificar manualmente
Invoke-RestMethod -Uri "http://localhost:8080/actuator/auth/status"
```

Ver: **[VERIFICACION-TOKEN.md](VERIFICACION-TOKEN.md)** para guía completa de verificación.

---

### Error: `kafka:9092` no se puede resolver
**Problema**: El servidor Kafka de la cátedra se anuncia como `kafka:9092` pero tu máquina no resuelve ese hostname.

**Solución 1** (Recomendada): Agregar entrada al archivo hosts
```powershell
# Ejecutar como Administrador
.\agregar-kafka-hosts.bat
```

O manual: Agregar a `C:\Windows\System32\drivers\etc\hosts`:
```
192.168.194.250 kafka
```

**Solución 2**: Deshabilitar Kafka temporalmente
```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.kafka.enabled=false"
```

### Error: Connection refused a servicios de la cátedra
**Problema**: Los servicios `192.168.194.250:8080/9092/6379` no están accesibles desde tu red.

**Solución**: Usar perfil dev (deshabilita Kafka/Redis)
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Error: "401 Unauthorized" al llamar a la cátedra
**Problema**: Token JWT no disponible, expirado o credenciales incorrectas.

**Solución**:

1. **Verificar estado del token:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/auth/status"
```

2. **Si no hay token, verificar credenciales:**
```powershell
# Verifica que estén configuradas
$env:APP_CATEDRA_USERNAME
$env:APP_CATEDRA_PASSWORD

# Si faltan, agrégalas:
$env:APP_CATEDRA_USERNAME='templay333'
$env:APP_CATEDRA_PASSWORD='B0lud0t0t4l'

# Reinicia el Proxy
```

3. **Forzar renovación manual:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/auth/refresh" -Method Post
```

**Nota**: El Proxy renueva automáticamente el token cada 30 minutos, por lo que este error no debería ocurrir en producción.

---

## 📝 Notas Arquitectónicas

### Para el Backend Developer

1. **Sin Base de Datos Propia**: El Proxy es stateless, no persiste nada
2. **Autenticación Transparente**: El Proxy maneja automáticamente el JWT de la cátedra
   - ✅ Obtiene el token al iniciar
   - ✅ Renueva cada 30 minutos automáticamente
   - ✅ El Backend **nunca** verá errores 401 por token expirado
   - Ver: [VERIFICACION-TOKEN.md](VERIFICACION-TOKEN.md)
3. **Webhooks son Fire-and-Forget**: El Proxy no espera respuesta del Backend
4. **DTOs Compartidos**: Los DTOs en `um.prog2.dto.*` pueden compartirse con el Backend (copiarlos o generar librería común)
5. **Modelo Híbrido**:
   - **Operaciones de consulta (GET)**: 100% síncronas
   - **Operaciones de modificación (POST)**: Respuesta síncrona + posible evento asíncrono vía Kafka
6. **El Backend debe procesar el `payload`**: Es un JSON string, no un objeto deserializado
7. **Idempotencia**: Kafka puede reenviar mensajes, considera usar `offset` + `partition` como ID único

### Respuesta a: "¿El Proxy pasó de asíncrono a síncrono?"

**No, el Proxy sigue siendo HÍBRIDO**:

- **Los endpoints HTTP son síncronos**: Llamas al Proxy, el Proxy llama a la cátedra, te devuelve la respuesta inmediatamente
- **Kafka sigue siendo asíncrono**: Eventos de la cátedra llegan vía Kafka al Proxy, que los reenvía al Backend vía webhook

**Ejemplo de flujo completo para una venta**:
1. Cliente → `POST /api/ventas/realizar` → Proxy
2. Proxy → Cátedra → respuesta inmediata (éxito/fallo)
3. Proxy → Cliente (respuesta síncrona)
4. [Después, cuando la venta se procesa en la cátedra]
5. Cátedra → Kafka → `VENTA_COMPLETADA`
6. Proxy (Kafka Consumer) → Backend (webhook)

**El problema de Kafka que solucionamos**: Era un error de DNS donde el broker Kafka se anunciaba como `kafka:9092` pero la máquina no podía resolver ese hostname. Se soluciona agregando la entrada al archivo hosts de Windows o deshabilitando Kafka temporalmente.

### Estructura de Paquetes

```
um.prog2
├── config/           
│   ├── WebClientConfig         # WebClient con filtro JWT automático ✨
│   ├── KafkaConfig
│   └── RedisConfig
├── dto/
│   ├── autenticacion/
│   ├── consultaventas/
│   ├── evento/
│   ├── notificacion/           # BackendNotificacionDTO ⭐
│   └── venta/
├── messaging/         
│   └── EventoCambioConsumer    # Consumer Kafka ⭐
├── service/
│   ├── AsientoRedisService
│   ├── AuthTokenService        # Gestión automática del token JWT ✨
│   └── NotificadorBackendService ⭐
└── web/              
    ├── AuthStatusController    # Endpoints de admin del token ✨
    ├── EventoProxyController
    ├── UserProxyController
    └── VentaProxyController
```

⭐ = Componentes clave para integración Backend  
✨ = Sistema de autenticación JWT automática

### Logging

- **Nivel INFO**: Operaciones principales, autenticación, Kafka conectado/desconectado
- **Nivel DEBUG**: Request/response de cada operación, contenido de mensajes Kafka
- **Nivel ERROR**: Fallos de conexión, errores de parsing, webhooks fallidos

Configurado para `um.prog2.*` en DEBUG para facilitar troubleshooting.
