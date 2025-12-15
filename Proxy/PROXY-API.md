# 📡 Documentación API del Proxy

Este documento describe **qué envía y qué recibe** el servicio Proxy, tanto para consumir la API de la cátedra como para exponer endpoints al Backend.

---

## 🎯 Endpoints del Proxy (Consumidos por el Backend)

El Backend consume estos endpoints del Proxy. El Proxy actúa como intermediario con la cátedra.

### Base URL del Proxy
```
http://localhost:8080
```

---

## 📋 1. Endpoints de Eventos

### 1.1. Listar Eventos Resumidos

**Endpoint Proxy**: `GET /api/eventos/resumidos`

**Consumido desde Cátedra**: `GET http://192.168.194.250:8080/api/endpoints/v1/eventos-resumidos`

**Request**: Ninguno (GET sin body)

**Response 200 OK**:
```json
[
  {
    "id": 1,
    "titulo": "Conferencia Nerd",
    "resumen": "Esta es una conferencia de Nerds",
    "descripcion": "Esta es una conferencia de prueba para verificar que los datos están correctos",
    "fecha": "2025-11-10T11:00:00Z",
    "precioEntrada": 2500.00,
    "eventoTipo": {
      "nombre": "Conferencia",
      "descripcion": "Conferencia"
    }
  },
  {
    "id": 2,
    "titulo": "Otra Conferencia Nerd",
    "resumen": "Esta es otra conferencia de Nerds",
    "descripcion": "Esta es una conferencia de prueba para verificar que los datos están correctos version 2",
    "fecha": "2025-12-12T14:00:00Z",
    "precioEntrada": 4500.00,
    "eventoTipo": {
      "nombre": "Obra de teatro",
      "descripcion": "Obra de teatro"
    }
  }
]
```

**Response 503 Service Unavailable**: Si la cátedra no responde

**Notas**:
- ✅ Cumple con **Payload 3** de la cátedra
- Datos resumidos para vistas de listado
- No incluye integrantes, dirección, imagen ni cantidad de asientos

---

### 1.2. Listar Eventos Completos

**Endpoint Proxy**: `GET /api/eventos`

**Consumido desde Cátedra**: `GET http://192.168.194.250:8080/api/endpoints/v1/eventos`

**Request**: Ninguno (GET sin body)

**Response 200 OK**:
```json
[
  {
    "id": 1,
    "titulo": "Conferencia Nerd",
    "resumen": "Esta es una conferencia de Nerds",
    "descripcion": "Esta es una conferencia de prueba para verificar que los datos están correctos",
    "fecha": "2025-11-10T11:00:00Z",
    "direccion": "Aula magna de la Universidad de Mendoza",
    "imagen": "https://ejemplo.com/imagen.jpg",
    "filaAsientos": 10,
    "columnAsientos": 20,
    "precioEntrada": 2500.00,
    "eventoTipo": {
      "nombre": "Conferencia",
      "descripcion": "Conferencia"
    },
    "integrantes": [
      {
        "nombre": "María",
        "apellido": "Corvalán",
        "identificacion": "Dra."
      }
    ]
  }
]
```

**Response 503 Service Unavailable**: Si la cátedra no responde

**Notas**:
- ✅ Cumple con **Payload 4** de la cátedra
- Incluye todos los datos del evento: integrantes, imagen, dirección, dimensiones de asientos

---

### 1.3. Obtener Detalle de un Evento

**Endpoint Proxy**: `GET /api/eventos/{id}`

**Consumido desde Cátedra**: `GET http://192.168.194.250:8080/api/endpoints/v1/evento/{id}`

**Request**: `id` en path parameter

