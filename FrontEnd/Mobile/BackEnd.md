# 📱 Backend API - Documentación para Mobile

> **API REST para compra de entradas a eventos**  
> Base URL: `http://localhost:8081`  
> Autenticación: JWT Bearer Token

---

## 📋 Tabla de Contenidos

1. [Arquitectura General](#arquitectura-general)
2. [Autenticación](#autenticación)
3. [Flujo Completo de Compra](#flujo-completo-de-compra)
4. [Endpoints Disponibles](#endpoints-disponibles)
5. [Modelos de Datos (DTOs)](#modelos-de-datos-dtos)
6. [Estados y Validaciones](#estados-y-validaciones)
7. [Manejo de Errores](#manejo-de-errores)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 🏗️ Arquitectura General

```
┌─────────────────┐
│  MOBILE (KMP)   │
└────────┬────────┘
         │ HTTP REST + JWT
         ↓
┌─────────────────┐
│  BACKEND :8081  │  ← Estás aquí
│  (Spring Boot)  │
└────────┬────────┘
         │ HTTP REST
         ↓
┌─────────────────┐
│   PROXY :8080   │
└────────┬────────┘
         │ HTTP + Kafka + Redis
         ↓
┌─────────────────┐
│  CÁTEDRA :8080  │
│  Kafka :9092    │
│  Redis :6379    │
└─────────────────┘
```

### ⚠️ Importante para Mobile

1. **NUNCA** llames directamente al Proxy o a la Cátedra
2. **SIEMPRE** usa los endpoints del Backend (puerto 8081)
3. **Todas las respuestas** son JSON
4. **Todas las fechas** están en formato ISO-8601 (UTC)
5. **Usuario único**: La aplicación es para un solo usuario (el alumno)

---

## 🔐 Autenticación

### 1. Login

**Endpoint**: `POST /api/authenticate`

**Request Body**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response** (200 OK):
```json
{
  "id_token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Uso del Token**:
Todas las llamadas subsiguientes deben incluir el header:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expiración**: 
- Token válido por 24 horas
- Si recibes `401 Unauthorized`, debes hacer login nuevamente

---

## 🛒 Flujo Completo de Compra

### Paso 1: Listar Eventos

```
GET /api/eventos-consulta/resumidos
```

**Response**:
```json
[
  {
    "id": 1,
    "titulo": "Concierto Rock",
    "resumen": "Banda local de rock",
    "fecha": "2025-12-15T20:00:00Z",
    "imagen": "https://...",
    "eventoTipo": {
      "id": 1,
      "nombre": "Música"
    }
  }
]
```

**UI**: Mostrar lista de tarjetas con título, fecha, imagen

---

### Paso 2: Ver Detalle del Evento

```
GET /api/eventos-consulta/{eventoId}
```

**Response**:
```json
{
  "id": 1,
  "titulo": "Concierto Rock",
  "resumen": "Banda local de rock",
  "descripcion": "Descripción completa del evento...",
  "fecha": "2025-12-15T20:00:00Z",
  "direccion": "Av. Corrientes 1234",
  "imagen": "https://...",
  "filaAsientos": 10,
  "columnAsientos": 15,
  "eventoTipo": {
    "id": 1,
    "nombre": "Música",
    "descripcion": "Eventos musicales"
  },
  "integrantes": [
    {
      "id": 1,
      "nombre": "Juan Pérez",
      "rol": "Vocalista"
    }
  ]
}
```

**UI**: Mostrar todos los detalles + botón "Comprar Entradas"

---

### Paso 3: Ver Mapa de Asientos

```
GET /api/asientos/evento/{eventoId}/mapa
```

**Response**:
```json
{
  "eventoId": 1,
  "totalFilas": 10,
  "totalColumnas": 15,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Bloqueado",
      "expira": "2025-12-11T14:35:00Z"
    },
    {
      "fila": 2,
      "columna": 5,
      "estado": "Vendido"
    }
  ]
}
```

**⚠️ Lógica de Estados**:

| Si el asiento... | Entonces... | Color sugerido |
|------------------|-------------|----------------|
| NO está en la lista | DISPONIBLE | 🟢 Verde |
| `estado: "Vendido"` | OCUPADO | 🔴 Rojo |
| `estado: "Bloqueado"` Y `expira > ahora` | BLOQUEADO (por otro) | 🟡 Amarillo |
| `estado: "Bloqueado"` Y `expira <= ahora` | DISPONIBLE (bloqueo expirado) | 🟢 Verde |
| Seleccionado por mí | SELECCIONADO | 🔵 Azul |

**UI**: 
- Mostrar grilla de `totalFilas` × `totalColumnas`
- Permitir seleccionar hasta 4 asientos disponibles
- Mostrar contador: "X/4 asientos seleccionados"
- Botón "Continuar" habilitado solo si seleccionó al menos 1

---

### Paso 4: Bloquear Asientos

```
POST /api/asientos/evento/{eventoId}/bloquear
```

**Request Body**:
```json
[
  { "fila": 1, "columna": 3 },
  { "fila": 1, "columna": 4 }
]
```

**Response** (200 OK):
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados correctamente",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Bloqueado",
      "mensaje": "OK"
    },
    {
      "fila": 1,
      "columna": 4,
      "estado": "Bloqueado",
      "mensaje": "OK"
    }
  ]
}
```

**Response Error** (500):
```json
{
  "resultado": false,
  "descripcion": "El asiento (1,3) ya está ocupado",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Ocupado",
      "mensaje": "No disponible"
    }
  ]
}
```

**⚠️ Duración del Bloqueo**: 
- Los asientos quedan bloqueados por **5 MINUTOS**
- Debes completar la venta antes de que expire
- Si expira, debes bloquear nuevamente

**UI**: 
- Mostrar timer: "Tiempo restante: 4:32"
- Si expira, volver al mapa de asientos

---

### Paso 5: Cargar Nombres

**UI**:
- Mostrar lista de asientos bloqueados
- Cada asiento tiene un campo de texto: "Nombre completo"
- Ejemplo:
  ```
  Fila 1, Asiento 3
  [__________________________]
  
  Fila 1, Asiento 4
  [__________________________]
  ```
- Botón "Volver" (permite cambiar asientos)
- Botón "Continuar" (habilitar cuando todos tengan nombre)

**Validación Local**:
- Nombre no puede estar vacío
- Mínimo 3 caracteres
- Solo letras y espacios

---

### Paso 6: Realizar Venta

```
POST /api/ventas/evento/{eventoId}/realizar
```

**Request Body**:
```json
[
  { "fila": 1, "columna": 3 },
  { "fila": 1, "columna": 4 }
]
```

**⚠️ IMPORTANTE**: 
- Los nombres NO se envían en este endpoint
- Los asientos DEBEN estar bloqueados previamente
- Si el bloqueo expiró, la venta fallará

**Response** (200 OK):
```json
{
  "eventoId": 1,
  "ventaId": 123,
  "fechaVenta": "2025-12-11T14:30:00Z",
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Vendido",
      "mensaje": "OK"
    },
    {
      "fila": 1,
      "columna": 4,
      "estado": "Vendido",
      "mensaje": "OK"
    }
  ],
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "precioVenta": 2500.00
}
```

**Response Error** (500):
```json
{
  "resultado": false,
  "descripcion": "Los asientos no están bloqueados o el bloqueo expiró",
  "eventoId": 1,
  "asientos": [],
  "precioVenta": 0.00
}
```

**UI**:
- Mostrar mensaje de éxito
- Mostrar resumen de la compra
- Botón "Ver Mis Compras" → `/api/ventas`
- Botón "Volver al Inicio" → `/api/eventos-consulta/resumidos`

---

### Paso 7: Ver Mis Compras

```
GET /api/ventas
```

**Response**:
```json
[
  {
    "id": 1,
    "ventaId": 123,
    "fechaVenta": "2025-12-11T14:30:00Z",
    "resultado": true,
    "descripcion": "Venta realizada exitosamente",
    "precioVenta": 2500.00,
    "usuario": {
      "id": 1,
      "login": "admin"
    },
    "evento": {
      "id": 1,
      "titulo": "Concierto Rock",
      "fecha": "2025-12-15T20:00:00Z"
    }
  }
]
```

**UI**: 
- Mostrar lista de compras realizadas
- Cada item con: evento, fecha de compra, precio
- Click → ver detalle

---

### Paso 8: Ver Detalle de Compra

```
GET /api/ventas/{ventaId}
```

**Response**:
```json
{
  "id": 1,
  "ventaId": 123,
  "fechaVenta": "2025-12-11T14:30:00Z",
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "precioVenta": 2500.00,
  "usuario": {
    "id": 1,
    "login": "admin"
  },
  "evento": {
    "id": 1,
    "titulo": "Concierto Rock",
    "fecha": "2025-12-15T20:00:00Z",
    "direccion": "Av. Corrientes 1234"
  }
}
```

**UI**:
- Mostrar todos los detalles de la compra
- Opción para generar QR code (opcional)

---

## 📡 Endpoints Disponibles

### 🎫 Eventos

#### GET `/api/eventos-consulta/resumidos`
Lista resumida de eventos activos.

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "titulo": "string",
    "resumen": "string",
    "fecha": "2025-12-15T20:00:00Z",
    "imagen": "string",
    "eventoTipo": { "id": 1, "nombre": "string" }
  }
]
```

---

#### GET `/api/eventos-consulta/{id}`
Detalle completo de un evento.

**Path Parameters**:
- `id` (Long): ID del evento

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "titulo": "string",
  "resumen": "string",
  "descripcion": "string",
  "fecha": "2025-12-15T20:00:00Z",
  "direccion": "string",
  "imagen": "string",
  "filaAsientos": 10,
  "columnAsientos": 15,
  "eventoTipo": {
    "id": 1,
    "nombre": "string",
    "descripcion": "string"
  },
  "integrantes": [
    {
      "id": 1,
      "nombre": "string",
      "rol": "string"
    }
  ]
}
```

**Response**: `404 Not Found`
```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Evento no encontrado"
}
```

---

### 💺 Asientos

#### GET `/api/asientos/evento/{eventoId}/mapa`
Obtiene el mapa de asientos de un evento.

**Path Parameters**:
- `eventoId` (Long): ID del evento

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
{
  "eventoId": 1,
  "totalFilas": 10,
  "totalColumnas": 15,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Bloqueado",
      "expira": "2025-12-11T14:35:00Z"
    }
  ]
}
```

**⚠️ Nota**: 
- `asientos` solo contiene bloqueados/vendidos
- Si un asiento NO está en la lista, está **disponible**
- Campo `expira` solo presente si `estado: "Bloqueado"`

---

#### POST `/api/asientos/evento/{eventoId}/bloquear`
Bloquea asientos temporalmente (5 minutos).

**Path Parameters**:
- `eventoId` (Long): ID del evento

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
[
  { "fila": 1, "columna": 3 },
  { "fila": 1, "columna": 4 }
]
```

**Validaciones**:
- Mínimo 1 asiento
- Máximo 4 asientos
- `fila` y `columna` > 0
- `fila` <= `filaAsientos` del evento
- `columna` <= `columnAsientos` del evento

**Response**: `200 OK` (bloqueo exitoso)
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados correctamente",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Bloqueado",
      "mensaje": "OK"
    }
  ]
}
```

**Response**: `400 Bad Request` (validación fallida)
```json
{
  "resultado": false,
  "descripcion": "Asientos inválidos. Verifica fila/columna y que no excedan el límite (máx 4)."
}
```

**Response**: `500 Internal Server Error` (asientos no disponibles)
```json
{
  "resultado": false,
  "descripcion": "El asiento (1,3) ya está ocupado",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Ocupado",
      "mensaje": "No disponible"
    }
  ]
}
```

---

#### GET `/api/asientos/evento/{eventoId}/disponible`
Verifica si un asiento específico está disponible.

**Path Parameters**:
- `eventoId` (Long): ID del evento

**Query Parameters**:
- `fila` (Integer): Número de fila
- `columna` (Integer): Número de columna

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
true
```

