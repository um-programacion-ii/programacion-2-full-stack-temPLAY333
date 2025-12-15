# 🎫 EventTickets - Frontend Mobile

> **Aplicación móvil para compra de entradas a eventos**  
> Kotlin Multiplatform (Android/iOS)  
> Light Mode · Púrpura/Rosa

---

## 🎨 Paleta de Colores

### Colores Principales
```kotlin
// Light Mode Base
val Background = Color(0xFFFFFFFF)           // Blanco - Fondo principal
val Surface = Color(0xFFFFFFFF)              // Blanco - Cards y superficies
val SurfaceVariant = Color(0xFFF5F5F5)       // Gris claro - Elevated surfaces

// Colores de Acento
val Primary = Color(0xFF6A5AE0)              // Púrpura vibrante - Acciones principales
val PrimaryVariant = Color(0xFF5548C8)       // Púrpura oscuro - Hover states
val Secondary = Color(0xFFE05A6A)            // Rosa coral - Acciones secundarias
val SecondaryVariant = Color(0xFFD64456)     // Rosa oscuro - Hover secundario

// Estados de Asientos
val SeatAvailable = Color(0xFF4CAF50)        // Verde - Disponible
val SeatSelected = Color(0xFF6A5AE0)         // Púrpura - Seleccionado
val SeatBlocked = Color(0xFFFFA000)          // Naranja - Bloqueado
val SeatSold = Color(0xFF757575)             // Gris - Vendido

// Textos
val TextPrimary = Color(0xFF212121)          // Casi negro - Texto principal
val TextSecondary = Color(0xFF757575)        // Gris medio - Texto secundario
val TextDisabled = Color(0xFFBDBDBD)         // Gris claro - Texto deshabilitado

// Estados
val Success = Color(0xFF388E3C)              // Verde - Éxito
val Error = Color(0xFFD32F2F)                // Rojo - Error
val Warning = Color(0xFFFFA000)              // Naranja - Advertencia
val Info = Color(0xFF0288D1)                 // Azul - Info
```

### Gradientes
```kotlin
val GradientPrimary = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6A5AE0),
        Color(0xFFE05A6A)
    )
)

val GradientBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF5F5F5)
    )
)
```

---

## 📱 Arquitectura de Pantallas

### Flujo de Navegación

```
┌─────────────────┐
│   SplashScreen  │ ────> Auto-verifica token
└────────┬────────┘
         │
         ├─ Token válido ──────────────────┐
         │                                  │
         ├─ Token inválido                 │
         │                                  ↓
         ↓                         ┌──────────────┐
┌─────────────────┐                │  HomeScreen  │
│   LoginScreen   │                │  (Eventos)   │
└────────┬────────┘                └──────┬───────┘
         │                                 │
         └─────────────────────────────────┤
                                           │
         ┌─────────────────────────────────┴──────────────┐
         │                                                 │
         ↓                                                 ↓
┌──────────────────┐                            ┌──────────────────┐
│  EventDetail     │                            │   MyPurchases    │
│  Screen          │                            │   Screen         │
└────────┬─────────┘                            └──────┬───────────┘
         │                                              │
         ↓                                              ↓
┌──────────────────┐                            ┌──────────────────┐
│  SeatMapScreen   │                            │  PurchaseDetail  │
└────────┬─────────┘                            │  Screen          │
         │                                      └──────────────────┘
         ↓
┌──────────────────┐
│  ConfirmSeats    │
│  Screen          │
└────────┬─────────┘
         │
         ↓
┌──────────────────┐
│  AttendeeNames   │
│  Screen          │
└────────┬─────────┘
         │
         ↓
┌──────────────────┐
│  PurchaseSummary │
│  Screen          │
└────────┬─────────┘
         │
         ↓
┌──────────────────┐
│  PurchaseSuccess │
│  Screen          │
└──────────────────┘
```

---

## 🖼️ Diseño de Pantallas

### 1. SplashScreen

**Propósito**: Pantalla inicial con logo y carga

**Elementos UI**:
```
┌─────────────────────────────────┐
│                                 │
│                                 │
│          [LOGO ANIMADO]         │
│          🎫 EventTickets        │
│                                 │
│      ⚡ Loading indicator       │
│                                 │
│                                 │
└─────────────────────────────────┘
```

**Lógica**:
- Verificar si existe token JWT guardado
- Si existe y es válido (< 24h) → HomeScreen
- Si no existe o expiró → LoginScreen
- Animación de fade-in del logo (500ms)
- Gradiente de fondo animado