**Response 200 OK**:
```json
{
  "id": 1,
  "titulo": "Conferencia Nerd",
  "resumen": "Esta es una conferencia de Nerds",
  "descripcion": "Esta es una conferencia de prueba para verificar que los datos están correctos",
  "fecha": "2025-11-10T11:00:00Z",
  "direccion": "Aula magna de la Universidad de Mendoza",
  "imagen": "https://ejemplo.com/imagen.jpg",
  "filaAsientos": 10,
  "columnAsientos": 20,
  "precioEntrada": 2500.00,
  "eventoTipo": {
    "nombre": "Conferencia",
    "descripcion": "Conferencia"
  },
  "integrantes": [
    {
      "nombre": "María",
      "apellido": "Corvalán",
      "identificacion": "Dra."
    }
  ]
}
```

**Response 404 Not Found**: Si el evento no existe

**Notas**:
- ✅ Cumple con **Payload 5** de la cátedra
- Retorna el mismo formato que `/api/eventos` pero para un solo evento

---

### 1.4. Bloquear Asientos

**Endpoint Proxy**: `POST /api/eventos/bloquear-asientos`

**Consumido desde Cátedra**: `POST http://192.168.194.250:8080/api/endpoints/v1/bloquear-asientos`

**Request Body**:
```json
{
  "eventoId": 1,
  "asientos": [
    {
      "fila": 2,
      "columna": 1
    },
    {
      "fila": 2,
      "columna": 2
    }
  ]
}
```

**Response 200 OK (Bloqueo Exitoso)**:
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados con exito",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "estado": "Bloqueo exitoso"
    },
    {
      "fila": 2,
      "columna": 4,
      "estado": "Bloqueo exitoso"
    }
  ]
}
```

**Response 200 OK (Bloqueo Fallido)**:
```json
{
  "resultado": false,
  "descripcion": "No todos los asientos pueden ser bloqueados",
  "eventoId": 1,
  "asientos": [
    {
      "fila": 2,
      "columna": 1,
      "estado": "Ocupado"
    },
    {
      "fila": 2,
      "columna": 2,
      "estado": "Ocupado"
    }
  ]
}
```

**Notas**:
- ✅ Cumple con **Payload 6** de la cátedra
- `resultado: true` = todos los asientos fueron bloqueados
- `resultado: false` = algún asiento estaba Ocupado o Bloqueado previamente
- El Proxy puede notificar al Backend vía webhook con la respuesta

---

### 1.5. Obtener Estado de Asientos (desde Redis)

**Endpoint Proxy**: `GET /api/eventos/{id}/asientos-estado`

**Consumido desde**: Redis (cátedra) - Keys `evento:{id}:asientos` o `evento:{id}:asiento:*`

**Request**: `id` en path parameter

**Response 200 OK**:
```json
[
  {
    "fila": 1,
    "columna": 1,
    "estado": "Libre"
  },
  {
    "fila": 2,
    "columna": 3,
    "estado": "Bloqueado"
  },
  {
    "fila": 3,
    "columna": 5,
    "estado": "Ocupado"
  }
]
```

**Response 500 Internal Server Error**: Si Redis no responde o no hay datos

**Notas**:
- Endpoint **exclusivo del Proxy**, no existe en la API de la cátedra
- Útil para consultar el estado en tiempo real sin hacer bloqueos
- Estados posibles: `Libre`, `Bloqueado`, `Ocupado`

---

## 💳 2. Endpoints de Ventas

### 2.1. Realizar Venta

**Endpoint Proxy**: `POST /api/ventas/realizar`

**Consumido desde Cátedra**: `POST http://192.168.194.250:8080/api/endpoints/v1/realizar-venta`

**Request Body**:
```json
{
  "eventoId": 1,
  "fecha": "2025-08-17T20:00:00.000Z",
  "precioVenta": 1400.10,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "persona": "Fernando Galvez"
    },
    {
      "fila": 2,
      "columna": 4,
      "persona": "Carlos Perez"
    }
  ]
}
```

