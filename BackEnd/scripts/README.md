# 📜 Scripts del Backend

Scripts esenciales para ejecutar y probar el Backend.

---

## 🚀 Scripts de Inicio

### `iniciar-dockers.ps1`
Inicia todos los servicios Docker necesarios (MySQL dev, MySQL prod, Redis).

```powershell
.\scripts\iniciar-dockers.ps1
```

**Servicios**:
- MySQL Dev (puerto 3306)
- MySQL Prod (puerto 3307)
- Redis (puerto 6379)

**Nota**: El Backend NO usa Kafka. El Proxy consume Kafka de la Cátedra.

**Uso**: Ejecutar PRIMERO antes de iniciar Backend.

---

### `iniciar-backend-dev.ps1`
Inicia el Backend en modo **DESARROLLO**.

```powershell
.\scripts\iniciar-backend-dev.ps1
```

**Características**:
- ✅ BD: `MicroservicesFinal` (puerto 3306)
- ✅ Sincroniza eventos al iniciar
- ✅ Logs en DEBUG
- ✅ Puerto: 8081

**Uso**: Para desarrollo diario.

---

### `iniciar-backend-prod.ps1`
Inicia el Backend en modo **PRODUCCIÓN**.

```powershell
.\scripts\iniciar-backend-prod.ps1
```

**Características**:
- ✅ BD: `MicroservicesFinal_prod` (puerto 3307)
- ❌ NO sincroniza al iniciar (manual)
- ✅ Logs en INFO
- ✅ Puerto: 8081

**Uso**: Para staging/producción.

**Importante**: Después de iniciar, ejecutar sincronización manual:
```powershell
curl -X POST http://localhost:8081/api/eventos-sync/manual
```

---

## 🧪 Scripts de Testing

### `test-sincronizacion-eventos.ps1`
Prueba la sincronización de eventos desde el Proxy hacia la BD local.

```powershell
.\scripts\test-sincronizacion-eventos.ps1
```

**Verifica**:
1. Estado del servicio de sincronización
2. Fuerza sincronización manual
3. Consulta eventos guardados en BD

**Requiere**: Backend y Proxy ejecutándose.

---

### `test-webhooks.ps1`
Prueba los webhooks que reciben eventos de Kafka del Proxy.

```powershell
.\scripts\test-webhooks.ps1
```

**Prueba**:
- ✅ `EVENTO_CAMBIADO`
- ✅ `ASIENTOS_BLOQUEADOS`
- ✅ `VENTA_COMPLETADA`

**Uso**: Simula que el Proxy envía notificaciones de eventos Kafka.

**Requiere**: Solo Backend ejecutándose (simula el Proxy).

---

## 🗄️ Scripts de Base de Datos

### `ver-bd-desarrollo.ps1`
Muestra datos de la BD de **desarrollo**.

```powershell
.\scripts\ver-bd-desarrollo.ps1
```

**Conexión**:
- Host: localhost:3306
- BD: MicroservicesFinal
- Usuario: devuser / devpass

---

### `ver-bd-produccion.ps1`
Muestra datos de la BD de **producción**.

```powershell
.\scripts\ver-bd-produccion.ps1
```

**Conexión**:
- Host: localhost:3307
- BD: MicroservicesFinal_prod
- Usuario: produser / prodpass123

---

## 📁 Directorio `payloads/`

Contiene payloads JSON de ejemplo para testing de webhooks:

- `webhook-evento-cambiado.json` - Evento modificado
- `webhook-asientos-bloqueados.json` - Asientos bloqueados
- `webhook-venta-completada.json` - Venta completada

**Uso**: Usados por `test-webhooks.ps1` automáticamente.

---

## 🔄 Flujo Típico de Trabajo

### Primera Vez (Setup)

```powershell
# 1. Iniciar Docker
.\scripts\iniciar-dockers.ps1

# 2. Esperar 10 segundos a que Docker inicie
Start-Sleep -Seconds 10

# 3. Verificar contenedores
docker ps

# 4. Iniciar Proxy (en otro proyecto)
cd ..\Proxy
mvn spring-boot:run

# 5. Volver a BackEnd e iniciar en DEV
cd ..\BackEnd
.\scripts\iniciar-backend-dev.ps1

# Esperar a que termine de iniciar y sincronizar...
# Ver logs: "Sincronización de eventos completada exitosamente"
```

### Desarrollo Diario

```powershell
# 1. Verificar Docker
docker ps

# 2. Iniciar Backend DEV
.\scripts\iniciar-backend-dev.ps1
```

### Testing

```powershell
# En otra terminal (mientras Backend corre)

# Probar sincronización
.\scripts\test-sincronizacion-eventos.ps1

# Probar webhooks
.\scripts\test-webhooks.ps1

# Ver datos en BD
.\scripts\ver-bd-desarrollo.ps1
```

---

## ⚠️ Notas Importantes

1. **Docker primero**: Siempre iniciar Docker ANTES que Backend
2. **Proxy opcional**: El Backend puede iniciar sin Proxy, pero no sincronizará eventos
3. **Perfil por defecto**: Si ejecutas `mvn spring-boot:run` sin script, usa perfil `dev`
4. **Puertos**:
   - Backend: 8081
   - Proxy: 8080
   - MySQL Dev: 3306
   - MySQL Prod: 3307
   - Redis: 6379
   
5. **Kafka NO está aquí**: El Proxy consume Kafka de la Cátedra (192.168.194.250:9092)

---

## 🐛 Troubleshooting

### "Docker no está ejecutándose"
**Solución**: Iniciar Docker Desktop y ejecutar `.\scripts\iniciar-dockers.ps1`

### "Proxy no responde"
**Solución**: El Backend iniciará pero no sincronizará. Inicia el Proxy en el otro proyecto.

### "Connection refused" en puerto 8080
**Solución**: El Proxy no está ejecutándose o Backend intenta usar puerto 8080 (debe ser 8081).

### BD vacía después de iniciar
**Solución**: 
- Verifica que el Proxy esté ejecutándose
- Fuerza sincronización: `curl -X POST http://localhost:8081/api/eventos-sync/manual`
- Ver logs del Backend para errores

---

## 📚 Documentación Relacionada

- `../GESTION-PERFILES-BD.md` - Gestión de perfiles y BD
- `../GUIA-BACKEND-PROXY-KAFKA.md` - Arquitectura completa
- `../RESUMEN-FINAL-ARQUITECTURA.md` - Resumen de arquitectura
- `../PRUEBA-SINCRONIZACION.md` - Cómo probar sincronización

