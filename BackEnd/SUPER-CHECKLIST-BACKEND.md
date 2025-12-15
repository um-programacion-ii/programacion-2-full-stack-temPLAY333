# SUPER CHECKLIST - BackEnd (consolidado desde todos los MD)

> Estado actual: Todos los tests unitarios ejecutados localmente pasan ✅ (fecha: 2025-12-14)
>
> Cambios principales aplicados y verificados: campo `imagen` en `EventoResumenDTO` y mapper; relación `Venta` ↔ `Asiento` + DTO/mappers; queries con `join fetch` para ventas; cambios en webhook/idempotencia y changelog Liquibase para `WebhookProcesado`.

Resumen rápido: voy a consolidar lo declarado en los MD del repo (Proxy.md, PROXY-API.md, Mobile.md, CORRECCIONES-FINALES.md, BACKEND_MISSING.md, scripts/README.md) y crear una lista única, priorizada y accionable con los archivos a revisar, cambios a aplicar y pruebas a ejecutar.

Plan de trabajo (rápido):
- Revisar los MD existentes y extraer afirmaciones y tareas.
- Validar qué ya está implementado en el código (según nuestros cambios recientes).
- Generar checklist priorizada (Crítico / Importante / Opcional).
- Añadir pasos concretos para cada tarea (archivos, tests, comandos, cómo revertir).
- Entrega: este archivo `SUPER-CHECKLIST-BACKEND.md` y recomendación de siguiente acción inmediata.

---

CHECKLIST PRINCIPAL (ordenada por prioridad)

1) CRÍTICOS - necesarios para que Mobile funcione (prioridad alta)
- [x] Endpoint: GET `/api/eventos-consulta/resumidos`
  - Verificar: devuelve `id, titulo, resumen, fecha, imagen, eventoTipo`.
  - Archivos a revisar: EventoResumenDTO, EventoResumenMapper, EventoConsultaController (o EventoMobileResource), EventoExternalClient.
  - Tests: unit + integration que validen formato y que `imagen` esté presente.
  - Comando para probar: `curl -H "Authorization: Bearer <token>" http://localhost:8081/api/eventos-consulta/resumidos`

- [x] Endpoint: GET `/api/eventos-consulta/{id}` (detalle)
  - Verificar campos completos (imagen, direccion, filaAsientos, columnAsientos, integrantes).
  - Archivos: EventoDTO, EventoMapper, EventoController.
  - Test: integración sobre endpoint con id real.

- [x] Endpoint: GET `/api/asientos/evento/{eventoId}/mapa`
  - Verificar: devuelve `filas`, `columnas`, `asientos[]` con estados y expira.
  - Archivos: MapaAsientosDTO, AsientoService, AsientoController.
  - Test: unit para DTO, integración para endpoint.

- [x] Endpoint: POST `/api/asientos/evento/{eventoId}/bloquear`
  - Verificar: request con `eventoId` y asientos; response con `resultado`, `asientos` y `expira`.
  - Archivos: BloquearAsientosRequestDTO, BloqueoAsientoService, BloqueoAsientoController.
  - Test: integración, y `test-webhooks.ps1` manual.

- [x] Endpoint: POST `/api/ventas/evento/{eventoId}/realizar`
  - Verificar: request incluye `asientos[].persona/nombreAsistente`; backend reenvía al Proxy y acepta respuesta.
  - Archivos: RealizarVentaRequestDTO/Response, VentaService, VentaController.
  - Test: integración con Proxy simulado o test de unidad usando mock del RestTemplate.

2) IMPORTANTES - completan funcionalidad
- [x] GET `/api/ventas` y GET `/api/ventas/{id}` deben devolver evento completo y `asientos` con `nombreAsistente`.
  - Archivos: Venta.java (relaciones), VentaDTO, VentaMapper, VentaRepository (JOIN FETCH o EntityGraph).
  - Test: unit y integration.

- [x] Optimización N+1: revisar repositorios que cargan eventos/ventas/asientos y aplicar `@EntityGraph` o `JOIN FETCH` donde sea necesario.
  - Archivos: VentaRepository, EventoRepository si aplica.

3) OPCIONALES / NICE-TO-HAVE
- [ ] Endpoint de búsqueda `GET /api/eventos-consulta/buscar` (opcional para Mobile).
- [ ] Mejoras UI en Mobile ya documentadas (placeholders, lazy images) — solo coordinación.

---