**Uso**: Para verificar disponibilidad antes de intentar bloquear

---

### 🛍️ Ventas

#### POST `/api/ventas/evento/{eventoId}/realizar`
Realiza la venta de asientos previamente bloqueados.

**Path Parameters**:
- `eventoId` (Long): ID del evento

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
[
  { "fila": 1, "columna": 3 },
  { "fila": 1, "columna": 4 }
]
```

**⚠️ Precondiciones**:
- Los asientos DEBEN estar bloqueados previamente
- El bloqueo NO debe haber expirado (< 5 minutos)
- Mismo usuario que hizo el bloqueo (JWT)

**Response**: `200 OK` (venta exitosa)
```json
{
  "eventoId": 1,
  "ventaId": 123,
  "fechaVenta": "2025-12-11T14:30:00Z",
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Vendido",
      "mensaje": "OK"
    }
  ],
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "precioVenta": 2500.00
}
```

**Response**: `400 Bad Request` (validación fallida)
```json
{
  "resultado": false,
  "descripcion": "Debe seleccionar al menos un asiento"
}
```

**Response**: `500 Internal Server Error` (venta fallida)
```json
{
  "resultado": false,
  "descripcion": "Los asientos no están bloqueados o el bloqueo expiró",
  "precioVenta": 0.00
}
```

---

#### GET `/api/ventas`
Lista todas las compras del usuario autenticado.

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "ventaId": 123,
    "fechaVenta": "2025-12-11T14:30:00Z",
    "resultado": true,
    "descripcion": "Venta realizada exitosamente",
    "precioVenta": 2500.00,
    "usuario": {
      "id": 1,
      "login": "admin"
    },
    "evento": {
      "id": 1,
      "titulo": "Concierto Rock"
    }
  }
]
```

