# Sistema de Inventario y Punto de Venta con NFC

Proyecto Integrador Móvil — Universidad Tecnológica Emiliano Zapata (UTEZ)

## Integrantes

| Nombre | Matrícula |
|---|---|
| _Baeza Luis_ | _20253DS092_ |
| _Flores Irving_ | _20253DS070_ |
| _Sanchez Axel_ | _20253DS084_ |

## Descripción de la solución

Aplicación móvil Android para llevar el **control de inventario y las ventas de un negocio pequeño**, que sustituye el registro en papel por un sistema con datos centralizados, existencias en tiempo real y un registro permanente de cada venta.

Cada producto se identifica con una **etiqueta NFC**: al acercarla al teléfono, la app encuentra el producto al instante (sin teclear ni buscar en una libreta), lo muestra o lo agrega al carrito de venta. Al confirmar la venta, el servidor descuenta el stock y guarda la venta con fecha, vendedor, productos, cantidades y precios.

**Qué resuelve frente al registro en papel**

- Existencias actualizadas automáticamente en cada venta (sin conteos a mano ni errores de captura).
- Alerta visual de **stock bajo** y de productos agotados.
- Identificación inmediata del producto con NFC (cobro más rápido, menos errores).
- Cada venta queda guardada de forma permanente en la base de datos con fecha, vendedor y detalle (la API ya expone el historial).
- Control de acceso por **roles**: el administrador gestiona el catálogo; el vendedor consulta y cobra.

**Sensores y hardware que integra**

- **NFC:** lectura y grabación de etiquetas (mensaje NDEF de tipo texto con el código del producto).
- **Cámara:** foto del producto al crearlo o editarlo.

## Tecnologías

| Parte | Tecnologías |
|---|---|
| App móvil | Kotlin, Jetpack Compose (Material 3), MVVM, Navigation Compose, Room, DataStore, Retrofit + OkHttp, Coil, Corrutinas y Flow, NFC (`NfcAdapter`, NDEF) |
| Servidor API REST | Java 21, Spring Boot 4.1.2, Spring Security + JWT (jjwt), Spring Data JPA, MySQL, Lombok, Maven |

Requisitos de la app: Android 12 o superior (`minSdk 31`). El NFC requiere un **dispositivo físico con NFC** (el emulador no lo soporta; la app ofrece búsqueda manual como alternativa).

## Estructura del repositorio

```
.
├── app/        # Aplicación Android (Kotlin + Compose)
│   └── src/main/java/mx/edu/utez/sistemadeinventario/
│       ├── data/local/      # Room (productos y carrito) y DataStore (sesión)
│       ├── data/remote/     # Retrofit: ApiService, interceptor JWT, DTOs
│       ├── data/nfc/        # NfcManager, NfcParser, NfcWriter
│       ├── data/repository/ # Repositorios (fuente de datos de la app)
│       ├── ui/screens/      # Pantallas Compose
│       ├── ui/viewmodels/   # ViewModels y estados de UI
│       └── ui/navigation/   # Grafo de navegación
└── backend/    # API REST con Spring Boot
    └── src/main/java/mx/edu/utez/inventario/
        ├── controller/      # Endpoints REST
        ├── service/         # Reglas de negocio (ventas, productos)
        ├── repository/      # Acceso a datos (JPA)
        ├── model/           # Entidades (Producto, Usuario, Venta, DetalleVenta)
        ├── security/        # JWT y configuración de Spring Security
        └── dto/             # Objetos de petición/respuesta de ventas
```

## Instrucciones de ejecución

### 1. Servidor (Spring Boot)

Requisitos: **JDK 21** y **MySQL** corriendo en `localhost:3306`.

1. Revisa las credenciales de la base de datos en `backend/src/main/resources/application.properties` (por defecto: usuario `root`, contraseña `root`). La base de datos `inventario` se crea automáticamente y las tablas se generan solas (`ddl-auto=update`).
2. Desde la carpeta `backend`, ejecuta:

   ```bash
   ./mvnw spring-boot:run        # Linux / macOS
   mvnw.cmd spring-boot:run      # Windows
   ```

3. El servidor queda en `http://localhost:8080`. Las fotos subidas se guardan en la carpeta `uploads/` del backend.

**Usuarios de prueba** (se crean al iniciar por primera vez):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `vendedor` | `vendedor123` | VENDEDOR |

### 2. Aplicación móvil (Android)

1. Abre la carpeta raíz del repositorio en **Android Studio** y espera la sincronización de Gradle.
2. Revisa la dirección del servidor en `RetrofitClient.kt` (`BASE_URL`, actualmente `http://127.0.0.1:8080/`) y ajústala según cómo pruebes:
   - **Teléfono físico por USB:** deja `127.0.0.1` y ejecuta `adb reverse tcp:8080 tcp:8080`.
   - **Teléfono físico por Wi-Fi:** usa la IP de tu computadora (misma red), por ejemplo `http://192.168.1.50:8080/`.
   - **Emulador:** usa `http://10.0.2.2:8080/`.
3. Ejecuta la app en un dispositivo con NFC (para probar etiquetas). Activa el NFC del teléfono en Ajustes.
4. Inicia sesión con alguno de los usuarios de prueba.

### Uso básico

1. **Administrador:** en *Inventario*, toca el botón **+** para crear un producto (nombre, precio, stock, stock mínimo, foto). Toca **Grabar** en la sección de código NFC y acerca una etiqueta al teléfono; se graba un código tipo `INV-XXXXXXXX` en la etiqueta y queda asociado al producto.
2. **Escáner NFC:** acerca una etiqueta para abrir el detalle del producto (o usa la búsqueda manual por código).
3. **Punto de venta:** acerca etiquetas para agregar productos al carrito (o agrégalos manualmente), ajusta cantidades y toca **Confirmar venta**. El stock se descuenta en el servidor.

## Endpoints de la API REST

Todas las rutas (excepto el login y las imágenes) requieren el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | Público | Inicia sesión; devuelve `token`, `username` y `rol` |
| `GET` | `/api/productos?q=texto` | Autenticado | Lista productos; `q` filtra por nombre o SKU |
| `GET` | `/api/productos/{id}` | Autenticado | Detalle de un producto |
| `GET` | `/api/productos/nfc/{nfcId}` | Autenticado | Busca un producto por el código de su etiqueta NFC |
| `POST` | `/api/productos` | ADMIN | Crea un producto |
| `PUT` | `/api/productos/{id}` | ADMIN | Edita un producto |
| `POST` | `/api/productos/{id}/foto` | ADMIN | Sube la foto (`multipart/form-data`, campo `image`, máx. 5 MB) |
| `POST` | `/api/ventas` | Autenticado | Registra una venta y descuenta el stock |
| `GET` | `/api/ventas` | Autenticado | Historial de ventas (ADMIN ve todas; VENDEDOR solo las suyas), de la más reciente a la más antigua |
| `GET` | `/uploads/{archivo}` | Público | Imágenes de productos |

**Ejemplo de login**

```json
POST /api/auth/login
{ "username": "admin", "password": "admin123" }
```

**Ejemplo de venta**

```json
POST /api/ventas
{ "items": [ { "productoId": 1, "cantidad": 2 } ] }
```

**Códigos de error principales:** `401` sin sesión o token inválido, `403` sin permiso para la acción, `404` producto o etiqueta no encontrada, `409` stock insuficiente o SKU/etiqueta NFC duplicados.
