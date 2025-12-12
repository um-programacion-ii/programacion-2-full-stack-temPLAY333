# 🎫 EventTickets Mobile - Guía de Inicio

## ✅ Proyecto Creado

¡El proyecto base está listo! Todas las pantallas están implementadas y navegables.

## 📱 Estructura del Proyecto

```
Mobile/
├── src/main/java/com/eventtickets/mobile/
│   ├── MainActivity.kt                 # Activity principal
│   ├── data/
│   │   ├── model/Models.kt            # Modelos de datos
│   │   └── MockData.kt                # Datos de prueba
│   ├── navigation/
│   │   ├── Screen.kt                  # Definición de rutas
│   │   └── AppNavigation.kt           # Sistema de navegación
│   ├── ui/
│   │   ├── theme/                     # Colores y estilos
│   │   ├── components/                # Componentes reutilizables
│   │   └── screens/                   # Todas las pantallas
│   │       ├── splash/
│   │       ├── login/
│   │       ├── home/
│   │       ├── eventdetail/
│   │       ├── seatmap/
│   │       ├── confirmseats/
│   │       ├── attendeenames/
│   │       ├── summary/
│   │       ├── success/
│   │       ├── purchases/
│   │       └── purchasedetail/
│   └── res/                           # Recursos Android
```

## 🚀 Cómo Ejecutar

### Opción 1: Android Studio
1. Abre Android Studio
2. File → Open → Selecciona la carpeta `Mobile`
3. Espera que se sincronice Gradle
4. Click en el botón ▶️ Run
5. Selecciona un emulador o dispositivo

### Opción 2: Línea de Comandos
```bash
cd Mobile
./gradlew assembleDebug
./gradlew installDebug
```

## 🎨 Características Implementadas

### ✅ Sistema de Navegación Completo
- 11 pantallas totalmente funcionales
- Navegación hacia adelante y atrás
- Paso de parámetros entre pantallas

### ✅ Pantallas Implementadas

1. **SplashScreen** 
   - Animación de logo
   - Verificación de token simulada

2. **LoginScreen**
   - Login hardcodeado (admin/admin)
   - Validación de campos
   - Estados de loading y error

3. **HomeScreen**
   - Lista de 5 eventos de ejemplo
   - Navegación a detalle
   - Botón de "Mis Compras"

4. **EventDetailScreen**
   - Información completa del evento
   - Imágenes con Coil
   - Botón "Comprar Entradas"

5. **SeatMapScreen**
   - Grilla de asientos interactiva
   - Selección de hasta 4 asientos
   - Estados: Disponible, Seleccionado, Bloqueado, Ocupado
   - Leyenda de colores

6. **ConfirmSeatsScreen**
   - Resumen de asientos seleccionados
   - Precio total
   - Advertencia sobre bloqueo

7. **AttendeeNamesScreen**
   - Timer de 5 minutos (simulado)
   - Inputs para nombres
   - Validación en tiempo real

8. **PurchaseSummaryScreen**
   - Resumen completo de compra
   - Desglose de precios
   - Confirmación final

9. **PurchaseSuccessScreen**
   - Animación de éxito
   - ID de compra
   - QR Code placeholder

10. **MyPurchasesScreen**
    - Historial de compras
    - Estadísticas totales
    - Separación por eventos próximos/pasados

11. **PurchaseDetailScreen**
    - Detalle completo de una compra
    - QR Code
    - Opciones de compartir

### ✅ Componentes Reutilizables
- `PrimaryButton` - Botón principal con loading
- `SecondaryButton` - Botón secundario outlined
- `EventCard` - Tarjeta de evento
- `SeatView` - Vista individual de asiento

### ✅ Dark Mode
- Paleta de colores Verde/Azul oscuro
- Todos los componentes siguiendo el theme

### ✅ Datos de Prueba
- 5 eventos con imágenes (Picsum)
- 2 compras de ejemplo
- Mapa de asientos con estados variados

## 🎯 Flujo de Navegación

```
Splash → Login (admin/admin) → Home
                                  ↓
                          Click en evento
                                  ↓
                          EventDetail
                                  ↓
                     "Comprar Entradas"
                                  ↓
                          SeatMap (seleccionar asientos)
                                  ↓
                          ConfirmSeats
                                  ↓
                          AttendeeNames (timer)
                                  ↓
                          PurchaseSummary
                                  ↓
                          PurchaseSuccess
                                  ↓
                    MyPurchases ←→ PurchaseDetail
```

## 🔑 Credenciales de Login

```
Usuario: admin
Contraseña: admin
```

## 🎨 Paleta de Colores

- **Background**: #0A0E1A (Azul muy oscuro)
- **Surface**: #141B2E (Azul oscuro)
- **Primary**: #00E5A0 (Verde neón)
- **Secondary**: #4169E1 (Azul real)
- **SeatAvailable**: #00E5A0 (Verde)
- **SeatSelected**: #4169E1 (Azul)
- **SeatBlocked**: #FFB84D (Naranja)
- **SeatSold**: #E63946 (Rojo)

## 📝 Datos Mockeados

### Eventos
1. **Concierto Rock** - 10x15 asientos
2. **Festival de Jazz** - 8x12 asientos
3. **Teatro: Hamlet** - 12x10 asientos
4. **Stand Up Comedy** - 15x20 asientos
5. **Ballet: El Lago de los Cisnes** - 10x14 asientos

### Compras
- 2 compras de ejemplo guardadas
- Aparecen en "Mis Compras"

## 🔧 Próximos Pasos (Para integrar con Backend)

1. **Crear servicio de API**
   - Configurar Retrofit/Ktor
   - Endpoints según `BackEnd.md`

2. **Implementar autenticación real**
   - Guardar token JWT
   - Interceptor para agregar token

3. **Conectar pantallas con API**
   - Reemplazar `MockData` con llamadas reales
   - Manejar estados de loading/error

4. **Implementar lógica de timer real**
   - Timer de 5 minutos funcional
   - Bloqueo de asientos en backend

5. **Agregar QR Code real**
   - Librería ZXing
   - Generar QR desde ID de compra

## 🐛 Notas

- **Mock Data**: Todos los datos son hardcodeados por ahora
- **Timer**: El timer en AttendeeNames es simulado (no cuenta realmente)
- **QR Codes**: Son placeholders, no QR codes reales
- **Imágenes**: Usando Picsum.photos para imágenes de ejemplo
- **Login**: Solo acepta admin/admin

## 📦 Dependencias Incluidas

- Jetpack Compose
- Material3
- Navigation Compose
- Coil (imágenes)
- ViewModel Compose
- Kotlin Coroutines

## 🎉 ¡Listo para Probar!

La app está completamente funcional para navegación y visualización. Puedes:
- ✅ Hacer login
- ✅ Ver eventos
- ✅ Seleccionar asientos
- ✅ Completar el flujo de compra
- ✅ Ver historial de compras

Todos los flujos están implementados y las transiciones funcionan correctamente.

## 📞 Siguiente Fase

Una vez que el backend esté listo, podremos:
1. Integrar los endpoints reales
2. Implementar la lógica de bloqueo
3. Conectar con el sistema de pagos
4. Agregar notificaciones

---

**¡Disfruta explorando la app!** 🚀

