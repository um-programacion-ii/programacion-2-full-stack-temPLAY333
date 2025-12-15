# Guía para Mobile: Registro, Login y Uso del Token (Backend)

Este documento explica de forma clara y práctica cómo debe integrarse la app Mobile con el Backend para: creación de usuarios, autenticación (JWT), uso del token en peticiones y consideraciones sobre el envío de códigos por correo (`sendCodeMail`).

Fecha: 2025-12-14

---

## 1) Resumen rápido
- Endpoint de registro: `POST /api/register` — crea la cuenta (en este backend la creación NO exige confirmar el email inmediatamente).
- Endpoint de login: `POST /api/authenticate` — devuelve un JWT en el campo `id_token`.
- Para llamadas protegidas: incluir cabecera `Authorization: Bearer <id_token>`.
- El token JWT tiene validez configurada en el backend (por defecto 86400s = 24h en este proyecto).
- `sendCodeMail` (o función similar): envía un código al email, pero en esta implementación NO bloquea la creación de la cuenta — sirve solo como ayuda/registro opcional.

---

## 2) Endpoints relevantes y formatos

### 2.1 Registro de usuario
- URL: `POST /api/register`
- Propósito: crear un usuario en el sistema.
- Request (JSON):
```json
{
  "username": "juan_perez",
  "email": "juan@ejemplo.com",
  "password": "MiPassSegura123"
}
```
- Response:
  - 201 Created: usuario creado correctamente.
  - 400 Bad Request: validación fallida (username/email en uso, password inválida, etc.).

**Nota importante**: en esta instalación el backend crea la cuenta inmediatamente. No es obligatorio confirmar el correo para que el usuario pueda iniciar sesión. Esto facilita onboarding en Mobile, pero si necesitás confirmación de email, el flujo extra (verificar un código) puede implementarse y revisarse más abajo.


### 2.2 Login / Autenticación (JWT)
- URL: `POST /api/authenticate`
- Request (JSON):
```json
{
  "username": "juan_perez",
  "password": "MiPassSegura123"
}
```
- Response (200 OK):
```json
{
  "id_token": "<JWT_TOKEN>",
  "token_type": "Bearer"
}
```

- El `id_token` es un JWT que el Mobile debe guardar de forma segura (keychain / secure storage).
- Uso en llamadas posteriores:
  - Cabecera HTTP: `Authorization: Bearer <id_token>`

### 2.3 Renovación / caducidad
- En el backend actual la configuración de validez del token (JHipster) suele ser de 86400 segundos (24 horas):
  - `jhipster.authentication.jwt.token-validity-in-seconds: 86400`
- Si el backend no implementa endpoint de `refresh token`, la Mobile debe:
  - Detectar 401/403 en peticiones y pedir al usuario que vuelva a iniciar sesión, o
  - Implementar silent-login si el backend soporta `refresh` — actualmente no habilitado por defecto.


### 2.4 Logout
- No existe un endpoint obligatorio para logout (JWT es stateless).
- Implementación Mobile: eliminar token localmente (secure storage) y redirigir a la pantalla de login.

---

## 3) Flujo recomendado en la app Mobile (paso a paso)

1. Pantalla de registro: pedir `username`, `email`, `password`. Enviar a `POST /api/register`.
2. Si registro OK, automáticamente mostrar pantalla de login o, si se desea, hacer login automático (llamar a `/api/authenticate`).
3. Al recibir `id_token`, guardarlo en almacenamiento seguro (Keychain en iOS, Keystore/EncryptedSharedPreferences en Android).
4. Incluir `Authorization: Bearer <id_token>` en todas las peticiones a endpoints protegidos.
5. Manejar 401:
   - Intentar refrescar token si hay soporte (no presente por defecto). Si no, pedir re-login.
6. Logout: borrar token y limpiar cachés locales.


## 4) Manejo de errores y mensajes a mostrar en Mobile
- 400 en registro: mostrar la lista de validaciones (usuario/email inválido o existente). El backend devuelve errores localizables.
- 401 en login: mostrar "Usuario o contraseña incorrectos" y permitir reintento.
- 403 en endpoints: mostrar mensaje genérico "No autorizado" y forzar re-login o mostrar pantalla de permisos.
- Timeouts y caídas de red: mostrar un mensaje "Sin conexión" y permitir reintento.

---

## 5) `sendCodeMail` (comentario, comportamiento actual y recomendaciones)

En el backend existe (o puede existir) una función llamada `sendCodeMail`, `sendActivationCode` o similar. En muchos proyectos sirve para enviar un código o enlace al email del usuario para confirmar su cuenta. A continuación se detalla cómo se comporta en este proyecto y qué debe hacer Mobile:

