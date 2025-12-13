# 📋 RESUMEN FINAL - Backend Requirements

## ✅ Decisiones Tomadas

Basado en la conversación con el usuario, estas son las decisiones finales:

### 1. **Autenticación y Usuarios**

**Decisión:** ✅ **Implementar registro de usuarios**

**Razón:**
- El usuario quiere poder **crear su propia cuenta**
- Aunque inicialmente sea una sola cuenta, la arquitectura permite múltiples usuarios

**Endpoints requeridos:**
- `POST /api/register` - Crear cuenta
- `POST /api/authenticate` - Login

---

### 2. **Búsqueda/Filtrado de Eventos**

**Decisión:** ⚪ **Nice to have (Opcional)**

**Razón:**
- No es crítico para MVP
- La app tiene SearchBar (lupa) pero puede implementarse después

**Recomendación:**
- Si hay **< 100 eventos** → Filtrado en **frontend** (más rápido)
- Si hay **> 100 eventos** → Endpoint en **backend** (necesario)

**Endpoint opcional:**
- `GET /api/eventos-consulta/buscar?texto={query}` - Buscar eventos

---

## 📊 Endpoints Finales Requeridos

### **Esenciales (Implementar AHORA):**

1. ✅ `POST /api/register` - Registro de usuario (NUEVO)
2. ✅ `POST /api/authenticate` - Login
3. ✅ `GET /api/eventos-consulta/resumidos` - Lista eventos
4. ✅ `GET /api/eventos-consulta/{id}` - Detalle evento
5. ✅ `GET /api/asientos/evento/{id}/mapa` - Mapa asientos
6. ✅ `POST /api/asientos/evento/{id}/bloquear` - Bloquear asientos
7. ✅ `POST /api/ventas/evento/{id}/realizar` - Comprar
8. ✅ `GET /api/ventas` - Mis compras
9. ✅ `GET /api/ventas/{id}` - Detalle compra

**Total: 9 endpoints esenciales**

---

### **Opcionales (Implementar DESPUÉS si es necesario):**

10. ⚪ `GET /api/eventos-consulta/buscar` - Búsqueda (nice to have)

---

## 📝 Especificaciones del Endpoint de Registro

### `POST /api/register`

**Request Body:**
```json
{
  "username": "juan_perez",
  "email": "juan@ejemplo.com",
  "password": "mipassword123"
}
```

**Response 201 Created:**
```json
{
  "mensaje": "Usuario creado exitosamente",
  "user_id": 1,
  "username": "juan_perez"
}
```

**Response 400 Bad Request:**
```json
{
  "mensaje": "Validación fallida",
  "errores": [
    "El username ya existe"
  ]
}
```

**Validaciones:**
- **Username:**
  - ✅ Único (no puede repetirse)
  - ✅ 3-50 caracteres
  - ✅ Solo letras, números, guiones y guiones bajos
  - ✅ Case-insensitive (guardar en lowercase)

- **Email:**
  - ✅ Único (no puede repetirse)
  - ✅ Formato válido
  - ✅ Case-insensitive (guardar en lowercase)

- **Password:**
  - ✅ Mínimo 6 caracteres
  - ✅ Debe hashearse con BCrypt (nunca guardar en texto plano)

**Pantalla:** `SignInScreen` (ya existe en la app, lista para conectarse)

---

## 📝 Especificaciones del Endpoint de Búsqueda (Opcional)

### `GET /api/eventos-consulta/buscar`

**Query Parameters:**
- `texto` (String, opcional): Busca en título y resumen
- `categoria` (String, opcional): Filtra por tipo de evento

**Ejemplo:**
```
GET /api/eventos-consulta/buscar?texto=rock&categoria=Música
```

**Response 200 OK:**
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

**Notas:**
- Búsqueda **case-insensitive**
- Si `texto` vacío, retorna todos los eventos
- Si no hay resultados, retorna `[]`

**Pantalla:** `HomeScreen` tiene SearchBar lista para usar este endpoint

**Alternativa:** Si hay < 100 eventos, hacer filtrado en frontend es válido y más rápido

---

## 🎯 Flujo de Usuario

### 1. Primera vez (Nuevo usuario)

```
1. Usuario abre app → SplashScreen
2. No tiene cuenta → LoginScreen
3. Click "Crear Cuenta" → SignInScreen
4. Ingresa datos → POST /api/register
5. Registro exitoso → Vuelve a LoginScreen
6. Ingresa credenciales → POST /api/authenticate
7. Login exitoso → HomeScreen
```

### 2. Usuario existente