**Response 200 OK (Venta Exitosa)**:
```json
{
  "eventoId": 1,
  "ventaId": 1506,
  "fechaVenta": "2025-08-24T23:18:41.974720Z",
  "resultado": true,
  "descripcion": "Venta realizada con exito",
  "precioVenta": 1400.0,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "persona": "Fernando Galvez",
      "estado": "Vendido"
    },
    {
      "fila": 2,
      "columna": 4,
      "persona": "Carlos Perez",
      "estado": "Vendido"
    }
  ]
}
```

**Response 200 OK (Venta Rechazada)**:
```json
{
  "eventoId": 1,
  "ventaId": null,
  "fechaVenta": "2025-08-24T23:18:07.541151Z",
  "resultado": false,
  "descripcion": "Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.",
  "precioVenta": 1400.0,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "persona": "Fernando Galvez",
      "estado": "Libre"
    },
    {
      "fila": 2,
      "columna": 4,
      "persona": "Carlos Perez",
      "estado": "Libre"
    }
  ]
}
```

**Notas**:
- ✅ Cumple con **Payload 7** de la cátedra
- `resultado: true` = venta exitosa, `ventaId` generado
- `resultado: false` = venta rechazada, asientos no estaban bloqueados
- El Proxy puede notificar al Backend vía webhook con la respuesta

---

### 2.2. Listar Ventas

**Endpoint Proxy**: `GET /api/ventas`

**Consumido desde Cátedra**: `GET http://192.168.194.250:8080/api/endpoints/v1/listar-ventas`

**Request**: Ninguno (GET sin body)

**Response 200 OK**:
```json
[
  {
    "eventoId": 1,
    "ventaId": 1503,
    "fechaVenta": "2025-08-23T22:51:02.574851Z",
    "resultado": false,
    "descripcion": "Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.",
    "precioVenta": 1200.1,
    "cantidadAsientos": 0
  },
  {
    "eventoId": 1,
    "ventaId": 1504,
    "fechaVenta": "2025-08-23T22:51:15.101553Z",
    "resultado": true,
    "descripcion": "Venta realizada con exito",
    "precioVenta": 1200.1,
    "cantidadAsientos": 2
  }
]
```

**Notas**:
- ✅ Cumple con **Payload 8** de la cátedra
- Datos resumidos de todas las ventas (exitosas y fallidas) del alumno
- `cantidadAsientos: 0` en ventas fallidas

---

### 2.3. Obtener Venta por ID

**Endpoint Proxy**: `GET /api/ventas/{id}`

**Consumido desde Cátedra**: `GET http://192.168.194.250:8080/api/endpoints/v1/listar-venta/{id}`

**Request**: `id` en path parameter

**Response 200 OK (Venta Exitosa)**:
```json
{
  "eventoId": 1,
  "ventaId": 1504,
  "fechaVenta": "2025-08-23T22:51:15.101553Z",
  "resultado": true,
  "descripcion": "Venta realizada con exito",
  "precioVenta": 1200.1,
  "asientos": [
    {
      "fila": 2,
      "columna": 1,
      "persona": "Fernando Villarreal",
      "estado": "Ocupado"
    },
    {
      "fila": 2,
      "columna": 2,
      "persona": "Carlos Perez",
      "estado": "Ocupado"
    }
  ]
}
```

**Response 200 OK (Venta Fallida)**:
```json
{
  "eventoId": 1,
  "ventaId": 1503,
  "fechaVenta": "2025-08-23T22:51:02.574851Z",
  "resultado": false,
  "descripcion": "Venta rechazada. Alguno de los asientos no se encontraban bloqueados para la venta.",
  "precioVenta": 1200.1,
  "asientos": []
}
```

**Response 404 Not Found**: Si la venta no existe

**Notas**:
- ✅ Cumple con **Payload 9** de la cátedra
- Ventas exitosas incluyen lista de asientos vendidos
- Ventas fallidas tienen `asientos: []`

---

## 👤 3. Endpoints de Autenticación

### 3.1. Login de Usuario