### 5.1 Comportamiento actual en este proyecto
- `sendCodeMail` está implementada para **enviar un código o enlace** al email del usuario.
- **Importante**: en la configuración actual del Backend, **la creación de la cuenta no está bloqueada por la verificación del email**. Eso significa:
  - `POST /api/register` crea la cuenta inmediatamente y retorna 201.
  - `sendCodeMail` solo envía un código informativo/auxiliar.
  - El usuario puede iniciar sesión sin haber confirmado el email.

### 5.2 Por qué está así (ventajas/desventajas)
- Ventajas:
  - Mejor conversión en onboarding: el usuario puede usar la app inmediatamente.
  - Menor fricción en pruebas y MVP.
- Desventajas:
  - Riesgo de cuentas con emails falsos o typo en email.
  - Menor seguridad para acciones críticas (recuperación de cuenta, notificaciones).

### 5.3 Recomendaciones para Mobile (UX)
- Tratar la verificación de email como **opcional** en el onboarding:
  - Después del registro, mostrar una pantalla: "Hemos enviado un código a tu email (opcional)." Opciones: "Saltar" o "Ingresar código".
  - Si el usuario ingresa el código y existe endpoint para confirmar, llamar a dicho endpoint y mostrar confirmación.
  - Si el backend no expone un endpoint de confirmación de código, entonces `sendCodeMail` solo tiene rol informativo — ignorarlo en la app o implementarlo como mejora futura.

### 5.4 Si se desea exigir verificación en el futuro
- Cambios en Backend necesarios:
  - Hacer que `POST /api/register` cree la cuenta en estado `INACTIVE` y que no permita login hasta confirmación.
  - Implementar endpoint `POST /api/account/confirm` o `POST /api/activate` que acepte el código y active la cuenta.
  - Adaptar Mobile para no permitir acceso hasta confirmar el correo o para habilitar funciones limitadas.

---

## 6) Ejemplos prácticos (cURL y snippets móviles)

### 6.1 Registro (cURL)
```bash
curl -X POST "http://localhost:8081/api/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"juan_perez","email":"juan@ejemplo.com","password":"MiPassSegura123"}'
```

### 6.2 Login (cURL)
```bash
curl -X POST "http://localhost:8081/api/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username":"juan_perez","password":"MiPassSegura123"}'
```
Respuesta esperada:
```json
{
  "id_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "token_type": "Bearer"
}
```

### 6.3 Swift (iOS) — Ejemplo simple para login
```swift
let url = URL(string: "http://localhost:8081/api/authenticate")!
var req = URLRequest(url: url)
req.httpMethod = "POST"
req.setValue("application/json", forHTTPHeaderField: "Content-Type")
let body = ["username": "juan_perez", "password": "MiPassSegura123"]
req.httpBody = try! JSONSerialization.data(withJSONObject: body)

URLSession.shared.dataTask(with: req) { data, resp, err in
  // parse JSON, extraer id_token y guardarlo en Keychain
}.resume()
```

### 6.4 Android (Kotlin) — Ejemplo con OkHttp
```kotlin
val client = OkHttpClient()
val json = JSONObject()
json.put("username", "juan_perez")
json.put("password", "MiPassSegura123")
val body = json.toString().toRequestBody("application/json".toMediaType())
val req = Request.Builder()
    .url("http://localhost:8081/api/authenticate")
    .post(body)
    .build()
client.newCall(req).enqueue(object : Callback { ... })
```

---

## 7) Consideraciones de seguridad y privacidad
- Guardar el `id_token` en almacenamiento seguro.
- No almacenar la contraseña en texto plano en el dispositivo.
- Usar HTTPS en producción para todas las comunicaciones (certificados válidos).
- Manejar expiración del token y revocación si se implementa.

---

## 8) Preguntas frecuentes (FAQ)
- P: ¿Debo esperar a la confirmación de email para permitir login?  
  R: No en la configuración actual; la cuenta queda activa al registrarse. Si se requiere confirmación, el backend debe cambiar la política.

- P: ¿Qué pasa si el token expira durante el uso?  
  R: La Mobile debe detectar 401 y pedir re-login (o refrescar si se implementa refresh tokens).

- P: ¿Qué uso tiene `sendCodeMail` si no bloquea creación?  
  R: Principalmente para verificación opcional, notificaciones y recuperación de cuenta; útil para mejorar confianza del usuario.

---

## 9) Próximos pasos propuestos (si quieres implementar verificación real)
1. Implementar endpoint `POST /api/account/confirm` que reciba `{ username, code }` y active cuenta.
2. Modificar `POST /api/register` para crear usuario en `PENDING` y sólo permitir login si `ACTIVE`.
3. Añadir endpoints de reenvío de código (`POST /api/account/resend-code`).
4. Actualizar Mobile para mostrar flujo de verificación (pantalla de código).

---

Si querés, puedo:
- Generar el patch backend para habilitar la confirmación de email (endpoints + DB flag), o
- Implementar en Mobile un flujo opcional de verificación basado en `sendCodeMail`.

Decime cuál prefieres y lo implemento o lo describo en detalle.