```
1. Usuario abre app → SplashScreen
2. Ya tiene token válido → HomeScreen directamente
3. O token expirado → LoginScreen
4. Login → POST /api/authenticate
5. HomeScreen
```

---

## 🔒 Seguridad

### Token JWT

**Contenido del token:**
```json
{
  "sub": "juan_perez",
  "user_id": 1,
  "email": "juan@ejemplo.com",
  "exp": 1702523400
}
```

**Uso:**
```
Authorization: Bearer eyJhbGci...
```

**Validación:**
- ✅ Verificar firma
- ✅ Verificar expiración (24 horas)
- ✅ Extraer `user_id` para operaciones

---

## 📂 Modelo de Datos (Backend)

### Tabla `usuarios`

```sql
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Notas:**
- `username` y `email` deben tener índices únicos
- `password_hash` es BCrypt (nunca texto plano)
- Guardar `username` y `email` en lowercase

---

## ✅ Checklist de Implementación

### Backend

- [ ] **Endpoint de Registro**
  - [ ] `POST /api/register` implementado
  - [ ] Validación de username único
  - [ ] Validación de email único
  - [ ] Hash de password con BCrypt
  - [ ] Retornar error si usuario existe

- [ ] **Endpoint de Login (actualizado)**
  - [ ] `POST /api/authenticate` verificado
  - [ ] Token JWT incluye `user_id`
  - [ ] Login funciona con username o email
  - [ ] Validación de password con BCrypt

- [ ] **Relaciones de Datos**
  - [ ] Tabla `ventas` tiene FK a `usuarios.id`
  - [ ] Bloqueos de asientos asociados a `user_id`
  - [ ] `GET /api/ventas` filtra por `user_id` del token

- [ ] **(Opcional) Endpoint de Búsqueda**
  - [ ] `GET /api/eventos-consulta/buscar` implementado
  - [ ] Búsqueda case-insensitive
  - [ ] Filtro por categoría funciona

### Frontend (Ya implementado)

- [x] Pantalla `SignInScreen` existe
- [x] Validaciones en frontend
- [x] Navegación Login ↔ SignIn
- [x] SearchBar en `HomeScreen` (deshabilitada)
- [ ] Conectar `SignInViewModel` con endpoint `/register`
- [ ] Conectar `HomeViewModel` con endpoint `/buscar` (si se implementa)

---

## 🚀 Prioridades

### **Fase 1: MVP (AHORA)**

1. ✅ Implementar registro (`POST /api/register`)
2. ✅ Actualizar login para múltiples usuarios
3. ✅ Asociar ventas a usuarios específicos
4. ✅ Probar flujo completo: Registro → Login → Compra

### **Fase 2: Nice to Have (DESPUÉS)**

5. ⚪ Implementar búsqueda (`GET /api/eventos-consulta/buscar`)
6. ⚪ O implementar filtrado en frontend
7. ⚪ Recuperación de contraseña
8. ⚪ Editar perfil de usuario

---

## 📞 Preguntas Resueltas

| Pregunta | Respuesta |
|----------|-----------|
| ¿Un solo usuario o múltiples? | Múltiples (aunque empieces con uno) |
| ¿Necesito endpoint de registro? | ✅ Sí |
| ¿Necesito endpoint de búsqueda? | ⚪ Nice to have (opcional) |
| ¿Dónde va el filtrado? | Frontend si < 100 eventos, backend si > 100 |
| ¿Cuántos endpoints en total? | 9 esenciales + 1 opcional = 10 total |

---

## 📚 Documentación Completa

**Ver:** `BackEnd.md` (actualizado con todos los cambios)

**Incluye:**
- ✅ Especificación completa de `POST /api/register`
- ✅ Especificación actualizada de `POST /api/authenticate`
- ✅ Especificación opcional de `GET /api/eventos-consulta/buscar`
- ✅ Checklist completo de implementación
- ✅ Resumen de endpoints

---

**Última actualización:** 13 Diciembre 2025  
**Estado:** ✅ Especificaciones completas y listas para implementar  
**Próximo paso:** Implementar `POST /api/register` en el backend

---

## 🎯 TL;DR (Resumen Muy Corto)

**Lo que necesitas implementar:**

1. ✅ **`POST /api/register`** - Crear cuenta (NUEVO)
   - Username, email, password
   - Validar únicos, hashear password

2. ✅ **9 endpoints existentes** - Ya especificados en `BackEnd.md`

3. ⚪ **`GET /api/eventos-consulta/buscar`** - Búsqueda (OPCIONAL)
   - Solo si > 100 eventos
   - Alternativa: filtrar en frontend

**Total:** 9-10 endpoints según necesidad de búsqueda.