**Componentes**:
- Logo con animación de scale/fade
- Loading indicator circular con color Primary
- Gradiente de fondo vertical

---

### 2. LoginScreen

**Propósito**: Autenticación del usuario

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← [Cerrar]                     │
│                                 │
│      🎫 EventTickets            │
│      Ingresa a tu cuenta        │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 👤 Usuario              │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🔒 Contraseña      [👁]  │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │     INICIAR SESIÓN      │   │
│  └─────────────────────────┘   │
│                                 │
│   [Mensaje de error si hay]    │
│                                 │
└─────────────────────────────────┘
```

**Campos**:
- Username (TextField con icono de usuario)
- Password (TextField con toggle para mostrar/ocultar)

**Acciones**:
- Botón "Iniciar Sesión" (Color Primary, full width)
- Validación: campos no vacíos
- Loading state mientras se autentica

**Estados**:
- **Idle**: Botón habilitado si campos llenos
- **Loading**: Botón con CircularProgressIndicator
- **Error**: Mensaje de error debajo del botón (color Error)
- **Success**: Navegación automática a HomeScreen

**API**:
- `POST /api/authenticate`
- Guardar token en almacenamiento seguro (EncryptedSharedPreferences)

---

### 3. HomeScreen (Lista de Eventos)

**Propósito**: Explorar eventos disponibles

**Elementos UI**:
```
┌─────────────────────────────────┐
│  🎫 Eventos        [👤] [🔍]    │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ [IMAGEN EVENTO]           │ │
│  │                           │ │
│  │ Concierto Rock 🎸         │ │
│  │ 15 Dic, 20:00            │ │
│  │ 🏷️ Música                 │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ [IMAGEN EVENTO]           │ │
│  │                           │ │
│  │ Festival de Jazz 🎷       │ │
│  │ 20 Dic, 19:30            │ │
│  │ 🏷️ Música                 │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
│  [🏠]  [🎟️]  [📋]             │
└─────────────────────────────────┘
```

**Componentes**:
- **TopBar**: 
  - Título "Eventos"
  - Icono de perfil (→ opciones de usuario)
  - Icono de búsqueda (→ filtro/búsqueda)

- **EventCard** (componente reutilizable):
  - Imagen del evento (AsyncImage con placeholder)
  - Título (TextPrimary, bold, 18sp)
  - Fecha y hora (TextSecondary, 14sp)
  - Categoría con icono (chip con color Secondary)
  - Fondo: Surface con elevación
  - Border radius: 16dp
  - Click → EventDetailScreen

- **Pull-to-Refresh**: Actualizar lista de eventos

- **BottomNavBar**:
  - 🏠 Eventos (seleccionado)
  - 🎟️ Mis Entradas
  - 📋 Perfil

**Estados**:
- **Loading**: Skeleton loader con shimmer effect
- **Success**: Lista de eventos
- **Empty**: "No hay eventos disponibles"
- **Error**: Mensaje con botón "Reintentar"

**API**:
- `GET /api/eventos-consulta/resumidos`

---

### 4. EventDetailScreen

**Propósito**: Ver detalles completos del evento

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ←  [❤️]  [⚙️]                  │
│ ┌───────────────────────────┐   │
│ │                           │   │
│ │   [IMAGEN FULL WIDTH]     │   │
│ │                           │   │
│ └───────────────────────────┘   │
│                                 │
│  🎸 Concierto Rock              │
│  ⭐⭐⭐⭐⭐ 4.8 (120 reviews)    │
│                                 │
│  📅 15 de Diciembre, 2025       │
│  🕐 20:00 hs                    │
│  📍 Av. Corrientes 1234         │
│  🏷️ Música                      │
│                                 │
│  ─────────────────────────      │
│                                 │
│  📖 Descripción                 │
│  Lorem ipsum dolor sit amet...  │
│                                 │
│  👥 Integrantes                 │
│  • Juan Pérez - Vocalista      │
│  • María López - Guitarrista   │
│                                 │
│  💺 10 filas × 15 columnas      │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │   COMPRAR ENTRADAS      │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Secciones**:
1. **Header Image**: 
   - Imagen a pantalla completa (ratio 16:9)
   - Overlay con gradiente para mejorar legibilidad

2. **Info Principal**:
   - Título (28sp, bold)
   - Rating con estrellas (opcional)
   - Fecha, hora, lugar con iconos
   - Categoría (chip)

3. **Descripción**:
   - Texto expandible ("Ver más" si es largo)

4. **Integrantes**:
   - Lista con avatar circular (opcional) + nombre + rol

5. **Info de Asientos**:
   - Capacidad total
   - Asientos disponibles (calcular en tiempo real)

6. **Botón de Acción**:
   - "Comprar Entradas" (Primary color, sticky al bottom)

**API**:
- `GET /api/eventos-consulta/{id}`

---

### 5. SeatMapScreen

**Propósito**: Selección visual de asientos

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Concierto Rock          [?]  │
├─────────────────────────────────┤
│                                 │
│       🎬 ESCENARIO 🎬           │
│       ═══════════════           │
│                                 │
│   F  🟢 🟢 🟡 🔴 🟢 🟢 🟢    │
│   E  🟢 🔵 🔵 🟢 🟢 🔴 🟢    │
│   D  🟢 🟢 🟢 🟢 🟢 🟢 🟢    │
│   C  🔴 🟢 🟢 🟢 🟢 🟢 🟢    │
│   B  🟢 🟢 🟢 🟢 🟢 🟢 🟢    │
│   A  🟢 🟢 🟢 🟢 🟢 🟢 🟢    │
│                                 │
│      1  2  3  4  5  6  7       │
│                                 │
│  ─────────────────────────      │
│  🟢 Disponible  🔵 Seleccionado │
│  🟡 Bloqueado   🔴 Ocupado      │
│  ─────────────────────────      │
│                                 │
│  Seleccionados: 2/4             │
│  Fila E, Asientos 2, 3          │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │     CONTINUAR (2)       │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Componentes**:

1. **Indicador de Escenario**:
   - Texto "ESCENARIO" con icono
   - Línea decorativa

2. **Grilla de Asientos**:
   - LazyVerticalGrid con scroll
   - Cada asiento es un círculo clickable
   - Estados con colores:
     - 🟢 Verde (SeatAvailable) - Disponible
     - 🔵 Azul (SeatSelected) - Seleccionado por mí
     - 🟡 Naranja (SeatBlocked) - Bloqueado por otro
     - 🔴 Rojo (SeatSold) - Vendido
   - Labels de filas (A-Z) y columnas (1-N)

3. **Leyenda**:
   - Card con los 4 estados y sus significados

4. **Resumen de Selección**:
   - Contador "X/4 seleccionados"
   - Lista de asientos seleccionados

5. **Botón de Acción**:
   - "Continuar (X)" donde X es cantidad seleccionada
   - Habilitado solo si hay al menos 1 seleccionado
   - Deshabilitado si llegó al máximo (4)

**Lógica**:
- Máximo 4 asientos por compra
- No permitir seleccionar bloqueados/vendidos
- Si un asiento bloqueado expiró, mostrarlo como disponible
- Actualizar estado cada 30 segundos (polling)

**API**:
- `GET /api/asientos/evento/{eventoId}/mapa`

---

### 6. ConfirmSeatsScreen

**Propósito**: Confirmar selección antes de bloquear

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Confirmar Selección          │
├─────────────────────────────────┤
│                                 │
│  📋 Resumen de tu selección     │
│                                 │
│  ┌───────────────────────────┐ │
│  │  Concierto Rock           │ │
│  │  15 Dic, 20:00           │ │
│  │                           │ │
│  │  💺 Asientos:             │ │
│  │  • Fila E, Asiento 2      │ │
│  │  • Fila E, Asiento 3      │ │
│  │                           │ │
│  │  💰 Total: $2,500.00      │ │
│  └───────────────────────────┘ │
│                                 │
│  ⚠️ Importante:                 │
│  Los asientos serán bloqueados  │
│  por 5 minutos. Debes completar │
│  la compra antes de que expire. │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │     BLOQUEAR ASIENTOS   │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │        VOLVER           │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Acciones**:
- **Bloquear Asientos**: Llama al API y procede
- **Volver**: Regresa al mapa para cambiar selección

**API**:
- `POST /api/asientos/evento/{eventoId}/bloquear`

---

### 7. AttendeeNamesScreen

**Propósito**: Cargar nombres de asistentes

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Datos de Asistentes     4:32 │
├─────────────────────────────────┤
│                                 │
│  ⏱️ Tiempo restante: 4:32       │
│  [████████░░] 80%              │
│                                 │
│  Ingresa los nombres de los     │
│  asistentes para cada entrada:  │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 💺 Fila E, Asiento 2      │ │
│  │ ┌─────────────────────┐   │ │
│  │ │ Nombre completo     │   │ │
│  │ └─────────────────────┘   │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 💺 Fila E, Asiento 3      │ │
│  │ ┌─────────────────────┐   │ │
│  │ │ Nombre completo     │   │ │
│  │ └─────────────────────┘   │ │
│  └───────────────────────────┘ │
│                                 │
│  ✓ Todos los campos completos  │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │       CONTINUAR         │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Componentes**:

1. **Timer de Bloqueo**:
   - Countdown timer (MM:SS)
   - Progress bar lineal
   - Cambia a color Warning cuando < 1 minuto
   - Si expira → Dialog de advertencia → Volver al mapa

2. **Lista de Inputs**:
   - Card por cada asiento
   - TextField para nombre completo
   - Validación en tiempo real
   - Icono de check verde cuando es válido

3. **Validaciones**:
   - No vacío
   - Mínimo 3 caracteres
   - Solo letras y espacios
   - Sin números ni caracteres especiales

4. **Botón Continuar**:
   - Habilitado solo si todos los nombres son válidos

**Lógica del Timer**:
```kotlin
// Duración: 5 minutos (300 segundos)
// Actualización: cada 1 segundo
// Al llegar a 0:
//   - Mostrar Dialog: "El bloqueo expiró"
//   - Navegar a SeatMapScreen
```

---

### 8. PurchaseSummaryScreen

**Propósito**: Revisar y confirmar compra final

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Resumen de Compra       2:15 │
├─────────────────────────────────┤
│                                 │
│  🎫 Concierto Rock              │
│  📅 15 Dic, 2025 - 20:00 hs     │
│  📍 Av. Corrientes 1234         │
│                                 │
│  ─────────────────────────      │
│  💺 Entradas (2)                │
│                                 │
│  ┌───────────────────────────┐ │
│  │ Fila E, Asiento 2         │ │
│  │ Juan Pérez                │ │
│  │                   $1250.00│ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ Fila E, Asiento 3         │ │
│  │ María García              │ │
│  │                   $1250.00│ │
│  └───────────────────────────┘ │
│                                 │
│  ─────────────────────────      │
│  Subtotal            $2,500.00  │
│  Cargo por servicio       $0.00 │
│  ─────────────────────────      │
│  💰 TOTAL            $2,500.00  │
│  ─────────────────────────      │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │   CONFIRMAR COMPRA      │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │        VOLVER           │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Secciones**:
1. Info del evento
2. Lista de entradas con nombres
3. Desglose de precios
4. Total destacado

**Acciones**:
- **Confirmar Compra**: Llama a realizar venta
- **Volver**: Permite cambiar nombres

**Estados**:
- **Loading**: Mostrar loading en botón
- **Success**: Navegar a PurchaseSuccessScreen
- **Error**: Dialog con mensaje de error

**API**:
- `POST /api/ventas/evento/{eventoId}/realizar`

---

### 9. PurchaseSuccessScreen

**Propósito**: Confirmación de compra exitosa

**Elementos UI**:
```
┌─────────────────────────────────┐
│                                 │
│                                 │
│          ✅                     │
│     ¡Compra Exitosa!            │
│                                 │
│  Tu ID de compra:               │
│  #VT-000123                     │
│                                 │
│  ┌───────────────────────────┐ │
│  │  [QR CODE]                │ │
│  │                           │ │
│  └───────────────────────────┘ │
│                                 │
│  Se ha enviado la confirmación  │
│  a tu correo electrónico.       │
│                                 │
│  📧 Revisa tu bandeja de entrada│
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │   VER MIS COMPRAS       │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │   VOLVER AL INICIO      │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Elementos**:
- Icono de éxito animado
- ID de compra destacado
- QR code generado (opcional)
- Mensajes informativos
- Botones de navegación