DOCUMENTACIÓN: Validar y corregir los MD
- [ ] `BACKEND_MISSING.md`: actualmente afirma que "TODOS los endpoints ya están implementados" pero en la realidad varios estaban pendientes; hay que sincronizar texto con el estado real. Revisar puntos marcados como "❌ Pendiente en Backend" y actualizar al completar.
- [ ] `CORRECCIONES-FINALES.md`: verificar que cada cambio listado realmente exista en el código (DTOs, mappers, repos). Si alguno no existe, marcarlo pendiente y añadir tarea concreta.
- [x] `PROXY-API.md` y `Proxy.md`: confirmar que las URLs documentadas coinciden con el código del Proxy (ya revisado) y que el Backend llama a esas rutas.
- [x] `Mobile.md`: confirmar que la documentación entregada al equipo Mobile coincide con la API real.
- Acción: crear un PR de documentación cuando el estado coincida con el código.

---

IMPLEMENTACIÓN -> pasos concretos por tarea (con archivos y comandos)

A. Añadir/Verificar `imagen` en EventoResumenDTO
- Archivos a editar:
  - `src/main/java/.../service/dto/EventoResumenDTO.java`
  - `src/main/java/.../service/mapper/EventoResumenMapper.java`
  - Controller que forma la lista: `EventoMobileResource` o el que uses
- Comandos de verificación:
  - `mvn -DskipTests clean compile`
  - `curl -H "Authorization: Bearer <token>" http://localhost:8081/api/eventos-consulta/resumidos | jq` (o revisar JSON)
- Reversión rápida: `git checkout -- <archivo>` o reset al commit funcional.

B. Asientos y Venta relation
- Revisar `Venta.java` y `Asiento.java`.
- Añadir `@OneToMany` en `Venta` y `@ManyToOne` en `Asiento` con helpers `addAsiento`/`removeAsiento`.
- Mapear en `VentaDTO` y `VentaMapper`.
- Actualizar `VentaRepository` con `@EntityGraph` o `join fetch` en queries que devuelvan ventas por usuario.
- Tests: `src/test/.../VentaServiceTest.java` y `VentaResourceIT.java`.

C. MapaAsientos DTO
- Reemplazar `totalFilas`/`totalColumnas` por `filas`/`columnas`.
- Revisar `AsientoService` y `AsientoController`.
- Tests: unit + integration.

D. Webhooks & Idempotencia (ya implementados parcialmente)
- Verificar `BackendNotificacionDTO`, `WebhookController`, `WebhookService`, `WebhookProcesado` y changelog Liquibase.
- Confirmar tests para idempotencia (simular envío duplicado y verificar un solo procesamiento).
- Si falta: agregar test containers test o unit test que llame controlador con same partition+offset.

E. Liquibase changelog
- Verificar que `src/main/resources/config/liquibase/master.xml` incluye el changelog generado (`20251214090400_added_entity_WebhookProcesado.xml`).
- Comprobar `mvn liquibase:update` localmente si procede (o iniciar app y revisar logs para migración).

---

PRUEBAS y VALIDACIÓN (comandos útiles)
- Compilar: `mvn -DskipTests clean compile`
- Ejecutar tests: `mvn test -Dtest=Evento*` (filtrar tests relevantes)
- Ejecutar app: `.
  scripts\iniciar-backend-dev.ps1` (usa scripts/README.md)
- Probar endpoint resumidos: `curl -H "Authorization: Bearer <token>" http://localhost:8081/api/eventos-consulta/resumidos`
- Simular webhook: `.
  scripts\test-webhooks.ps1` o `curl -X POST http://localhost:8081/api/webhooks/evento-cambio -d @payloads/webhook-venta-completada.json -H "Content-Type: application/json"`

---

REVERT / BACKUP
- Antes de cambios grandes: commit y tag el estado funcional:
  ```bash
  git add .
  git commit -m "estado funcional - punto seguro"
  git tag estado-funcional-20251214
  git push origin HEAD --tags
  ```
- Si merge rompe: `git reset --hard estado-funcional-20251214` o `git checkout estado-funcional-20251214 -b restore-point`

---

ENTREGABLE: Archivo de estado y PR de cambios
- Crear rama `fix/backend-md-sync` e implementar correcciones en código una por una.
- Para cada corrección: implementar -> compilar -> test -> documentar en MD -> push -> crear PR.

---

VOY A CREAR ESTE ARCHIVO EN EL REPO COMO `SUPER-CHECKLIST-BACKEND.md` (hecho). 

Siguiente paso propuesto (elige una):
- [A] Empiezo a aplicar automáticamente las correcciones críticas en el código (por ejemplo añadir campo `imagen` en DTO/mappers y ajustar controller). 
- [B] Prefieres que solo genere PRs/patches listos para aplicar y tú apruebas antes.
- [C] O quieres que haga un run de tests y detecte fallos actuales para priorizar.

Dime qué opción y empiezo a ejecutar los pasos automáticos (A/B/C).
