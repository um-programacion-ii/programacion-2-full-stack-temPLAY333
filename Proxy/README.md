# Proxy - Servicio Intermedio

Servicio proxy intermedio entre el **BackEnd** y los servicios externos de la cátedra (API HTTP, Kafka, Redis).

## 📚 Documentación

### Para Desarrolladores del Proxy:
- **[README.md](./README.md)** - Este archivo (arquitectura, configuración, ejecución)
- **[VERIFICACION-TOKEN.md](./VERIFICACION-TOKEN.md)** - Sistema de autenticación JWT automática

### Para el Backend:
- **[PROXY-API.md](./PROXY-API.md)** ⭐ - Documentación completa de la API del Proxy (request/response de cada endpoint)
- **[RESUMEN-ACUERDO-Backend.md](../BackEnd/RESUMEN-ACUERDO-Backend.md)** ⭐ - Resumen ejecutivo de acuerdos y correcciones
- **[Backend-INTEGRACION-Proxy.md](../BackEnd/Backend-INTEGRACION-Proxy.md)** - Documento de integración (corregido)
- **[ANALISIS-INTEGRACION-Backend.md](../BackEnd/ANALISIS-INTEGRACION-Backend.md)** - Análisis detallado de discrepancias

### Especificaciones de la Cátedra:
- **[PayLoads Catedra](./PayLoads%20Catedra)** - Especificación de payloads de la cátedra (9 payloads)

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

El Proxy se comunica con los siguientes servicios de la cátedra. Ver archivo **[PayLoads Catedra](./PayLoads%20Catedra)** para detalles completos.

| Endpoint Cátedra | Método | Payload | Descripción |
|-----------------|--------|---------|-------------|
| `/api/v1/agregar_usuario` | POST | Payload 1 | ❌ No usado (registro manual) |
| `/api/authenticate` | POST | Payload 2 | ✅ Login de usuario |
| `/api/endpoints/v1/eventos-resumidos` | GET | Payload 3 | ✅ Listado de eventos (resumidos) |
| `/api/endpoints/v1/eventos` | GET | Payload 4 | ✅ Listado de eventos (completos) |
| `/api/endpoints/v1/evento/{id}` | GET | Payload 5 | ✅ Detalle de un evento |
| `/api/endpoints/v1/bloquear-asientos` | POST | Payload 6 | ✅ Bloquear asientos |
| `/api/endpoints/v1/realizar-venta` | POST | Payload 7 | ✅ Realizar venta |
| `/api/endpoints/v1/listar-ventas` | GET | Payload 8 | ✅ Listar ventas del alumno |
| `/api/endpoints/v1/listar-venta/{id}` | GET | Payload 9 | ✅ Detalle de una venta |

**Documentación completa de la API del Proxy**: [PROXY-API.md](./PROXY-API.md)

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

Ver documentación completa con ejemplos de request/response: **[PROXY-API.md](./PROXY-API.md)**

### Resumen de Endpoints

| Endpoint Proxy | Método | Endpoint Cátedra | Payload | Respuesta |
|----------------|--------|------------------|---------|-----------|
| `/api/users/login` | POST | `/api/authenticate` | Payload 2 | Síncrona ✅ |
| `/api/eventos/resumidos` | GET | `/api/endpoints/v1/eventos-resumidos` | Payload 3 | Síncrona ✅ |
| `/api/eventos` | GET | `/api/endpoints/v1/eventos` | Payload 4 | Síncrona ✅ |
| `/api/eventos/{id}` | GET | `/api/endpoints/v1/evento/{id}` | Payload 5 | Síncrona ✅ |
| `/api/eventos/bloquear-asientos` | POST | `/api/endpoints/v1/bloquear-asientos` | Payload 6 | Síncrona ✅ + Kafka ⚡ |
| `/api/eventos/{id}/asientos-estado` | GET | Redis (`evento:{id}:asientos`) | N/A | Síncrona ✅ |
| `/api/ventas/realizar` | POST | `/api/endpoints/v1/realizar-venta` | Payload 7 | Síncrona ✅ + Kafka ⚡ |
| `/api/ventas` | GET | `/api/endpoints/v1/listar-ventas` | Payload 8 | Síncrona ✅ |
| `/api/ventas/{id}` | GET | `/api/endpoints/v1/listar-venta/{id}` | Payload 9 | Síncrona ✅ |

**Leyenda**:
- ✅ Respuesta síncrona inmediata
- ⚡ Puede generar evento asíncrono vía Kafka

### Endpoints Exclusivos del Proxy

Estos endpoints NO existen en la cátedra:

- **`GET /api/eventos/{id}/asientos-estado`**: Consulta estado de asientos desde Redis
- **`GET /actuator/health`**: Health check del servicio
- **`GET /actuator/auth/status`**: Estado del token JWT
- **`POST /actuator/auth/refresh`**: Renovar token JWT manualmente

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
- **mock**: Simulación completa sin servicios externos (AuthTokenService, Kafka y Redis deshabilitados, MockCatedraService activo) ✨
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