**Animaciones**:
- Check animado con scale + fade-in
- Confetti effect (opcional, sutil)

---

### 10. MyPurchasesScreen

**Propósito**: Historial de compras del usuario

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Mis Compras         [🔍]     │
├─────────────────────────────────┤
│                                 │
│  📊 Total de compras: 3         │
│  💰 Total gastado: $7,500       │
│                                 │
│  ─────────────────────────      │
│  🗓️ Próximos eventos            │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 🎸 Concierto Rock         │ │
│  │ 15 Dic, 20:00            │ │
│  │ 2 entradas  •  $2,500.00 │ │
│  │ [VER DETALLES]            │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 🎭 Teatro Musical         │ │
│  │ 20 Dic, 19:00            │ │
│  │ 1 entrada   •  $1,800.00 │ │
│  │ [VER DETALLES]            │ │
│  └───────────────────────────┘ │
│                                 │
│  ─────────────────────────      │
│  📜 Eventos pasados             │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 🎵 Festival Jazz          │ │
│  │ 01 Dic, 18:00            │ │
│  │ 4 entradas  •  $3,200.00 │ │
│  │ [VER DETALLES]            │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

**Agrupación**:
- Próximos eventos (fecha >= hoy)
- Eventos pasados (fecha < hoy)

**PurchaseCard**:
- Nombre del evento
- Fecha y hora
- Cantidad de entradas + precio total
- Estado visual (próximo vs pasado)
- Click → PurchaseDetailScreen

