# 📮 Guía de Colecciones Postman - Arquitectura Completa

Esta guía explica las **3 colecciones de Postman** que representan los 3 niveles de la arquitectura.

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Cátedra   │ ◄────── │    Proxy    │ ◄────── │   Backend   │ ◄────── │   Mobile    │
│ 192.168.x.x │         │ localhost:  │         │ localhost:  │         │   (App)     │
│    :8080    │         │    8080     │         │    8081     │         │             │
└─────────────┘         └─────────────┘         └─────────────┘         └─────────────┘
   (Servidor              (Intermediario)         (API Backend)           (Cliente)
    Original)             Consume Cátedra          Consume Proxy           Consume Backend
                          Expone a Backend         Expone a Mobile
```

---

## 📦 Colecciones Disponibles

### 1. **Cátedra → Proxy** (`1-Catedra-Proxy.postman_collection.json`)

**Descripción**: Endpoints que el **Proxy consume de la Cátedra**.

**Base URL**: `http://192.168.194.250:8080`

**Rol**: El Proxy actúa como **cliente** de la Cátedra.

**Endpoints**:
- `GET /api/endpoints/v1/eventos-resumidos` - Listar eventos resumidos
- `GET /api/endpoints/v1/eventos` - Listar eventos completos
- `GET /api/endpoints/v1/evento/{id}` - Detalle de evento
- `POST /api/endpoints/v1/bloquear-asientos` - Bloquear asientos
- `POST /api/endpoints/v1/realizar-venta` - Realizar venta
- `GET /api/endpoints/v1/listar-ventas` - Listar ventas
- `GET /api/endpoints/v1/listar-venta/{id}` - Detalle de venta
- `POST /api/authenticate` - Login

**Cuándo usar**: Para probar directamente la API de la Cátedra o verificar qué recibe el Proxy.

---

### 2. **Proxy → Backend** (`2-Proxy-Backend.postman_collection.json`)

**Descripción**: Endpoints que el **Backend consume del Proxy**.

**Base URL**: `http://localhost:8080`

**Rol**: El Proxy actúa como **servidor** para el Backend, intermediario con la Cátedra.

**Endpoints**:
- `GET /api/eventos/resumidos` - Listar eventos resumidos (Proxy → Cátedra)
- `GET /api/eventos` - Listar eventos completos (Proxy → Cátedra)
- `GET /api/eventos/{id}` - Detalle de evento (Proxy → Cátedra)
- `GET /api/eventos/{id}/asientos-estado` - Estado de asientos desde Redis (exclusivo Proxy)
- `POST /api/eventos/bloquear-asientos` - Bloquear asientos (Proxy → Cátedra)
- `POST /api/ventas/realizar` - Realizar venta (Proxy → Cátedra)
- `GET /api/ventas` - Listar ventas (Proxy → Cátedra)
- `GET /api/ventas/{id}` - Detalle de venta (Proxy → Cátedra)
- `POST /api/users/login` - Login (Proxy → Cátedra)
- `GET /actuator/health` - Health check del Proxy
- `GET /actuator/auth/status` - Estado del token JWT del Proxy
- `POST /actuator/auth/refresh` - Refrescar token JWT del Proxy

**Cuándo usar**: Para probar qué recibe el Backend del Proxy, o para debuggear problemas de comunicación Proxy-Backend.

---

### 3. **Backend → Mobile** (`Backend-API.postman_collection.json`)

**Descripción**: Endpoints que **Mobile consume del Backend**.

**Base URL**: `http://localhost:8081`

**Rol**: El Backend actúa como **servidor** para Mobile, lee de su BD local y consume el Proxy cuando es necesario.

**Endpoints**:

**Públicos (sin autenticación)**:
- `GET /api/eventos/resumidos` - Listar eventos resumidos (BD local, sin paginación)
- `GET /api/eventos/resumidos?page=0&size=20` - Listar eventos resumidos con paginación opcional
- `GET /api/eventos` - Listar eventos completos (BD local, sin paginación)
- `GET /api/eventos?page=0&size=20` - Listar eventos completos con paginación opcional
- `GET /api/eventos/{id}` - Detalle de evento (BD local)
- `GET /api/asientos/evento/{id}/mapa` - Mapa de asientos (Backend → Proxy → Redis)
- `GET /api/asientos/evento/{id}/disponible` - Verificar disponibilidad

**Con autenticación (requieren token JWT)**:
- `POST /api/authenticate` - Login
- `POST /api/register` - Registro de usuario
- `GET /api/account` - Obtener cuenta actual
- `POST /api/asientos/evento/{id}/bloquear` - Bloquear asientos (Backend → Proxy → Cátedra)
- `POST /api/ventas/evento/{id}/realizar` - Realizar venta (Backend → Proxy → Cátedra)
- `GET /api/ventas` - Mis ventas (BD local)
- `GET /api/ventas/{id}` - Detalle de venta (BD local)

**Cuándo usar**: Para probar los endpoints que Mobile usa, simular el comportamiento de la app móvil.

---

## 🔄 Flujo de Datos

### Ejemplo: Obtener Eventos

1. **Mobile** → `GET /api/eventos/resumidos` → **Backend** (localhost:8081)
   - O con paginación: `GET /api/eventos/resumidos?page=0&size=20`
2. **Backend** lee de su **BD local** (MySQL) y devuelve los eventos
3. Si la BD está vacía, el Backend puede sincronizar desde el Proxy

### Ejemplo: Bloquear Asientos