**Qué hace**:
- ✅ Deshabilita AuthTokenService (no intenta conectarse a la cátedra)
- ✅ Deshabilita Kafka consumer
- ✅ Deshabilita Redis
- ✅ Activa MockCatedraService con datos inventados (5 eventos, ventas simuladas)
- ✅ Simula webhooks asíncronos al Backend con delay random

**Ideal para**: Desarrollo cuando los servicios de la cátedra están caídos, demos, testing del flujo completo Mobile→Backend→Proxy.

#### 📊 Datos Mock del Proxy vs Mobile

Los datos mock están **inspirados** en el MockData del mobile pero con **variaciones intencionales** para facilitar el debugging:

| Campo | Mobile Mock | Proxy Mock | Diferencia |
|-------|-------------|------------|------------|
| **Evento 1** | "Concierto de Rock Sinfónico" | "Concierto Sinfónico de Rock Clásico" | Título invertido |
| **Fecha 1** | 2024-10-26 | 2025-12-26 | Año diferente |
| **Precio 1** | Sin precio explícito | $2800 | Precio agregado |
| **Sala 1** | 15x20 asientos | 18x25 asientos | Sala más grande |
| **Evento 2** | "Final de Conferencia" | "Gran Final de Campeonato" | Título extendido |
| **Precio 2** | Sin precio | $5200 | Precio agregado |
| **Sala 2** | Sin dimensiones | 20x30 asientos | Dimensiones agregadas |
| **Evento 3** | "Obra de Teatro Clásico" | "Teatro Clásico Moderno" | Énfasis diferente |
| **Integrantes 1** | Alex Turner, Miles Kane, Matt Helders | Robert Plant, Jimmy Page, John Bonham | Nombres de Led Zeppelin |

**¿Por qué estas diferencias?**
- 🔍 **Debugging**: Al ver datos distintos, sabes inmediatamente de dónde viene la información
- 🧪 **Testing**: Puedes verificar que el flujo Mobile→Backend→Proxy funciona correctamente
- 🚀 **Demo**: Demostrar que el Proxy puede funcionar independientemente de la cátedra

**Ver código**: `src/main/java/um/prog2/service/MockCatedraService.java`

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

## ✅ Cumplimiento con Payloads de la Cátedra

El Proxy implementa **8 de 9** payloads especificados en el documento de la cátedra:

| Payload | Descripción | Estado | Endpoint Proxy |
|---------|-------------|--------|----------------|
| **Payload 1** | Registro de usuario | ❌ No implementado | N/A |
| **Payload 2** | Login de usuario | ✅ Implementado | `POST /api/users/login` |
| **Payload 3** | Eventos resumidos | ✅ Implementado | `GET /api/eventos/resumidos` |
| **Payload 4** | Eventos completos | ✅ Implementado | `GET /api/eventos` |
| **Payload 5** | Detalle de evento | ✅ Implementado | `GET /api/eventos/{id}` |
| **Payload 6** | Bloquear asientos | ✅ Implementado | `POST /api/eventos/bloquear-asientos` |
| **Payload 7** | Realizar venta | ✅ Implementado | `POST /api/ventas/realizar` |
| **Payload 8** | Listar ventas | ✅ Implementado | `GET /api/ventas` |
| **Payload 9** | Detalle de venta | ✅ Implementado | `GET /api/ventas/{id}` |

**Nota sobre Payload 1**: El registro de usuario (`/api/v1/agregar_usuario`) no está implementado porque:
1. Es una operación de una sola vez por alumno (ya realizada manualmente)
2. El Proxy ya tiene credenciales configuradas (`APP_CATEDRA_USERNAME`, `APP_CATEDRA_PASSWORD`)
3. Si se necesitara, puede agregarse fácilmente siguiendo el patrón de `UserProxyController`

### Verificación de Estructura de DTOs

Los DTOs del Proxy coinciden **exactamente** con los ejemplos JSON de la cátedra:

- ✅ **EventoResumenDTO** (Payload 3): `id`, `titulo`, `resumen`, `descripcion`, `fecha`, `precioEntrada`, `eventoTipo`
- ✅ **EventoDTO** (Payload 4): Incluye además `direccion`, `imagen`, `filaAsientos`, `columnAsientos`, `integrantes[]`
- ✅ **BloquearAsientosRequestDTO/ResponseDTO** (Payload 6): `eventoId`, `asientos[]` con `fila`, `columna`, `estado`
- ✅ **RealizarVentaRequestDTO/ResponseDTO** (Payload 7): `eventoId`, `fecha`, `precioVenta`, `asientos[]` con `persona`
- ✅ **VentaResumenDTO** (Payload 8): `eventoId`, `ventaId`, `fechaVenta`, `resultado`, `cantidadAsientos`
- ✅ **VentaDTO** (Payload 9): Incluye además lista completa de `asientos[]` vendidos

Ver documentación completa: **[PROXY-API.md](./PROXY-API.md)**

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