**API**:
- `GET /api/ventas`

---

### 11. PurchaseDetailScreen

**Propósito**: Detalles completos de una compra

**Elementos UI**:
```
┌─────────────────────────────────┐
│  ← Detalle de Compra     [⋮]    │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐ │
│  │  [QR CODE]                │ │
│  │                           │ │
│  │  ID: #VT-000123           │ │
│  └───────────────────────────┘ │
│                                 │
│  🎸 Concierto Rock              │
│  📅 15 Dic, 2025 - 20:00 hs     │
│  📍 Av. Corrientes 1234         │
│                                 │
│  ─────────────────────────      │
│  📋 Información de compra       │
│                                 │
│  Fecha de compra:               │
│  11 Dic, 2025 - 14:30 hs        │
│                                 │
│  💺 Entradas (2):               │
│  • Fila E, Asiento 2            │
│    Juan Pérez                   │
│  • Fila E, Asiento 3            │
│    María García                 │
│                                 │
│  💰 Total pagado: $2,500.00     │
│                                 │
│  ─────────────────────────      │
│                                 │
└─────────────────────────────────┘
│  ┌─────────────────────────┐   │
│  │    COMPARTIR QR         │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │    DESCARGAR ENTRADAS   │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

**Acciones**:
- Compartir QR (share sheet nativo)
- Descargar entradas como PDF (opcional)
- Ver ubicación en mapa (opcional)

**API**:
- `GET /api/ventas/{id}`

---

## 🧩 Componentes Reutilizables

### EventCard
```kotlin
@Composable
fun EventCard(
    event: EventoResumenDTO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            AsyncImage(
                model = event.imagen,
                contentDescription = event.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.titulo,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(event.fecha),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                CategoryChip(category = event.eventoTipo.nombre)
            }
        }
    }
}
```

### SeatView
```kotlin
@Composable
fun SeatView(
    seat: Seat,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val color = when {
        isSelected -> SeatSelected
        seat.estado == SeatState.AVAILABLE -> SeatAvailable
        seat.estado == SeatState.BLOCKED -> SeatBlocked
        seat.estado == SeatState.SOLD -> SeatSold
        else -> SeatAvailable
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
```

### CountdownTimer
```kotlin
@Composable
fun CountdownTimer(
    remainingSeconds: Int,
    onExpired: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val progress = remainingSeconds / 300f // Total 5 minutos
    
    val color = if (remainingSeconds < 60) Warning else Primary
    
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tiempo restante: $minutes:${seconds.toString().padStart(2, '0')}",
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = SurfaceVariant
        )
    }
}
```

### PrimaryButton
```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Background,
            disabledContainerColor = TextDisabled
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Background
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## 🏗️ Arquitectura Técnica

