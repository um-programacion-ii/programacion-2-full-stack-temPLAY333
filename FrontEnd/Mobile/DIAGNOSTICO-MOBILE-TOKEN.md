# 🔍 Diagnóstico: Mobile muestra usuario incorrecto

## ✅ Confirmación del Problema

**Backend funciona correctamente:**
- Los logs muestran que `/api/account` devuelve correctamente `admin` cuando se llama desde Postman
- El backend está extrayendo correctamente el usuario del JWT

**El problema está en Mobile:**
- Mobile muestra datos de usuario `user` cuando debería mostrar `admin`
- Esto indica que Mobile está usando un token JWT viejo o cacheado

---

## 🔎 Posibles Causas

### 1. **Token JWT Viejo (MÁS PROBABLE)**
Mobile está usando un token JWT que fue generado cuando se autenticó como `user`, y no está actualizando el token después de hacer login como `admin`.

**Síntomas:**
- Hiciste login como `admin` en Mobile
- Pero Mobile sigue usando el token de `user` guardado anteriormente
- El token viejo todavía es válido (no ha expirado)

**Solución:**
- **Eliminar el token guardado** antes de hacer login
- **Guardar el nuevo token** después de hacer login exitoso
- **Verificar que el token se actualice** en cada login

### 2. **Cache de Respuesta**
Mobile está cacheando la respuesta de `/api/account` de una sesión anterior.

**Síntomas:**
- La primera llamada después de login muestra datos correctos
- Pero luego muestra datos viejos (cacheados)

**Solución:**
- **Limpiar cache** después de logout/login
- **No cachear** respuestas de `/api/account`
- **Invalidar cache** cuando cambia el usuario

### 3. **Token no se actualiza en Storage**
Mobile guarda el token en storage, pero no lo actualiza cuando hace login con otro usuario.

**Síntomas:**
- Haces login como `admin`
- Pero el token guardado sigue siendo el de `user`

**Solución:**
- **Sobrescribir** el token en storage después de cada login exitoso
- **Verificar** que el token se guarde correctamente

---

## 🛠️ Cómo Diagnosticar en Mobile

### Paso 1: Verificar qué token está usando Mobile

Agrega logs en Mobile para ver:
1. **Token guardado en storage:**
   ```javascript
   // Ejemplo (ajustar según tu framework)
   const token = await SecureStorage.getItem('jwt_token');
   console.log('Token guardado:', token);
   ```

2. **Token enviado en la petición:**
   ```javascript
   // Verificar el header Authorization
   console.log('Authorization header:', `Bearer ${token}`);
   ```

3. **Decodificar el JWT para ver qué usuario contiene:**
   ```javascript
   // Decodificar el payload del JWT
   const payload = JSON.parse(atob(token.split('.')[1]));
   console.log('Usuario en JWT:', payload.sub);
   console.log('User ID en JWT:', payload.userId);
   ```

### Paso 2: Verificar el flujo de login

1. **Antes de hacer login:**
   - Eliminar el token viejo del storage
   - Limpiar cualquier cache

2. **Después de hacer login:**
   - Verificar que recibiste un nuevo token
   - Verificar que el token se guardó en storage
   - Decodificar el token y verificar que contiene el usuario correcto

3. **Antes de llamar a `/api/account`:**
   - Leer el token del storage
   - Verificar que es el token correcto (decodificar y ver el `sub`)

### Paso 3: Comparar tokens

1. **Token de Postman (funciona):**
   - Decodifica el token que usaste en Postman
   - Anota el `sub` (debería ser `admin`)

2. **Token de Mobile (no funciona):**
   - Decodifica el token que Mobile está usando
   - Compara el `sub` (probablemente sea `user`)

---

## 🔧 Soluciones Recomendadas

### Solución 1: Limpiar token en logout/login

