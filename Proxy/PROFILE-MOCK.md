# Profile Mock - Simular Servicios de Cátedra

## 🎯 Objetivo

El **Profile Mock** permite ejecutar el Proxy **sin conexión a los servicios de la Cátedra** (API HTTP, Kafka, Redis), simulando datos inventados para probar el flujo completo **Mobile → Backend → Proxy**.

## ✅ ¿Cuándo usar el Profile Mock?

- ✅ Cuando los **servicios de la cátedra están caídos**
- ✅ Para **desarrollo local** sin depender de infraestructura externa
- ✅ Para **probar el flujo completo** entre Mobile, Backend y Proxy
- ✅ Para **demos** con datos controlados y consistentes

## ❌ ¿Cuándo NO usar el Profile Mock?

- ❌ Para **validar integración real** con los servicios de la cátedra
- ❌ En **producción**
- ❌ Para **probar escenarios de error reales** (timeouts, errores HTTP, etc.)

---

## 🚀 Cómo Activar el Profile Mock

### Opción 1: Con Maven (desarrollo)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

### Opción 2: Con JAR empaquetado

```bash
mvn clean package -DskipTests
java -jar target/proxy-1.0-SNAPSHOT.jar --spring.profiles.active=mock
```

### Opción 3: Variable de entorno

```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="mock"
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=mock
mvn spring-boot:run
```

---

## 📊 Datos Simulados

El **MockCatedraService** devuelve datos inventados consistentes:

### 🎭 Eventos (5 eventos con diferentes tamaños de sala)

| ID | Título | Tipo | Fecha | Precio | Sala (Fila x Columna) |
|----|--------|------|-------|--------|-----------------------|
| 1 | Conferencia Tech 2025 | Conferencia | 2025-12-20 | $2,500 | 10x8 (80 asientos) |
| 2 | Concierto Rock Nacional | Concierto | 2025-12-22 | $4,500 | 15x12 (180 asientos) |
| 3 | Obra de Teatro Clásica | Teatro | 2025-12-25 | $3,000 | 8x10 (80 asientos) |
| 4 | Stand-Up Comedy Night | Comedia | 2025-12-28 | $1,800 | 6x6 (36 asientos) |
| 5 | Festival de Cine Indie | Cine | 2025-12-30 | $2,000 | 12x10 (120 asientos) |

### 🎫 Asientos

- Los asientos se gestionan **en memoria** dentro del mock
- Al bloquear/vender, se actualizan en un mapa interno
- El endpoint `/api/eventos/{id}/asientos-estado` devuelve asientos bloqueados/vendidos

### 💰 Ventas

- Las ventas se guardan en una lista en memoria
- Cada venta tiene un ID incremental (1, 2, 3...)
- Se pueden listar con `/api/ventas` y consultar con `/api/ventas/{id}`

---

## 🔄 Flujo Síncrono y Asíncrono Simulado

El mock simula **ambos canales de notificación**:

### 1️⃣ Respuesta Síncrona (HTTP)
El endpoint devuelve respuesta inmediata con datos mock:

```json
POST /api/eventos/bloquear-asientos
→ 200 OK { "resultado": true, "eventoId": 1, ... }
```

### 2️⃣ Webhook Asíncrono (simula Kafka)
Después de un **delay random (1-5 segundos)**, el mock envía un webhook al Backend:

```
[MockCatedraService] --delay random--> [Webhook Backend]
  POST http://localhost:8081/api/webhooks/evento-cambio
  Body: { "topic": "ASIENTOS_BLOQUEADOS", "payload": "..." }
```

**Nota**: El Backend debe estar corriendo en `http://localhost:8081` para recibir los webhooks.

---

## 🧪 Endpoints Disponibles en Mock

Todos los endpoints del Proxy funcionan normalmente:

### Eventos
- `GET /api/eventos/resumidos` → Lista de 5 eventos resumidos
- `GET /api/eventos` → Lista de 5 eventos completos
- `GET /api/eventos/{id}` → Detalle de un evento (1-5)
- `GET /api/eventos/{id}/asientos-estado` → Estado de asientos desde memoria
- `POST /api/eventos/bloquear-asientos` → Bloquea asientos (+ webhook async)

### Ventas
- `POST /api/ventas/realizar` → Realiza venta (+ webhook async)
- `GET /api/ventas` → Lista ventas realizadas
- `GET /api/ventas/{id}` → Detalle de una venta

### Health Check
- `GET /actuator/health` → Estado del servicio

---

## ✅ Verificar que el Mock está Activo

Después de ejecutar el Proxy con el profile mock, verifica que esté funcionando correctamente:

### 1. Verificar el profile activo en los logs de inicio

```
The following 1 profile is active: "mock"
```