### Capas

```
┌─────────────────────────────────┐
│        Presentation Layer       │
│  (Composables + ViewModels)     │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│         Domain Layer            │
│  (Use Cases + Repositories)     │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│          Data Layer             │
│  (API Service + Data Sources)   │
└─────────────────────────────────┘
```

### Tecnologías Sugeridas

- **UI**: Jetpack Compose / Compose Multiplatform
- **Navegación**: Compose Navigation
- **Estado**: StateFlow + ViewModel
- **Networking**: Ktor Client
- **Serialización**: Kotlinx Serialization
- **Imágenes**: Coil / Kamel
- **Storage**: DataStore / EncryptedSharedPreferences
- **DI**: Koin

---

## 📂 Estructura de Proyecto

```
Mobile/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── EventoDTO.kt
│   │   │   │   │       ├── AsientoDTO.kt
│   │   │   │   │       └── VentaDTO.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── EventRepository.kt
│   │   │   │   │   ├── SeatRepository.kt
│   │   │   │   │   └── PurchaseRepository.kt
│   │   │   │   └── local/
│   │   │   │       └── TokenStorage.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Event.kt
│   │   │   │   │   ├── Seat.kt
│   │   │   │   │   └── Purchase.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── GetEventsUseCase.kt
│   │   │   │       ├── BlockSeatsUseCase.kt
│   │   │   │       └── PurchaseTicketsUseCase.kt
│   │   │   └── ui/
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Theme.kt
│   │   │       │   └── Type.kt
│   │   │       ├── components/
│   │   │       │   ├── EventCard.kt
│   │   │       │   ├── SeatView.kt
│   │   │       │   ├── CountdownTimer.kt
│   │   │       │   └── PrimaryButton.kt
│   │   │       └── screens/
│   │   │           ├── splash/
│   │   │           ├── login/
│   │   │           ├── home/
│   │   │           ├── eventdetail/
│   │   │           ├── seatmap/
│   │   │           ├── attendeenames/
│   │   │           ├── summary/
│   │   │           ├── success/
│   │   │           └── purchases/
│   │   └── resources/
│   │       ├── images/
│   │       └── strings/
│   ├── androidMain/
│   └── iosMain/
└── README.md
```