---

#### GET `/api/ventas/{id}`
Obtiene el detalle de una venta específica.

**Path Parameters**:
- `id` (Long): ID de la venta

**Headers**:
```
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "ventaId": 123,
  "fechaVenta": "2025-12-11T14:30:00Z",
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "precioVenta": 2500.00,
  "usuario": {
    "id": 1,
    "login": "admin"
  },
  "evento": {
    "id": 1,
    "titulo": "Concierto Rock",
    "fecha": "2025-12-15T20:00:00Z",
    "direccion": "Av. Corrientes 1234"
  }
}
```

**Response**: `404 Not Found`
```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Venta no encontrada"
}
```

---

## 📦 Modelos de Datos (DTOs)

### EventoResumenDTO
```typescript
{
  id: number;
  titulo: string;
  resumen: string;
  fecha: string; // ISO-8601 UTC
  imagen: string; // URL
  eventoTipo: {
    id: number;
    nombre: string;
  };
}
```

---

### EventoDetalleDTO
```typescript
{
  id: number;
  titulo: string;
  resumen: string;
  descripcion: string;
  fecha: string; // ISO-8601 UTC
  direccion: string;
  imagen: string; // URL
  filaAsientos: number;
  columnAsientos: number;
  eventoTipo: {
    id: number;
    nombre: string;
    descripcion: string;
  };
  integrantes: Array<{
    id: number;
    nombre: string;
    rol: string;
  }>;
}
```