Si ves `"default"` o `"dev"`, el profile mock NO está activo.

### 2. Verificar que MockCatedraService está cargado

Busca en los logs:
```
Bean 'mockCatedraService' of type [um.prog2.service.MockCatedraService] is not eligible for getting processed by all BeanPostProcessors
```

### 3. Health Check (debe estar UP sin Redis/Kafka)

```bash
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

**NO debe aparecer**: `"redis": {"status": "DOWN"}` ni `"kafka": {"status": "DOWN"}`

### 4. Probar un endpoint mock

```bash
curl http://localhost:8080/api/eventos/resumidos
```

**Debe devolver 5 eventos mock** (no datos reales de la cátedra).

**En los logs debe aparecer:**
```
Usando MockCatedraService para eventos resumidos
MockCatedraService: devolviendo eventos resumidos mock
```

---

## 📋 Ejemplo de Uso Completo

### 1. Levantar Backend (puerto 8081)
```bash
cd ../BackEnd
mvn spring-boot:run
```

### 2. Levantar Proxy con Mock (puerto 8080)
```bash
cd ../Proxy
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

### 3. Probar endpoints desde Mobile o Postman

#### 3.1. Listar eventos
```bash
curl http://localhost:8080/api/eventos/resumidos
```

#### 3.2. Ver detalle de evento
```bash
curl http://localhost:8080/api/eventos/1
```

#### 3.3. Ver estado de asientos (inicialmente vacío)
```bash
curl http://localhost:8080/api/eventos/1/asientos-estado
```

#### 3.4. Bloquear asientos
```bash
curl -X POST http://localhost:8080/api/eventos/bloquear-asientos \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [
      {"fila": 1, "columna": 1},
      {"fila": 1, "columna": 2}
    ]
  }'
```

**Respuesta inmediata** (síncrona):
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados exitosamente",
  "eventoId": 1,
  "asientos": [
    {"fila": 1, "columna": 1, "estado": "Bloqueado"},
    {"fila": 1, "columna": 2, "estado": "Bloqueado"}
  ]
}
```

**Después de 1-3 segundos**, el Backend recibe un webhook en `/api/webhooks/evento-cambio`:
```json
{
  "timestamp": "2025-12-13T19:30:00Z",
  "topic": "ASIENTOS_BLOQUEADOS",
  "partition": 0,
  "offset": 1234,
  "key": "evento-1",
  "payload": "{\"eventoId\":1,\"asientos\":[...],\"bloqueadoHasta\":\"...\"}"
}
```

#### 3.5. Realizar venta
```bash
curl -X POST http://localhost:8080/api/ventas/realizar \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [
      {"fila": 1, "columna": 1, "persona": "Juan Pérez"},
      {"fila": 1, "columna": 2, "persona": "María García"}
    ]
  }'
```

**Respuesta inmediata**:
```json
{
  "eventoId": 1,
  "ventaId": 1,
  "fechaVenta": "2025-12-13T19:35:00Z",
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "precioVenta": 5000.00,
  "asientos": [
    {"fila": 1, "columna": 1, "persona": "Juan Pérez", "estado": "Ocupado"},
    {"fila": 1, "columna": 2, "persona": "María García", "estado": "Ocupado"}
  ]
}
```

**Después de 2-5 segundos**, webhook al Backend:
```json
{
  "topic": "VENTA_COMPLETADA",
  "payload": "{\"ventaId\":1,\"eventoId\":1,\"asientos\":[...],\"montoTotal\":5000.00}"
}
```

#### 3.6. Ver estado actualizado de asientos
```bash
curl http://localhost:8080/api/eventos/1/asientos-estado
```

```json
[
  {"fila": 1, "columna": 1, "estado": "Vendido"},
  {"fila": 1, "columna": 2, "estado": "Vendido"}
]
```

---

## ⚙️ Configuración del Profile Mock

El archivo `application-mock.properties` contiene:

```properties
# Backend (para webhooks)
app.backend.base-url=http://localhost:8081
app.backend.webhook-path=/api/webhooks/evento-cambio

# Cátedra - Credenciales NO configuradas (evita conexión de AuthTokenService)
app.catedra.username=
app.catedra.password=

# Kafka - DESHABILITADO
spring.kafka.enabled=false

# Redis - DESHABILITADO (excluye autoconfiguración)
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
management.health.redis.enabled=false

# Scheduling - DESHABILITADO (evita renovación automática de token)
spring.task.scheduling.enabled=false