1. **Mobile** → `POST /api/asientos/evento/1/bloquear` → **Backend** (localhost:8081) [con token JWT]
2. **Backend** → `POST /api/eventos/bloquear-asientos` → **Proxy** (localhost:8080)
3. **Proxy** → `POST /api/endpoints/v1/bloquear-asientos` → **Cátedra** (192.168.194.250:8080)
4. **Cátedra** procesa y responde
5. **Proxy** recibe respuesta y la reenvía al **Backend**
6. **Backend** recibe respuesta y la reenvía a **Mobile**

### Ejemplo: Realizar Venta

1. **Mobile** → `POST /api/ventas/evento/1/realizar` → **Backend** (localhost:8081) [con token JWT]
2. **Backend** → `POST /api/ventas/realizar` → **Proxy** (localhost:8080)
3. **Proxy** → `POST /api/endpoints/v1/realizar-venta` → **Cátedra** (192.168.194.250:8080)
4. **Cátedra** procesa la venta y puede notificar vía **Kafka**
5. **Proxy** escucha Kafka y envía webhook → `POST /api/eventos/webhook` → **Backend**
6. **Backend** actualiza su BD local con la venta
7. **Backend** responde a **Mobile**

---

## 📋 Diferencias Clave

| Aspecto | Cátedra | Proxy | Backend |
|---------|---------|-------|---------|
| **URL Base** | `192.168.194.250:8080` | `localhost:8080` | `localhost:8081` |
| **Rol** | Servidor original | Intermediario | API para Mobile |
| **Fuente de Datos** | BD propia | Consume Cátedra | BD local + Proxy |
| **Endpoints** | `/api/endpoints/v1/*` | `/api/eventos/*`, `/api/ventas/*` | `/api/app/*`, `/api/mobile/*`, `/api/ventas/*` |
| **Autenticación** | JWT (alumno) | JWT automático (configurado) | JWT (usuario Mobile) |
| **Redis** | Usa Redis | Lee Redis de Cátedra | No usa Redis directamente |
| **Kafka** | Publica eventos | Consume Kafka | Recibe webhooks del Proxy |
| **BD Local** | No aplica | No aplica | MySQL (dev/prod) |

---

## 🎯 Cuándo Usar Cada Colección

### Usar **Cátedra → Proxy** cuando:
- Quieres probar directamente la API de la Cátedra
- Verificar qué datos devuelve la Cátedra
- Debuggear problemas en el Proxy (ver qué recibe de la Cátedra)
- Verificar conectividad con la Cátedra

### Usar **Proxy → Backend** cuando:
- Quieres probar qué recibe el Backend del Proxy
- Debuggear problemas de comunicación Proxy-Backend
- Verificar que el Proxy está funcionando correctamente
- Probar endpoints exclusivos del Proxy (como `/asientos-estado`)

### Usar **Backend → Mobile** cuando:
- Quieres simular el comportamiento de la app Mobile
- Probar los endpoints que Mobile realmente usa
- Verificar autenticación JWT
- Probar flujos completos de usuario (login → ver eventos → bloquear → comprar)

---

## 📥 Importar las Colecciones

1. Abre Postman
2. Click en **Import**
3. Selecciona los 3 archivos:
   - `1-Catedra-Proxy.postman_collection.json`
   - `2-Proxy-Backend.postman_collection.json`
   - `Backend-API.postman_collection.json` (renombrado a "3. Backend → Mobile")
4. Selecciona los 3 entornos:
   - `1-Catedra-Proxy.postman_environment.json`
   - `2-Proxy-Backend.postman_environment.json`
   - `Backend-API.postman_environment.json` (renombrado a "Backend → Mobile")

---

## 🔧 Configuración de Variables

### Cátedra → Proxy
- `catedra_base_url`: `http://192.168.194.250:8080`
- `catedra_username`: Usuario de la Cátedra (si aplica)
- `catedra_password`: Contraseña de la Cátedra (si aplica)

### Proxy → Backend
- `proxy_base_url`: `http://localhost:8080`
- `proxy_username`: Usuario para login (si aplica)
- `proxy_password`: Contraseña para login (si aplica)

### Backend → Mobile
- `base_url`: `http://localhost:8081`
- `username`: `admin` (o tu usuario)
- `password`: `admin` (o tu contraseña)
- `jwt_token`: Se llena automáticamente al hacer login

---

## 💡 Tips

1. **Orden de prueba recomendado**:
   - Primero: **Backend → Mobile** (lo que realmente usa Mobile)
   - Segundo: **Proxy → Backend** (si hay problemas, verificar qué recibe el Backend)
   - Tercero: **Cátedra → Proxy** (si hay problemas, verificar la fuente original)

2. **Para debuggear un problema**:
   - Empieza desde **Backend → Mobile** (el nivel más alto)
   - Si falla, prueba **Proxy → Backend** (nivel intermedio)
   - Si falla, prueba **Cátedra → Proxy** (nivel más bajo)

3. **Endpoints exclusivos**:
- Solo Proxy: `/api/eventos/{id}/asientos-estado` (lee Redis)
- Solo Backend: `/api/eventos/*` (lee BD local, paginación opcional)
- Solo Cátedra: `/api/endpoints/v1/*` (formato original)

---

## 📝 Notas Importantes

- **Cátedra** es el servidor original, no debe modificarse
- **Proxy** actúa como intermediario, maneja autenticación automática con la Cátedra
- **Backend** tiene su propia BD local que se sincroniza desde el Proxy/Cátedra
- **Mobile** solo se comunica con el Backend, nunca directamente con Proxy o Cátedra

---

¿Dudas? Revisa `PROXY-API.md` para más detalles sobre los endpoints del Proxy.