---

### MapaAsientosDTO
```typescript
{
  eventoId: number;
  totalFilas: number;
  totalColumnas: number;
  asientos: Array<{
    fila: number;
    columna: number;
    estado: "Bloqueado" | "Vendido";
    expira?: string; // ISO-8601 UTC, solo si estado === "Bloqueado"
  }>;
}
```

---

### AsientoSeleccionDTO
```typescript
{
  fila: number; // >= 1
  columna: number; // >= 1
}
```

---

### BloquearAsientosResponseDTO
```typescript
{
  resultado: boolean;
  descripcion: string;
  eventoId: number;
  asientos: Array<{
    fila: number;
    columna: number;
    estado: "Bloqueado" | "Ocupado" | "NoDisponible";
    mensaje: string;
  }>;
}
```

---

### RealizarVentaResponseDTO
```typescript
{
  eventoId: number;
  ventaId?: number; // null si resultado === false
  fechaVenta: string; // ISO-8601 UTC
  asientos: Array<{
    fila: number;
    columna: number;
    estado: "Vendido" | "NoDisponible";
    mensaje: string;
  }>;
  resultado: boolean;
  descripcion: string;
  precioVenta: number; // Decimal con 2 decimales
}
```

---

### VentaDTO
```typescript
{
  id: number;
  ventaId: number;
  fechaVenta: string; // ISO-8601 UTC
  resultado: boolean;
  descripcion: string;
  precioVenta: number;
  usuario: {
    id: number;
    login: string;
  };
  evento: {
    id: number;
    titulo: string;
    fecha: string; // ISO-8601 UTC
    direccion?: string;
  };
}
```

