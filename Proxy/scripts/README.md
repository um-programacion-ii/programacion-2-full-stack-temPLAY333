# Scripts de Testing

Esta carpeta contiene scripts de PowerShell para probar los endpoints de manera interactiva.

## 📄 Archivos Disponibles

### 1. `test-proxy-endpoints.ps1` ⭐ (RECOMENDADO)
**Prueba los endpoints DEL PROXY** (http://localhost:8080)

- ✅ **Usa el Proxy local**: No necesitas token JWT manual
- ✅ **Autenticación automática**: El Proxy maneja el JWT con la cátedra
- ✅ **Rutas del Proxy**: `/api/eventos/resumidos`, `/api/ventas`, etc.
- ✅ **Endpoints de admin**: Health check, estado del token, renovar token

**Cuándo usarlo:**
- Para probar que el Proxy funciona correctamente
- Para verificar la integración entre Proxy y Cátedra
- Para desarrollo del Backend (el Backend llama al Proxy, no a la Cátedra)

**Ejecución:**
```powershell
.\scripts\test-proxy-endpoints.ps1
```

**Requisitos:**
- El Proxy debe estar corriendo: `mvn spring-boot:run`
- El Proxy debe tener credenciales configuradas en `.env`

---

### 2. `test-endpoints-jwt.ps1`
**Prueba los endpoints DE LA CÁTEDRA DIRECTAMENTE** (http://192.168.194.250:8080)

- ⚠️ **Llama directamente a la cátedra**: Sin pasar por el Proxy
- 🔑 **Requiere token JWT manual**: Debes tenerlo en el `.env` como `JWT_TOKEN`
- 🌐 **Rutas de la cátedra**: `/api/endpoints/v1/eventos-resumidos`, etc.
- 📋 **Útil para debugging**: Ver si la cátedra responde correctamente

**Cuándo usarlo:**
- Para verificar que la cátedra está accesible
- Para obtener/renovar el token JWT manualmente
- Para debugging de problemas de conectividad

**Ejecución:**
```powershell
.\scripts\test-endpoints-jwt.ps1
```

**Requisitos:**
- La API de la cátedra debe estar accesible desde tu red
- Necesitas un token JWT válido en el `.env`

---

## 🔄 Diferencias Clave

| Característica | test-proxy-endpoints.ps1 | test-endpoints-jwt.ps1 |
|----------------|--------------------------|------------------------|
| **Target** | Proxy (localhost:8080) | Cátedra (192.168.194.250:8080) |
| **Autenticación** | Automática (Proxy) | Manual (JWT en .env) |
| **Rutas** | `/api/eventos/*` | `/api/endpoints/v1/*` |
| **Admin endpoints** | ✅ Sí | ❌ No |
| **Uso recomendado** | Desarrollo normal | Debugging/Testing directo |

---

## 📝 Ejemplos de Uso

### Probar el Proxy (Recomendado)

```powershell
# 1. Inicia el Proxy
mvn spring-boot:run

# 2. En otra terminal, ejecuta el script
.\scripts\test-proxy-endpoints.ps1

# 3. Prueba las opciones:
#    - Opción 2: Listar eventos resumidos
#    - Opción B: Ver estado del token JWT
#    - Opción C: Renovar token JWT
```

### Probar la Cátedra Directamente

```powershell
# 1. Asegúrate de tener el JWT_TOKEN en el .env
# 2. Ejecuta el script
.\scripts\test-endpoints-jwt.ps1

# 3. Si necesitas un nuevo token:
#    - Opción 2: Login (obtendrás un nuevo token)
#    - Copia el token y agrégalo al .env
```

---

## 🐛 Solución de Problemas

### "Array vacío []" o "Respuesta: (null)"

**Causa**: La respuesta es válida pero no hay datos.

**Solución**: Esto es normal si no hay eventos/ventas en el sistema. Ambos scripts ahora muestran esta información claramente.

### "ERROR (Status: 401)" en test-proxy-endpoints.ps1

**Causa**: El Proxy no pudo autenticarse con la cátedra.

**Solución**:
```powershell
# Verifica el estado del token del Proxy
.\scripts\test-proxy-endpoints.ps1
# Selecciona opción B (Estado del token JWT)
# Si no hay token, verifica las credenciales en .env
```

### "ERROR (Status: 401)" en test-endpoints-jwt.ps1

**Causa**: El token JWT en `.env` está expirado o es inválido.

**Solución**:
```powershell
# 1. Usa la opción 2 (Login) para obtener un nuevo token
# 2. O usa el Proxy que maneja el token automáticamente
```

---

## 📁 Carpeta payloads/

Ambos scripts crean archivos JSON en `scripts/payloads/` con plantillas para los requests POST:

- `proxy-login.json` - Login (Proxy)
- `proxy-bloquear-asientos.json` - Bloquear asientos (Proxy)
- `proxy-realizar-venta.json` - Realizar venta (Proxy)
- `payload-2-login.json` - Login (Cátedra)
- `payload-6-bloquear-asientos.json` - Bloquear asientos (Cátedra)
- `payload-7-realizar-venta.json` - Realizar venta (Cátedra)

Puedes editar estos archivos directamente o usar la opción de editar en el Bloc de notas cuando el script lo solicite.

---

## 💡 Recomendación

**Para desarrollo normal**: Usa `test-proxy-endpoints.ps1`

El Proxy maneja automáticamente la autenticación, renovación de token, y proporciona endpoints adicionales de administración. Es la forma recomendada de trabajar con el sistema.

**Para debugging avanzado**: Usa `test-endpoints-jwt.ps1`

Si necesitas verificar que la cátedra responde correctamente o depurar problemas de conectividad, este script te permite interactuar directamente con la API de la cátedra.

