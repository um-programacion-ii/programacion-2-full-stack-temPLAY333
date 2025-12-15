# Configuración de DBeaver para Base de Datos de Producción

## Problema
Error: "Public Key Retrieval is not allowed"

## Solución

### Opción 1: Agregar parámetro en la URL de conexión

1. Abre DBeaver
2. Crea una nueva conexión MySQL o edita la existente
3. En la pestaña **Main**:
   - **Host**: `localhost`
   - **Port**: `3307` (puerto de producción)
   - **Database**: `MicroservicesFinal_prod`
   - **Username**: `produser`
   - **Password**: `prodpass123`

4. Ve a la pestaña **Driver properties** (o **Connection settings**)
5. Agrega una nueva propiedad:
   - **Property name**: `allowPublicKeyRetrieval`
   - **Value**: `true`

### Opción 2: Modificar la URL de conexión directamente

En la pestaña **Main**, en el campo **URL**, asegúrate de que tenga:
```
jdbc:mysql://localhost:3307/MicroservicesFinal_prod?allowPublicKeyRetrieval=true&useSSL=false
```

### Opción 3: Configuración completa recomendada

**Main Tab:**
- Server Host: `localhost`
- Port: `3307`
- Database: `MicroservicesFinal_prod`
- Username: `produser`
- Password: `prodpass123`

**Driver Properties Tab:**
Agrega estas propiedades:
- `allowPublicKeyRetrieval` = `true`
- `useSSL` = `false`
- `useUnicode` = `true`
- `characterEncoding` = `utf8`

**Connection URL resultante:**
```
jdbc:mysql://localhost:3307/MicroservicesFinal_prod?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=utf8
```

## Verificación

Después de configurar, prueba la conexión haciendo clic en **Test Connection**. Deberías ver un mensaje de éxito.

## Nota de Seguridad

El parámetro `allowPublicKeyRetrieval=true` permite que el cliente MySQL recupere la clave pública del servidor. Esto es necesario para MySQL 8.0+ cuando se usa autenticación `caching_sha2_password`. 

**En producción real**, considera:
- Usar certificados SSL/TLS
- Configurar usuarios con permisos específicos
- No exponer la base de datos directamente al exterior