---

## ⚠️ Estados y Validaciones

### Estados de Asientos

| Estado | Descripción | Color UI |
|--------|-------------|----------|
| **Disponible** | No está en la lista de Redis | 🟢 Verde |
| **Bloqueado** | `estado: "Bloqueado"` y `expira > now()` | 🟡 Amarillo |
| **Vendido** | `estado: "Vendido"` | 🔴 Rojo |
| **Expirado** | `estado: "Bloqueado"` y `expira <= now()` | 🟢 Verde (disponible) |
| **Seleccionado** | Marcado por el usuario actual | 🔵 Azul |

### Reglas de Negocio

#### Bloqueo de Asientos
- ✅ Mínimo: 1 asiento
- ✅ Máximo: 4 asientos por compra
- ✅ Duración: 5 minutos
- ✅ Solo asientos disponibles
- ✅ Fila/columna dentro de los límites del evento
- ❌ No se puede bloquear el mismo asiento 2 veces
- ❌ No se puede bloquear asientos vendidos

#### Venta
- ✅ Asientos deben estar bloqueados previamente
- ✅ Bloqueo NO debe haber expirado
- ✅ Mismo usuario que hizo el bloqueo
- ❌ No se puede vender sin bloqueo previo
- ❌ No se puede vender con bloqueo expirado

### Tiempos y Expiración

| Acción | Duración |
|--------|----------|
| Token JWT | 24 horas |
| Bloqueo de asientos | 5 minutos |
| Sesión de usuario | 30 minutos de inactividad |

---

## 🚨 Manejo de Errores

### Códigos de Estado HTTP

| Código | Significado | Acción Recomendada |
|--------|-------------|-------------------|
| `200` | OK | Procesar respuesta |
| `400` | Bad Request | Mostrar `descripcion` al usuario |
| `401` | Unauthorized | Redirigir a login |
| `404` | Not Found | Mostrar "Recurso no encontrado" |
| `500` | Internal Server Error | Mostrar error genérico + `descripcion` |

### Estructura de Error

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Descripción del error",
  "path": "/api/asientos/evento/1/bloquear",
  "message": "error.validation"
}
```

### Errores Comunes

#### 401 Unauthorized
**Causa**: Token JWT expirado o inválido

**Solución**: 
```kotlin
if (response.code == 401) {
    // Borrar token local
    // Redirigir a pantalla de login
    navigateToLogin()
}
```

---

#### 400 Bad Request en Bloqueo
**Causa**: Asientos inválidos (fuera de rango, más de 4, etc.)

**Respuesta**:
```json
{
  "resultado": false,
  "descripcion": "Asientos inválidos. Verifica fila/columna y que no excedan el límite (máx 4)."
}
```

**Solución**: Validar localmente antes de enviar

---

#### 500 Internal Server Error en Bloqueo
**Causa**: Asientos no disponibles

**Respuesta**:
```json
{
  "resultado": false,
  "descripcion": "El asiento (1,3) ya está ocupado",
  "asientos": [
    {
      "fila": 1,
      "columna": 3,
      "estado": "Ocupado"
    }
  ]
}
```

**Solución**: 
- Refrescar mapa de asientos
- Mostrar mensaje al usuario
- Permitir seleccionar otros asientos

---

#### 500 Internal Server Error en Venta
**Causa**: Bloqueo expirado o no existe

**Respuesta**:
```json
{
  "resultado": false,
  "descripcion": "Los asientos no están bloqueados o el bloqueo expiró"
}
```

**Solución**:
- Volver a la pantalla de selección de asientos
- Mostrar mensaje: "El tiempo de bloqueo expiró. Por favor, selecciona los asientos nuevamente"

---

## 💡 Ejemplos de Uso

### Ejemplo 1: Flujo Completo con Retrofit (Kotlin)

```kotlin
// 1. Login
val loginResponse = apiService.login(
    LoginRequest("admin", "admin")
)
val token = loginResponse.idToken
saveToken(token)