---

## 🎯 Funcionalidades Core

### ✅ Must Have (MVP)

1. **Autenticación**
   - Login con username/password
   - Almacenamiento seguro de token
   - Auto-login si token válido

2. **Exploración de Eventos**
   - Lista de eventos disponibles
   - Vista detalle con toda la información
   - Imágenes y descripción

3. **Compra de Entradas**
   - Selección visual de asientos (máx 4)
   - Bloqueo temporal (5 min con timer visible)
   - Carga de nombres de asistentes
   - Confirmación y finalización de compra

4. **Historial**
   - Lista de compras realizadas
   - Detalle de cada compra

### 🎨 Nice to Have (v2)

- Búsqueda y filtros de eventos
- Favoritos
- Notificaciones push
- Compartir eventos
- QR Code real para entradas
- Modo offline (cache de eventos)
- Animaciones más elaboradas
- Soporte multi-idioma
- Tema claro (opcional)

---

## 🔄 Lógica de Estados

### ViewModel Pattern

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### Ejemplo: HomeViewModel

```kotlin
class HomeViewModel(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {
    
    private val _eventsState = MutableStateFlow<UiState<List<Event>>>(UiState.Idle)
    val eventsState: StateFlow<UiState<List<Event>>> = _eventsState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = UiState.Loading
            getEventsUseCase()
                .onSuccess { events ->
                    _eventsState.value = UiState.Success(events)
                }
                .onFailure { error ->
                    _eventsState.value = UiState.Error(error.message ?: "Error desconocido")
                }
        }
    }
    
    fun refresh() {
        loadEvents()
    }
}
```

---

## 🎬 Animaciones

### Transiciones de Pantalla
- Slide horizontal para navegación forward/back
- Fade para modals y dialogs

### Micro-interacciones
- Ripple effect en todos los clickables
- Scale animation en botones (pressed state)
- Shimmer effect en loading states
- Smooth scroll en listas

### Animaciones Específicas

**SplashScreen**:
```kotlin
val scale = remember { Animatable(0f) }
LaunchedEffect(Unit) {
    scale.animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )
}
```

**PurchaseSuccess**:
```kotlin
val checkScale = remember { Animatable(0f) }
val checkRotation = remember { Animatable(0f) }
LaunchedEffect(Unit) {
    launch {
        checkScale.animateTo(1.2f, tween(300))
        checkScale.animateTo(1f, tween(100))
    }
    launch {
        checkRotation.animateTo(360f, tween(500))
    }
}
```

---

## 🔐 Seguridad

### Token JWT
- Almacenar en EncryptedSharedPreferences (Android)
- Almacenar en Keychain (iOS)
- No almacenar credenciales (username/password)
- Incluir en header de todas las requests

### Validaciones
- Validar todos los inputs en cliente
- No confiar solo en validaciones del backend
- Sanitizar inputs antes de enviar

---

## 📊 Métricas y Logging

### Events a Trackear
- App opened
- Login success/failure
- Event viewed
- Seats selected
- Purchase completed
- Purchase failed

### Logging
```kotlin
// No loggear información sensible
Logger.d("EventDetailScreen", "Loading event $eventId")
Logger.e("PurchaseViewModel", "Purchase failed", exception)
```

---

## 🧪 Testing

### Unit Tests
- ViewModels
- UseCases
- Repositories
- Utils

### UI Tests
- Flujo completo de compra
- Login
- Navegación

---

## 🚀 Roadmap de Desarrollo

### Sprint 1 (2 semanas)
- [ ] Setup proyecto KMP
- [ ] Configurar theme y colores
- [ ] Implementar SplashScreen
- [ ] Implementar LoginScreen
- [ ] Configurar networking (Ktor)
- [ ] Implementar token storage