**Endpoint Proxy**: `POST /api/users/login`

**Consumido desde Cátedra**: `POST http://192.168.194.250:8080/api/authenticate`

**Request Body**:
```json
{
  "username": "juan",
  "password": "juan123",
  "rememberMe": false
}
```

**Response 200 OK**:
```json
{
  "id_token": "eyJhbGciOiJITzUxMiJ9.eyJzdWIiOiJmZXJuYW5kbyIsImV4cCI6MTc1ODIyMTY5..."
}
```

**Response 500 Internal Server Error**: Si el login falla

**Notas**:
- ✅ Cumple con **Payload 2** de la cátedra
- **NO se usa** el Payload 1 (registro) en el Proxy actual
- El token devuelto es para uso del **cliente final** (no del Proxy)
- El Proxy tiene su propio token JWT automático configurado en `.env`

---

## 🔔 4. Notificaciones Asíncronas (Kafka → Backend)

El Proxy escucha el topic `eventos-actualizacion` de Kafka y notifica al Backend vía webhook.

### 4.1. Consumer Kafka

**Topic**: `eventos-actualizacion`  
**Bootstrap**: `192.168.194.250:9092`  
**Group ID**: `proxy-grupo`

**Formato de mensaje Kafka recibido**:
```json
{
  "tipoEvento": "VENTA_COMPLETADA",
  "eventoId": 1,
  "ventaId": 1506,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "estado": "Vendido"
    }
  ],
  "timestamp": "2025-08-24T23:18:41.974720Z"
}
```

**Tipos de evento**:
- `EVENTO_MODIFICADO`: Evento actualizado
- `ASIENTOS_BLOQUEADOS`: Asientos bloqueados asíncronamente
- `VENTA_COMPLETADA`: Venta finalizada asíncronamente

### 4.2. Webhook al Backend

El Proxy notifica al Backend en la URL configurada: `${app.backend.webhook-url}`

**Request enviado al Backend**:
```http
POST http://localhost:8081/api/eventos/webhook
Content-Type: application/json
```

**Body (ejemplo genérico)**:
```json
{
  "tipo": "kafka:venta-completada",
  "payload": {
    "eventoId": 1,
    "ventaId": 1506,
    "asientos": [...]
  },
  "timestamp": "2025-08-24T23:18:41.974720Z"
}
```

**Notas**:
- El Backend debe implementar el endpoint webhook
- El Proxy reintenta la notificación si falla (ver logs)
- Puede deshabilitarse con `spring.kafka.enabled=false`

---

## 🛠️ 5. Endpoints de Administración

### 5.1. Health Check

**Endpoint Proxy**: `GET /actuator/health`

**Response 200 OK**:
```json
{
  "status": "UP"
}
```

**Notas**:
- Verifica conectividad con Redis (si está habilitado)
- `status: DOWN` si Redis no responde

---

### 5.2. Estado del Token JWT

**Endpoint Proxy**: `GET /actuator/auth/status`

**Response 200 OK**:
```json
{
  "hasToken": true,
  "username": "templay333",
  "lastRefresh": "2025-12-13T18:30:00Z"
}
```

**Notas**:
- Muestra si el Proxy tiene un token JWT válido
- Útil para debugging de autenticación

---

### 5.3. Refrescar Token JWT Manualmente

**Endpoint Proxy**: `POST /actuator/auth/refresh`

**Response 200 OK**:
```json
{
  "message": "Token renovado exitosamente"
}
```

**Notas**:
- Fuerza la renovación del token JWT del Proxy
- Normalmente no necesario (renovación automática cada 30 min)

---

## 📊 Resumen de Cumplimiento con Payloads de la Cátedra