// 2. Listar eventos
val eventos = apiService.getEventosResumidos(
    authorization = "Bearer $token"
)
showEventosList(eventos)

// 3. Ver detalle
val eventoId = 1L
val detalle = apiService.getEventoDetalle(
    id = eventoId,
    authorization = "Bearer $token"
)
showEventoDetail(detalle)

// 4. Obtener mapa
val mapa = apiService.getMapaAsientos(
    eventoId = eventoId,
    authorization = "Bearer $token"
)
renderAsientos(mapa)

// 5. Bloquear asientos
val asientosSeleccionados = listOf(
    AsientoSeleccionDTO(1, 3),
    AsientoSeleccionDTO(1, 4)
)
val bloqueoResponse = apiService.bloquearAsientos(
    eventoId = eventoId,
    asientos = asientosSeleccionados,
    authorization = "Bearer $token"
)

if (bloqueoResponse.resultado) {
    // Iniciar timer de 5 minutos
    startBloqueoTimer(5 * 60)
    navigateToCargarNombres()
} else {
    showError(bloqueoResponse.descripcion)
}

// 6. Realizar venta
val ventaResponse = apiService.realizarVenta(
    eventoId = eventoId,
    asientos = asientosSeleccionados,
    authorization = "Bearer $token"
)

if (ventaResponse.resultado) {
    showSuccess("¡Compra exitosa! ID: ${ventaResponse.ventaId}")
    navigateToMisCompras()
} else {
    showError(ventaResponse.descripcion)
}
```

---

### Ejemplo 2: Renderizar Mapa de Asientos

```kotlin
fun renderAsientos(mapa: MapaAsientosDTO) {
    val grid = Array(mapa.totalFilas) { 
        Array(mapa.totalColumnas) { AsientoEstado.DISPONIBLE } 
    }
    
    val now = Instant.now()
    
    mapa.asientos.forEach { asiento ->
        val estado = when {
            asiento.estado == "Vendido" -> AsientoEstado.VENDIDO
            asiento.estado == "Bloqueado" && asiento.expira != null -> {
                val expira = Instant.parse(asiento.expira)
                if (expira.isAfter(now)) {
                    AsientoEstado.BLOQUEADO
                } else {
                    AsientoEstado.DISPONIBLE // Bloqueo expirado
                }
            }
            else -> AsientoEstado.DISPONIBLE
        }
        
        grid[asiento.fila - 1][asiento.columna - 1] = estado
    }
    
    renderGrid(grid)
}

enum class AsientoEstado {
    DISPONIBLE,    // 🟢 Verde - Clickeable
    BLOQUEADO,     // 🟡 Amarillo - No clickeable
    VENDIDO,       // 🔴 Rojo - No clickeable
    SELECCIONADO   // 🔵 Azul - Mi selección
}
```

---

### Ejemplo 3: Timer de Bloqueo

```kotlin
class BloqueoTimer(private val durationSeconds: Int) {
    
    private var job: Job? = null
    
    fun start(onTick: (Int) -> Unit, onExpired: () -> Unit) {
        job = CoroutineScope(Dispatchers.Main).launch {
            var remaining = durationSeconds
            
            while (remaining > 0) {
                onTick(remaining)
                delay(1000)
                remaining--
            }
            
            onExpired()
        }
    }
    
    fun stop() {
        job?.cancel()
    }
}