### Sprint 2 (2 semanas)
- [ ] HomeScreen (lista de eventos)
- [ ] EventDetailScreen
- [ ] Componentes reutilizables base
- [ ] Navegación entre pantallas

### Sprint 3 (2 semanas)
- [ ] SeatMapScreen con lógica completa
- [ ] ConfirmSeatsScreen
- [ ] AttendeeNamesScreen con timer
- [ ] Integración de bloqueo de asientos

### Sprint 4 (2 semanas)
- [ ] PurchaseSummaryScreen
- [ ] PurchaseSuccessScreen
- [ ] MyPurchasesScreen
- [ ] PurchaseDetailScreen
- [ ] Integración de venta

### Sprint 5 (1 semana)
- [ ] Testing
- [ ] Bug fixing
- [ ] Optimizaciones
- [ ] Documentación

---

## 📝 Notas de Implementación

### Timer de Bloqueo

**Importante**: El timer debe ser **local** (no basado en servidor). Cuando se bloquean asientos:

1. Guardar timestamp de bloqueo: `val blockedAt = System.currentTimeMillis()`
2. Calcular expiración: `val expiresAt = blockedAt + (5 * 60 * 1000)`
3. Actualizar countdown cada segundo
4. Al expirar, forzar navegación al mapa

### Actualización del Mapa

Polling cada 30 segundos para refrescar estado de asientos:
```kotlin
LaunchedEffect(Unit) {
    while (isActive) {
        refreshSeatMap()
        delay(30_000) // 30 segundos
    }
}
```

### Manejo de Errores de Red

Mostrar Snackbar con opción "Reintentar":
```kotlin
Snackbar(
    action = {
        TextButton(onClick = { retry() }) {
            Text("Reintentar")
        }
    }
) {
    Text(errorMessage)
}
```

---

## 🎨 Assets Necesarios

### Iconos
- Logo de la app
- Iconos de categorías de eventos
- Iconos para estados de asientos
- Iconos de navegación

### Imágenes
- Placeholder para eventos sin imagen
- Background pattern para splash
- Empty states (sin eventos, sin compras)

### Animaciones (Lottie)
- Loading spinner
- Success checkmark
- Error icon
- Empty state

---

## 📱 Especificaciones Técnicas

- **Mínimo SDK Android**: 24 (Android 7.0)
- **Target SDK Android**: 34 (Android 14)
- **iOS Deployment Target**: 15.0
- **Kotlin Version**: 1.9+
- **Compose Multiplatform**: Latest stable

---

## 🤝 Convenciones de Código

### Naming
- ViewModels: `[Feature]ViewModel` (ej: `HomeViewModel`)
- Screens: `[Feature]Screen` (ej: `LoginScreen`)
- Components: Descriptive name (ej: `EventCard`, `SeatView`)

### Estructura de Archivos
- Un archivo por componente/pantalla
- DTOs en carpeta separada
- ViewModels junto a sus screens

### Comentarios
```kotlin
/**
 * Pantalla principal que muestra la lista de eventos disponibles.
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para manejar estado
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    // Implementation
}
```

---

## ✨ Detalles de Diseño

### Espaciado
- Padding interno de cards: 16dp
- Spacing entre elementos: 8dp / 16dp
- Margin entre cards: 12dp

### Tipografía
- Title Large: 28sp, Bold
- Title Medium: 22sp, Bold
- Body Large: 16sp, Regular
- Body Medium: 14sp, Regular
- Label: 12sp, Medium

### Elevación
- Cards: 4dp
- FAB: 8dp
- Dialogs: 24dp

### Border Radius
- Cards: 16dp
- Buttons: 16dp
- TextFields: 12dp
- Chips: 20dp

---

## 🎯 Criterios de Aceptación

### Login
- ✅ Campos de usuario y contraseña funcionales
- ✅ Validación de campos vacíos
- ✅ Mensaje de error en caso de credenciales incorrectas
- ✅ Token guardado de forma segura
- ✅ Navegación automática a Home tras login exitoso

### Selección de Asientos
- ✅ Mapa visual con colores correctos
- ✅ Máximo 4 asientos seleccionables
- ✅ No permitir seleccionar ocupados/bloqueados
- ✅ Actualización del mapa cada 30s
- ✅ Contador de selección visible

### Bloqueo
- ✅ Timer visible y actualizado en tiempo real
- ✅ Warning visual cuando < 1 minuto
- ✅ Dialog de expiración si llega a 0
- ✅ Navegación forzada al mapa si expira

### Compra
- ✅ Resumen completo antes de confirmar
- ✅ Validación de nombres (todos completos)
- ✅ Manejo de error si bloqueo expiró
- ✅ Pantalla de éxito con ID de compra
- ✅ Opción de ver historial

