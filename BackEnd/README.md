# 🎓 Backend - Sistema de Venta de Entradas a Eventos

> **Proyecto Final - Aplicaciones Distribuidas**  
> Backend desarrollado en Spring Boot (JHipster) que interactúa con el servicio de la Cátedra a través del Proxy

---

## 📋 Índice

1. [Descripción General](#descripción-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Funcionalidades Principales](#funcionalidades-principales)
4. [Tecnologías Utilizadas](#tecnologías-utilizadas)
5. [Configuración y Ejecución](#configuración-y-ejecución)
6. [Endpoints API](#endpoints-api)
7. [Integración con Servicios](#integración-con-servicios)
8. [Base de Datos](#base-de-datos)
9. [Documentación Relacionada](#documentación-relacionada)

---

## 🎯 Descripción General

Este backend es parte de un sistema distribuido de 4 componentes para la venta de entradas a eventos. Su función principal es:

- **Intermediar** entre el cliente móvil y el servicio de la Cátedra
- **Mantener sincronizada** una copia local de eventos desde la Cátedra
- **Gestionar** sesiones de usuario con Redis
- **Procesar** bloqueos de asientos y ventas
- **Persistir** información de ventas localmente

### 🎓 Contexto Académico

**⚠️ IMPORTANTE**: Este es un sistema de **UN SOLO USUARIO** (el alumno). Aunque la arquitectura soporta múltiples usuarios (JHipster lo provee), en la práctica cada alumno tiene su propio entorno aislado.

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE MÓVIL (KMP)                      │
│                    Puerto: N/A (Android/iOS)                │
└──────────────────────────┬──────────────────────────────────┘
                           │ JWT + REST
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  BACKEND DEL ALUMNO (Este Proyecto)         │
│                         Puerto: 8081                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   MySQL      │  │    Redis     │  │  Spring Boot │    │
│  │   :3306      │  │    Local     │  │   JHipster   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  PROXY DEL ALUMNO                           │
│                     Puerto: 8080                            │
│  - Accede a Redis de la Cátedra                           │
│  - Consume Kafka de la Cátedra                            │
│  - Notifica al Backend de cambios                         │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              SERVICIO DE LA CÁTEDRA                         │
│                192.168.194.250:8080                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │    Kafka     │  │    Redis     │  │   API REST   │    │
│  │    :9092     │  │    :6379     │  │    :8080     │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 🔗 Componentes del Sistema

#### 1. **Servicio de la Cátedra** (Externo - No modificable)
- **URL**: `http://192.168.194.250:8080`
- **Función**: Provee eventos, gestiona bloqueos y ventas
- **Kafka**: `192.168.194.250:9092` (Notificaciones de cambios)
- **Redis**: `192.168.194.250:6379` (Estado de asientos)

#### 2. **Proxy del Alumno** (Ver `Proxy.md`)
- **URL**: `http://localhost:8080`
- **Función**: Intermediario único con acceso a Kafka y Redis de la Cátedra
- **Responsabilidades**:
  - Consultar Redis de la Cátedra para estado de asientos
  - Suscribirse al tópico Kafka `eventos-actualizacion`
  - Notificar al Backend sobre cambios en eventos

#### 3. **Backend del Alumno** (Este Proyecto)
- **URL**: `http://localhost:8081`
- **Función**: API REST para el cliente móvil
- **Base de Datos**: MySQL (desarrollo: `:3306`, producción: `:3307`)
- **Caché**: Redis Local (sesiones y cache)

#### 4. **Cliente Móvil** (Ver `README-MOBILE.md`)
- **Tecnología**: Kotlin Multiplatform (KMP)
- **Función**: Interfaz gráfica del sistema
- **Comunica SOLO con**: Backend (puerto 8081)

---

## ⚙️ Funcionalidades Principales

### 🎫 1. Manejo de Eventos

#### Sincronización Automática
- **Al iniciar**: Descarga todos los eventos desde el Proxy
- **Cada hora**: Actualiza eventos automáticamente
- **Vía Kafka**: El Proxy notifica cambios en tiempo real

#### Tipos de Cambios
- ✅ Nuevos eventos agregados
- ⏰ Eventos expirados por tiempo
- ❌ Eventos cancelados
- 🔄 Modificaciones en datos de eventos

El Backend mantiene una **copia local sincronizada** en MySQL para responder rápidamente al móvil sin depender de la disponibilidad de la Cátedra.

---

### 🪑 2. Proceso de Selección de Eventos y Asientos

#### Flujo Completo:

```
1. Listar Eventos (Resumidos)
   ↓
2. Seleccionar Evento (Ver Detalles + Mapa de Asientos)
   ↓
3. Iniciar Sesión (Dura X minutos)
   ↓
4. Seleccionar Asientos (1-4 asientos)
   ↓
5. Bloquear Asientos (5 minutos de bloqueo)
   ↓
6. Cargar Datos (Nombre/Apellido por asiento)
   ↓
7. Confirmar Venta
   ↓
8. Recibir Confirmación
```

#### 📌 Características Importantes:

**Sesiones**:
- Duración: **30 minutos** de inactividad (parametrizable)
- Persistente entre clientes (si inicias en otro dispositivo, continúas donde quedaste)
- Se almacenan en **Redis Local**

**Bloqueo de Asientos**:
- Duración: **5 minutos**
- Máximo: **4 asientos** por sesión
- Se notifica a la Cátedra vía Proxy
- Al expirar, los asientos se liberan automáticamente

**Validaciones**:
- No se puede vender sin bloqueo activo
- No se puede bloquear fila/columna 0 o negativa
- No se puede exceder el límite de filas/columnas del evento

---

### 💳 3. Proceso de Venta

#### Flujo Interno:

1. **Pre-validación**: Verifica que los asientos estén bloqueados y no hayan expirado
2. **Notificación a Cátedra**: Envía solicitud de venta vía Proxy
3. **Persistencia Local**: Guarda la venta en MySQL
4. **Confirmación**: Espera respuesta de la Cátedra
5. **Manejo de Fallos**: Si falla, queda pendiente para reintentar

#### 📊 Datos de Venta:
```json
{
  "ventaId": 12345,
  "fechaVenta": "2025-12-13T18:30:00Z",
  "resultado": true,
  "descripcion": "Venta exitosa",
  "precioVenta": 10000.0,
  "evento": { ... },
  "asientos": [
    {
      "fila": 5,
      "columna": 10,
      "persona": "Juan Pérez"
    }
  ]
}
```

---

### 🔄 4. Sesiones Concurrentes

El sistema soporta múltiples sesiones activas simultáneamente:

- Si inicias sesión en otro dispositivo, **continúas donde quedaste**
- El estado se mantiene en **Redis Local**
- Las sesiones expiran a los **30 minutos de inactividad**
- Al cerrar sesión manualmente, **se invalidan todos los datos**

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 3.4.5 | Framework backend |
| **JHipster** | 8.x | Generador de aplicaciones |
| **MySQL** | 8.0 | Base de datos principal |
| **Redis** | 7.x | Sesiones y caché |
| **Maven** | 3.8+ | Gestión de dependencias |
| **Liquibase** | - | Migraciones de BD |
| **MapStruct** | - | Mapeo de DTOs |
| **JWT** | - | Autenticación |

---

## 🚀 Configuración y Ejecución

### Prerrequisitos

1. **Java 17** instalado
2. **Maven 3.8+** instalado
3. **Docker** instalado (para MySQL y Redis)
4. **Proxy** corriendo en `localhost:8080`

### 1. Configuración de Base de Datos

```powershell
# Iniciar MySQL (dev y prod) + Redis
cd C:\Users\totob\IdeaProjects\Final\BackEnd
docker-compose up -d
```

Esto levanta:
- **MySQL Dev**: `localhost:3306` (BD: `MicroservicesFinal`, user: `root`, pass: `root`)
- **MySQL Prod**: `localhost:3307` (BD: `MicroservicesFinal`, user: `root`, pass: `root`)
- **Redis Local**: `localhost:6379`

### 2. Configuración del Proxy

Asegúrate de que el Proxy esté configurado y corriendo:
- URL: `http://localhost:8080`
- Ver: `Proxy.md` para instrucciones

### 3. Ejecutar el Backend

#### Modo Desarrollo:
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Modo Producción:
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. Verificar

- Backend: http://localhost:8081
- Health Check: http://localhost:8081/management/health
- Swagger (si habilitado): http://localhost:8081/admin/docs

---

## 📡 Endpoints API

### 🔐 Autenticación (Públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/register` | Registro de usuario |
| `POST` | `/api/authenticate` | Login (retorna JWT) |
| `GET` | `/api/account` | Obtener usuario actual |

### 🎫 Eventos (Requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/eventos-consulta/resumidos` | Lista resumida de eventos |
| `GET` | `/api/eventos-consulta/{id}` | Detalle completo de un evento |
| `POST` | `/api/eventos-consulta/sync` | Forzar sincronización manual |

### 🪑 Asientos (Requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/asientos/evento/{id}/mapa` | Mapa de asientos (disponibilidad) |
| `POST` | `/api/asientos/evento/{id}/bloquear` | Bloquear asientos (5 min) |

### 💳 Ventas (Requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/ventas/evento/{id}/realizar` | Realizar venta |
| `GET` | `/api/ventas` | Mis ventas (historial) |
| `GET` | `/api/ventas/{id}` | Detalle de una venta |

**Ver**: `README-MOBILE.md` para ejemplos detallados de cada endpoint.

---

## 🔗 Integración con Servicios

### 1. Integración con Proxy

El Backend se comunica **ÚNICAMENTE** con el Proxy, nunca directamente con la Cátedra:

```java
// Configuración
app.proxy.base-url=http://localhost:8080
```

**Endpoints del Proxy usados**:
- `GET /api/eventos` - Obtener eventos completos
- `GET /api/asientos/evento/{id}/estado` - Estado de asientos (desde Redis)
- `POST /api/asientos/bloquear` - Bloquear asientos
- `POST /api/ventas/realizar` - Realizar venta

### 2. Notificaciones desde el Proxy (Webhooks)

El Proxy notifica al Backend sobre eventos de Kafka:

| Webhook | Cuándo se Invoca | Acción del Backend |
|---------|------------------|-------------------|
| `/api/webhooks/asientos-bloqueados` | Confirmación de bloqueo | Actualizar estado local |
| `/api/webhooks/venta-completada` | Confirmación de venta | Persistir venta en BD |
| `/api/webhooks/evento-cambiado` | Evento modificado en Cátedra | Actualizar evento en BD |

### 3. Kafka (Vía Proxy)

El Backend **NO** accede directamente a Kafka. El Proxy:
1. Se suscribe al tópico `eventos-actualizacion`
2. Lee mensajes con su `group_id` único
3. Notifica al Backend vía webhooks

### 4. Redis de la Cátedra (Vía Proxy)

El Backend **NO** accede directamente a Redis de la Cátedra. El Proxy:
1. Consulta el estado de asientos: `evento_{eventoId}`
2. Ejemplo de clave: `evento_1`
3. Formato:
```json
{
  "eventoId": 1,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "estado": "Vendido"
    },
    {
      "fila": 3,
      "columna": 10,
      "estado": "Bloqueado",
      "expira": "2025-12-13T22:05:00Z"
    }
  ]
}
```

**⚠️ IMPORTANTE**: Redis solo contiene asientos **Bloqueados** o **Vendidos**. Los asientos que NO aparecen se consideran **Disponibles**.

---

## 💾 Base de Datos

### Configuración

#### Desarrollo (`application-dev.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/MicroservicesFinal?allowPublicKeyRetrieval=true
    username: root
    password: root
```

#### Producción (`application-prod.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/MicroservicesFinal?allowPublicKeyRetrieval=true
    username: root
    password: root
```

### Esquema Principal

Generado automáticamente por JHipster y Liquibase:

```
Tablas:
- evento (id, titulo, resumen, descripcion, fecha, direccion, imagen, ...)
- evento_tipo (id, nombre, descripcion)
- integrante (id, nombre, rol, evento_id)
- venta (id, venta_id, fecha_venta, resultado, precio_venta, usuario_id, evento_id)
- asiento (id, fila, columna, persona, estado, venta_id)
- jhi_user (id, login, password_hash, email, activated, ...)
- jhi_authority (name)
```

### Migraciones

Las migraciones se ejecutan automáticamente al iniciar:
```
src/main/resources/config/liquibase/
├── master.xml
└── changelog/
    ├── 00000000000000_initial_schema.xml
    ├── 20251108154019_added_entity_Evento.xml
    ├── 20251108154020_added_entity_Venta.xml
    └── ...
```

### Ver Base de Datos

#### Con MySQL Workbench:
```
Host: localhost
Port: 3306 (dev) / 3307 (prod)
User: root
Password: root
Database: MicroservicesFinal
```

#### Con Docker:
```powershell
# Acceder al contenedor de MySQL Dev
docker exec -it microservices-mysql-dev mysql -uroot -proot MicroservicesFinal

# Ver tablas
SHOW TABLES;

# Ver eventos
SELECT id, titulo, fecha, precio_entrada FROM evento;
```

---

## 🧪 Testing

### Ejecutar Tests

```powershell
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=EventoSyncServiceTest
mvn test -Dtest=VentaResourceIT
mvn test -Dtest=AsientoServiceTest

# Tests de integración
mvn verify
```

### Tests Disponibles

- **Unit Tests**: Lógica de negocio (Services, Mappers)
- **Integration Tests**: Controllers REST (Resources)
- **Repository Tests**: Queries JPA
- **Service Tests**: Integración con Proxy

---

## 📖 Documentación Relacionada

| Archivo | Descripción |
|---------|-------------|
| **`README.md`** | Este archivo - Documentación general del Backend |
| **`README-MOBILE.md`** | Documentación de API para el equipo de Mobile |
| **`Proxy.md`** | Documentación del servicio Proxy |
| **`CORRECCIONES-FINALES.md`** | Historial de cambios y correcciones aplicadas |
| **`scripts/README.md`** | Scripts de utilidad para desarrollo |

---

## 🐛 Troubleshooting

### Problema: "Public Key Retrieval is not allowed"
**Solución**: Ya agregado `allowPublicKeyRetrieval=true` en las configuraciones.

### Problema: "Connection refused" al Proxy
**Verificar**:
1. El Proxy está corriendo: `http://localhost:8080/management/health`
2. La configuración es correcta en `application.yml`

### Problema: Backend no sincroniza eventos
**Verificar**:
1. El Proxy está corriendo
2. El Proxy tiene acceso a la Cátedra
3. Ver logs: `tail -f logs/spring.log`

### Problema: Redis no conecta
**Solución**:
```powershell
docker-compose up -d redis
```

---

## 📝 Notas Importantes

### 🎓 Contexto Académico

Este proyecto es para la materia **Aplicaciones Distribuidas**:
- Cada alumno tiene su **propio entorno aislado**
- Aunque soporta múltiples usuarios, en práctica es **un solo usuario** (el alumno)
- No se maneja pago real, solo simulación del proceso

### 🔐 Seguridad

- **JWT Token**: Válido por 24 horas
- **Sesiones**: Expiran a los 30 minutos de inactividad
- **Bloqueos**: Expiran a los 5 minutos

### ⚠️ Limitaciones Conocidas

1. **Conflictos de venta**: Si dos personas compran el mismo asiento simultáneamente, el conflicto no se resuelve en esta versión
2. **Reintentos**: Las ventas fallidas quedan pendientes para reintentar
3. **Tamaño de eventos**: Los eventos tienen columnas reducidas para facilitar la visualización en móvil

---

## 👥 Autores

**Alumno**: [Tu Nombre]  
**Materia**: Aplicaciones Distribuidas  
**Año**: 2025

---

**Desarrollado con**: Spring Boot 3.4.5 | JHipster 8.x | MySQL 8.0 | Redis 7.x

**Última actualización**: 2025-12-13