// Uso
val timer = BloqueoTimer(5 * 60) // 5 minutos
timer.start(
    onTick = { seconds ->
        val minutes = seconds / 60
        val secs = seconds % 60
        updateTimerUI("Tiempo restante: $minutes:${secs.toString().padStart(2, '0')}")
    },
    onExpired = {
        showDialog("El tiempo de bloqueo expiró. Debes seleccionar los asientos nuevamente.")
        navigateToMapaAsientos()
    }
)
```

---

### Ejemplo 4: Manejo de Errores Robusto

```kotlin
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        val response = call()
        Result.success(response)
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> {
                // Token expirado
                clearToken()
                navigateToLogin()
                Result.failure(Exception("Sesión expirada"))
            }
            400 -> {
                // Error de validación
                val errorBody = e.response()?.errorBody()?.string()
                val error = parseError(errorBody)
                Result.failure(Exception(error.descripcion))
            }
            404 -> {
                Result.failure(Exception("Recurso no encontrado"))
            }
            500 -> {
                // Error del servidor
                val errorBody = e.response()?.errorBody()?.string()
                val error = parseError(errorBody)
                Result.failure(Exception(error.descripcion ?: "Error del servidor"))
            }
            else -> {
                Result.failure(Exception("Error desconocido: ${e.message}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Error de conexión: ${e.message}"))
    }
}

// Uso
val result = safeApiCall { 
    apiService.bloquearAsientos(eventoId, asientos, "Bearer $token") 
}

result.fold(
    onSuccess = { response ->
        if (response.resultado) {
            showSuccess()
        } else {
            showError(response.descripcion)
        }
    },
    onFailure = { error ->
        showError(error.message ?: "Error desconocido")
    }
)
```

---

## 🔧 Configuración del Cliente HTTP

### Retrofit (Kotlin/Android)

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("http://localhost:8081/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    )
    .build()

interface BackendApiService {
    
    @POST("/api/authenticate")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @GET("/api/eventos-consulta/resumidos")
    suspend fun getEventosResumidos(
        @Header("Authorization") authorization: String
    ): List<EventoResumenDTO>
    
    @GET("/api/eventos-consulta/{id}")
    suspend fun getEventoDetalle(
        @Path("id") id: Long,
        @Header("Authorization") authorization: String
    ): EventoDetalleDTO
    
    @GET("/api/asientos/evento/{eventoId}/mapa")
    suspend fun getMapaAsientos(
        @Path("eventoId") eventoId: Long,
        @Header("Authorization") authorization: String
    ): MapaAsientosDTO
    
    @POST("/api/asientos/evento/{eventoId}/bloquear")
    suspend fun bloquearAsientos(
        @Path("eventoId") eventoId: Long,
        @Body asientos: List<AsientoSeleccionDTO>,
        @Header("Authorization") authorization: String
    ): BloquearAsientosResponseDTO
    
    @POST("/api/ventas/evento/{eventoId}/realizar")
    suspend fun realizarVenta(
        @Path("eventoId") eventoId: Long,
        @Body asientos: List<AsientoSeleccionDTO>,
        @Header("Authorization") authorization: String
    ): RealizarVentaResponseDTO
    
    @GET("/api/ventas")
    suspend fun getVentas(
        @Header("Authorization") authorization: String
    ): List<VentaDTO>
    
    @GET("/api/ventas/{id}")
    suspend fun getVentaDetalle(
        @Path("id") id: Long,
        @Header("Authorization") authorization: String
    ): VentaDTO
}
```

---

## 📝 Checklist para Mobile Developer

### Antes de Empezar
- [ ] Configurar cliente HTTP (Retrofit/Ktor)
- [ ] Base URL: `http://localhost:8081` (dev) o IP del servidor (prod)
- [ ] Timeouts: 30 segundos mínimo
- [ ] Manejo de errores HTTP (401, 400, 404, 500)

### Pantalla de Login
- [ ] Endpoint: `POST /api/authenticate`
- [ ] Guardar token JWT en almacenamiento seguro
- [ ] Agregar token a todas las llamadas subsiguientes

### Pantalla de Eventos
- [ ] Endpoint: `GET /api/eventos-consulta/resumidos`
- [ ] Mostrar: título, fecha, imagen, tipo
- [ ] Pull-to-refresh para actualizar