---

**Última actualización**: 2025-12-13  
**Versión**: 1.0.0  
**Estado**: ✅ Completo y listo para usar

---

## 🔧 Configuración para Desarrollo

### Permitir comunicación HTTP con servidor local

La aplicación está configurada para comunicarse con el backend en `http://10.0.2.2:8081` (localhost del emulador).

Android requiere configuración especial para permitir tráfico HTTP (cleartext):

**Archivo creado:** `src/main/res/xml/network_security_config.xml`
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

**AndroidManifest.xml actualizado:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="true"
    ...>
```

### Cambiar URL del Backend

Si necesitas cambiar la URL del backend, edita:  
`src/main/java/com/eventtickets/mobile/data/network/RetrofitClient.kt`

```kotlin
private const val BASE_URL = "http://10.0.2.2:8081"  // Cambiar aquí
```

**Direcciones importantes:**
- `10.0.2.2` = localhost del emulador de Android
- `localhost` / `127.0.0.1` = dispositivo físico con backend en tu PC

---

## 📚 Documentación

### Documentación Disponible
- ✅ **`README.md`** (este archivo) - Arquitectura completa del Mobile, UI/UX, pantallas y flujos
- ✅ **`BackEnd.md`** - Especificación completa de la API REST para el backend

---

## 🎯 Estado del Proyecto

### ✅ Mobile (Frontend) - COMPLETAMENTE IMPLEMENTADO
Todas las pantallas, navegación y funcionalidades están implementadas y funcionando:

**Autenticación:**
- ✅ Login con JWT (token persiste en SharedPreferences)
- ✅ Registro de cuenta
- ✅ Gestión de sesión

**Flujo de Compra:**
- ✅ Listado de eventos
- ✅ Detalle de evento
- ✅ Selección de asientos (max 4)
- ✅ Bloqueo temporal (5 minutos)
- ✅ Ingreso de nombres de asistentes
- ✅ Confirmación y resumen
- ✅ Historial de compras
- ✅ Visualización de QR codes

**Integración:**
- ✅ Conexión con backend via HTTP REST
- ✅ Manejo robusto de errores
- ✅ Null safety completo
- ✅ Network security config para HTTP cleartext

### ✅ Backend (API) - COMPLETAMENTE IMPLEMENTADO

Todos los endpoints necesarios están implementados y funcionando en el backend:

**Endpoints Disponibles:**
1. ✅ `POST /api/register` - Registro de usuarios
2. ✅ `POST /api/authenticate` - Login con JWT
3. ✅ `GET /api/eventos-consulta/resumidos` - Lista de eventos
4. ✅ `GET /api/eventos-consulta/{id}` - Detalle de evento
5. ✅ `GET /api/asientos/evento/{id}/mapa` - Mapa de asientos
6. ✅ `POST /api/asientos/evento/{id}/bloquear` - Bloquear asientos (5 min)
7. ✅ `POST /api/ventas/evento/{id}/realizar` - Realizar compra
8. ✅ `GET /api/ventas` - Historial de compras del usuario
9. ✅ `GET /api/ventas/{id}` - Detalle de compra con QR

**Mejoras Implementadas en Backend:**
- ✅ Rutas corregidas a `/api/eventos-consulta/*`
- ✅ VentaMapper retorna eventos completos (no solo ID)
- ✅ Queries optimizadas (N+1 problem resuelto con JOIN FETCH)
- ✅ Tests de integración actualizados

---

## 🚀 Sistema Listo para Usar

**El sistema Mobile + Backend está completamente funcional end-to-end:**
- ✅ Autenticación JWT funcionando
- ✅ Eventos sincronizados desde sistema externo
- ✅ Compra de entradas completa
- ✅ Historial con QR codes
- ✅ Performance optimizada

---

## 📖 Para Más Información

### Sobre el Mobile (este proyecto):
Consulta las secciones anteriores de este README para:
- Arquitectura MVVM + Repository Pattern
- Paleta de colores y diseño
- Descripción detallada de cada pantalla
- Guías de navegación y flujos

### Sobre la API del Backend:
Consulta **`BackEnd.md`** para:
- Especificación completa de todos los endpoints
- Request/Response de cada endpoint
- DTOs y modelos de datos
- Ejemplos de uso
- Validaciones y reglas de negocio

---

**Sistema desarrollado con:** Kotlin, Jetpack Compose, Spring Boot, JWT  
**Estado:** ✅ Producción Ready  
**Última actualización:** 2025-12-13