```javascript
// Al hacer logout
async function logout() {
    // Eliminar token
    await SecureStorage.deleteItem('jwt_token');
    // Limpiar cache
    await clearCache();
    // Redirigir a login
    navigateToLogin();
}

// Al hacer login
async function login(username, password) {
    // 1. Eliminar token viejo primero
    await SecureStorage.deleteItem('jwt_token');
    
    // 2. Hacer login
    const response = await fetch(`${BASE_URL}/api/authenticate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    const newToken = data.id_token;
    
    // 3. Guardar nuevo token
    await SecureStorage.setItem('jwt_token', newToken);
    
    // 4. Verificar que se guardó correctamente
    const savedToken = await SecureStorage.getItem('jwt_token');
    console.log('Token guardado:', savedToken === newToken ? 'OK' : 'ERROR');
    
    // 5. Decodificar y verificar usuario
    const payload = JSON.parse(atob(newToken.split('.')[1]));
    console.log('Usuario autenticado:', payload.sub);
}
```

### Solución 2: No cachear respuestas de `/api/account`

```javascript
// Al obtener cuenta
async function getAccount() {
    const token = await SecureStorage.getItem('jwt_token');
    
    const response = await fetch(`${BASE_URL}/api/account`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Cache-Control': 'no-cache' // No cachear
        }
    });
    
    const account = await response.json();
    
    // Verificar que el usuario coincide con el token
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (account.login !== payload.sub) {
        console.error('INCONSISTENCIA: Token tiene', payload.sub, 'pero API devuelve', account.login);
        // Forzar re-login
        await logout();
        return;
    }
    
    return account;
}
```

### Solución 3: Verificar token antes de cada petición

```javascript
// Interceptor para verificar token
async function verifyTokenBeforeRequest() {
    const token = await SecureStorage.getItem('jwt_token');
    
    if (!token) {
        navigateToLogin();
        return null;
    }
    
    // Decodificar token
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const now = Math.floor(Date.now() / 1000);
        
        // Verificar expiración
        if (payload.exp < now) {
            console.log('Token expirado, forzar re-login');
            await logout();
            return null;
        }
        
        console.log('Token válido para usuario:', payload.sub);
        return token;
    } catch (e) {
        console.error('Error decodificando token:', e);
        await logout();
        return null;
    }
}
```

---

## 📋 Checklist para Mobile

- [ ] **Al hacer logout:** Eliminar token del storage
- [ ] **Al hacer login:** Eliminar token viejo antes de guardar el nuevo
- [ ] **Después de login:** Verificar que el token se guardó correctamente
- [ ] **Antes de usar token:** Decodificar y verificar que contiene el usuario correcto
- [ ] **No cachear:** Respuestas de `/api/account`
- [ ] **Logs:** Agregar logs para ver qué token se está usando
- [ ] **Verificar expiración:** Verificar que el token no haya expirado antes de usarlo

---

## 🧪 Test para Verificar

1. **Logout completo:**
   - Cierra sesión en Mobile
   - Verifica que el token se eliminó del storage

2. **Login como admin:**
   - Inicia sesión como `admin`
   - Verifica que recibiste un nuevo token
   - Decodifica el token y verifica que `sub` es `admin`

3. **Llamar a `/api/account`:**
   - Llama al endpoint con el token
   - Verifica que devuelve datos de `admin`
   - Compara con lo que muestra Postman

4. **Si sigue mostrando `user`:**
   - Revisa los logs para ver qué token se está enviando
   - Compara el token de Mobile con el de Postman
   - Verifica que no haya cache

---

## 💡 Recomendación Final

**El problema más probable es que Mobile está usando un token JWT viejo.**

**Solución inmediata:**
1. Hacer logout completo en Mobile
2. Eliminar manualmente el token del storage (si es posible)
3. Hacer login nuevamente como `admin`
4. Verificar que el nuevo token se guardó correctamente

**Solución a largo plazo:**
1. Implementar limpieza de token en logout
2. Implementar verificación de token antes de cada petición
3. Agregar logs para diagnosticar problemas futuros
4. No cachear respuestas de endpoints de usuario

---

## 📞 Si el Problema Persiste

Si después de implementar estas soluciones el problema persiste:

1. **Captura los logs de Mobile** mostrando:
   - Token guardado en storage
   - Token enviado en la petición
   - Payload decodificado del token
   - Respuesta de `/api/account`

2. **Compara con Postman:**
   - Token usado en Postman
   - Payload decodificado del token de Postman
   - Respuesta de `/api/account` en Postman

3. **Verifica la configuración:**
   - URL base del backend
   - Headers enviados
   - Manejo de errores 401/403