### Pantalla de Detalle
- [ ] Endpoint: `GET /api/eventos-consulta/{id}`
- [ ] Mostrar todos los campos
- [ ] Botón "Comprar Entradas"

### Pantalla de Mapa de Asientos
- [ ] Endpoint: `GET /api/asientos/evento/{id}/mapa`
- [ ] Renderizar grilla de `totalFilas` × `totalColumnas`
- [ ] Colores según estado (disponible, bloqueado, vendido, seleccionado)
- [ ] Validar bloqueos expirados (`expira <= now()`)
- [ ] Limitar selección a 4 asientos
- [ ] Mostrar contador: "X/4 seleccionados"

### Pantalla de Bloqueo
- [ ] Endpoint: `POST /api/asientos/evento/{id}/bloquear`
- [ ] Mostrar timer de 5 minutos
- [ ] Actualizar cada segundo
- [ ] Al expirar, volver al mapa
- [ ] Manejar error: asientos no disponibles

### Pantalla de Nombres
- [ ] Campos de texto para cada asiento
- [ ] Validación: no vacío, min 3 chars
- [ ] Botón "Volver" (permite cambiar asientos)

### Pantalla de Confirmación
- [ ] Endpoint: `POST /api/ventas/evento/{id}/realizar`
- [ ] Mostrar resumen antes de confirmar
- [ ] Manejar error: bloqueo expirado
- [ ] Mostrar `ventaId` en éxito

### Pantalla de Mis Compras
- [ ] Endpoint: `GET /api/ventas`
- [ ] Lista de compras realizadas
- [ ] Click → detalle

### Manejo de Token
- [ ] Guardar en KeyStore/Keychain
- [ ] Renovar al recibir 401
- [ ] Borrar al cerrar sesión

---

## 🐛 Troubleshooting

### "Connection refused" al llamar al Backend

**Causa**: Backend no está ejecutándose o URL incorrecta

**Solución**:
1. Verificar que el Backend esté corriendo: `http://localhost:8081/actuator/health`
2. En emulador Android, usar `10.0.2.2:8081` en lugar de `localhost:8081`
3. En dispositivo físico, usar IP local del servidor

---

### "401 Unauthorized" en todas las llamadas

**Causa**: Token inválido o no enviado

**Solución**:
1. Verificar que estás enviando header: `Authorization: Bearer {token}`
2. Verificar que el token no esté expirado (24h)
3. Hacer login nuevamente

---

### "Los asientos no están bloqueados" al vender

**Causa**: Bloqueo expiró (> 5 minutos)

**Solución**:
1. Implementar timer visible
2. Al expirar, forzar regreso al mapa
3. Bloquear nuevamente

---

### Respuestas lentas (> 5 segundos)

**Causa**: El Backend está llamando al Proxy que llama a la Cátedra

**Solución**:
1. Aumentar timeouts a 30 segundos
2. Mostrar loading indicator
3. No permitir múltiples clicks

---

## 📞 Contacto y Soporte

- **Backend Developer**: [Tu contacto]
- **Documentación Completa**: Ver archivos `*.md` en el repositorio
- **Arquitectura**: Ver `ARQUITECTURA-COMUNICACION-COMPLETA.md`
- **Gestión de Perfiles**: Ver `GESTION-PERFILES-BD.md`

---

## 📄 Changelog

### v1.0.0 (2025-12-11)
- ✅ Endpoints de eventos (lista resumida, detalle)
- ✅ Endpoints de asientos (mapa, bloqueo)
- ✅ Endpoints de ventas (realizar, listar, detalle)
- ✅ Autenticación JWT
- ✅ Integración con Proxy
- ✅ Sincronización automática de eventos
- ✅ Webhooks de Kafka

---

## 📚 Referencias

- **Spring Boot**: https://spring.io/projects/spring-boot
- **JWT**: https://jwt.io/
- **ISO-8601**: https://www.iso.org/iso-8601-date-and-time-format.html
- **Retrofit**: https://square.github.io/retrofit/
- **Ktor Client**: https://ktor.io/docs/client.html

---

**Última actualización**: 2025-12-11  
**Versión del Backend**: 1.0.0  
**Puerto**: 8081