| Payload | Endpoint Cátedra | Endpoint Proxy | Estado |
|---------|-----------------|----------------|--------|
| **Payload 1** - Registro | `/api/v1/agregar_usuario` | ❌ No implementado | No usado |
| **Payload 2** - Login | `/api/authenticate` | `POST /api/users/login` | ✅ Cumple |
| **Payload 3** - Eventos Resumidos | `/api/endpoints/v1/eventos-resumidos` | `GET /api/eventos/resumidos` | ✅ Cumple |
| **Payload 4** - Eventos Completos | `/api/endpoints/v1/eventos` | `GET /api/eventos` | ✅ Cumple |
| **Payload 5** - Detalle Evento | `/api/endpoints/v1/evento/{id}` | `GET /api/eventos/{id}` | ✅ Cumple |
| **Payload 6** - Bloquear Asientos | `/api/endpoints/v1/bloquear-asientos` | `POST /api/eventos/bloquear-asientos` | ✅ Cumple |
| **Payload 7** - Realizar Venta | `/api/endpoints/v1/realizar-venta` | `POST /api/ventas/realizar` | ✅ Cumple |
| **Payload 8** - Listar Ventas | `/api/endpoints/v1/listar-ventas` | `GET /api/ventas` | ✅ Cumple |
| **Payload 9** - Detalle Venta | `/api/endpoints/v1/listar-venta/{id}` | `GET /api/ventas/{id}` | ✅ Cumple |

**Adicionales del Proxy**:
- `GET /api/eventos/{id}/asientos-estado` - Estado de asientos desde Redis
- `GET /actuator/health` - Health check
- `GET /actuator/auth/status` - Estado del token JWT
- `POST /actuator/auth/refresh` - Renovar token JWT manualmente

---

## 🔧 Configuración del Backend para consumir el Proxy

El Backend debe:

1. **Consumir los endpoints del Proxy** en vez de la cátedra directamente
   - Base URL: `http://localhost:8080` (o la configurada)

2. **Implementar el endpoint webhook** para recibir notificaciones asíncronas de Kafka
   - Endpoint sugerido: `POST /api/eventos/webhook`
   - Content-Type: `application/json`

3. **Manejar respuestas síncronas** de los endpoints del Proxy
   - El Proxy devuelve las respuestas de la cátedra sin modificar

4. **Manejar notificaciones asíncronas** del webhook
   - Eventos de Kafka llegan vía webhook del Proxy

---

## 📝 Ejemplo de Flujo Completo

### Flujo: Bloquear Asientos

1. **Backend → Proxy**: `POST /api/eventos/bloquear-asientos`
   ```json
   {
     "eventoId": 1,
     "asientos": [{"fila": 2, "columna": 1}]
   }
   ```

2. **Proxy → Cátedra**: `POST http://192.168.194.250:8080/api/endpoints/v1/bloquear-asientos`
   - Incluye `Authorization: Bearer {token}` automáticamente

3. **Cátedra → Proxy**: Respuesta síncrona
   ```json
   {
     "resultado": true,
     "descripcion": "Asientos bloqueados con exito",
     "eventoId": 1,
     "asientos": [{"fila": 2, "columna": 1, "estado": "Bloqueo exitoso"}]
   }
   ```

4. **Proxy → Backend**: Devuelve la respuesta síncrona

5. **(Opcional) Kafka → Proxy**: Mensaje asíncrono en `eventos-actualizacion`
   ```json
   {
     "tipoEvento": "ASIENTOS_BLOQUEADOS",
     "eventoId": 1,
     "asientos": [...]
   }
   ```

6. **(Opcional) Proxy → Backend Webhook**: `POST /api/eventos/webhook`
   ```json
   {
     "tipo": "kafka:asientos-bloqueados",
     "payload": {...}
   }
   ```

---

## 🚀 Testing

Usa los scripts en `scripts/`:
- `test-proxy-endpoints.ps1` - Prueba todos los endpoints del Proxy
- `test-conexion.ps1` - Verifica conectividad con cátedra
- `test-endpoints-jwt.ps1` - Prueba endpoints de la cátedra directamente

---

**Última actualización**: 2025-12-14