# Logging
logging.level.um.prog2.service.MockCatedraService=DEBUG
```

**Importante**: 
- Asegúrate de que el Backend esté corriendo en el puerto 8081 para recibir los webhooks
- Redis, Kafka y AuthTokenService están completamente deshabilitados
- No habrá renovación automática de token JWT (no es necesaria en modo mock)

---

## 🔍 Logs de Depuración

Con el profile mock activo, verás logs como:

```
MockCatedraService: devolviendo eventos resumidos mock
MockCatedraService: bloqueando 2 asientos para evento 1 mock
MockCatedraService: webhook ASIENTOS_BLOQUEADOS enviado para evento 1
MockCatedraService: realizando venta de 2 asientos para evento 1 mock
MockCatedraService: webhook VENTA_COMPLETADA enviado para venta 1
```

---

## 🔄 Cambiar de Mock a Real

Para volver a conectarte a los servicios reales de la cátedra:

### 1. Detener el Proxy con Mock
```bash
Ctrl + C
```

### 2. Ejecutar sin el profile mock
```bash
mvn spring-boot:run
```

O explícitamente con el profile por defecto:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📝 Limitaciones del Mock

- ❌ **No valida reglas de negocio reales** (ej: asientos ya ocupados, evento lleno)
- ❌ **No simula errores HTTP** (401, 500, timeout)
- ❌ **No persiste datos** (se pierden al reiniciar)
- ❌ **Webhooks requieren Backend activo** en localhost:8081
- ❌ **No valida tokens JWT reales**

---

## 🎯 Resumen

| Característica | Mock | Real |
|----------------|------|------|
| Requiere servicios de cátedra | ❌ No | ✅ Sí |
| Kafka activo | ❌ No | ✅ Sí |
| Redis activo | ❌ No | ✅ Sí |
| Datos | 🎭 Inventados | 📡 Reales |
| Webhooks asíncronos | ✅ Simulados | ✅ Reales |
| Ideal para | 🧪 Desarrollo/Demo | 🚀 Producción |

---

## 🆘 Troubleshooting

### Error: "AuthTokenService: NO SE PUEDE CONECTAR al servidor de la cátedra"

**Problema**: El AuthTokenService está intentando obtener/renovar el token JWT cuando debería estar deshabilitado en modo mock.

**Logs típicos:**
```
AuthTokenService: ejecutando renovación programada del token JWT
AuthTokenService: solicitando nuevo token a http://localhost:9999/api/authenticate
ERROR AuthTokenService: NO SE PUEDE CONECTAR al servidor de la cátedra
```

**Solución**: Verifica que `application-mock.properties` tenga:
```properties
# Credenciales vacías = AuthTokenService no intenta conectarse
app.catedra.username=
app.catedra.password=

# Deshabilita la renovación automática programada
spring.task.scheduling.enabled=false
```

Si el problema persiste, recompila:
```bash
mvn clean compile
mvn spring-boot:run "-Dspring-boot.run.profiles=mock"
```

### Error: "Redis health check failed" o "Unable to connect to Redis"

**Problema**: El profile mock está intentando conectarse a Redis cuando debería estar deshabilitado.

**Solución**: Verifica que `application-mock.properties` tenga las siguientes propiedades:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
management.health.redis.enabled=false
```

Si el problema persiste, recompila:
```bash
mvn clean compile
mvn spring-boot:run "-Dspring-boot.run.profiles=mock"
```

### El Backend no recibe webhooks

**Problema**: Los logs muestran "webhook enviado" pero el Backend no los recibe.

**Solución**:
1. Verifica que el Backend esté corriendo: `curl http://localhost:8081/actuator/health`
2. Verifica que el endpoint webhook esté implementado: `POST /api/webhooks/evento-cambio`
3. Revisa los logs del Backend para errores de parsing

### Los tests de integración siguen usando datos reales

**Problema**: Los tests de integración (`ProxyIT`) intentan conectarse a la cátedra.

**Solución**: Los tests de integración NO usan el profile mock por diseño. Desactívalos si no hay conectividad:
```bash
mvn test  # Solo tests unitarios (no requieren cátedra)
```

### El Proxy no usa el MockCatedraService

**Problema**: Los logs no muestran "Usando MockCatedraService" y parece que intenta conectarse a servicios reales.

**Solución**: Verifica que el profile esté activo correctamente:
```bash
# En los logs de inicio debe aparecer:
# "The following 1 profile is active: "mock""

# Si no aparece, revisa el comando:
mvn spring-boot:run "-Dspring-boot.run.profiles=mock"
# Nota: Las comillas son importantes en PowerShell
```

---

## 📚 Ver También

- [README.md](README.md) - Documentación principal del Proxy
- [VERIFICACION-TOKEN.md](VERIFICACION-TOKEN.md) - Sistema de autenticación JWT automática
- `application-mock.properties` - Configuración del profile mock
- `MockCatedraService.java` - Implementación del servicio mock

