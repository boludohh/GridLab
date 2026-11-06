# Registro de archivos del proyecto — GridLab

Este documento es el índice de referencia de **todos los archivos del proyecto**.
La idea es que cuando necesites recordar dónde vive algo o cómo funciona
internamente un archivo, lo busques aquí primero en vez de rastrear el código.

- Los archivos del **módulo Kotlin** (`app/src/main/kotlin/...` y
  `app/src/test/kotlin/...`) tienen una descripción detallada: qué contienen,
  qué responsabilidad cumplen y cómo funcionan por dentro (clases, funciones
  clave, estado, reglas de negocio).
- El resto de archivos (recursos, assets, configuración de Gradle, CI, etc.)
  tiene una descripción breve, pensada para alguien que no programa y solo
  quiere entender cómo está organizado el proyecto.

> Convención de rutas: todas las rutas son relativas a la raíz del repo
> (`GridLab/`), salvo que se indique lo contrario.

---

## Índice

1. [Raíz del proyecto](#1-raíz-del-proyecto)
2. [Integración continua](#2-integración-continua)
3. [Configuración del módulo `app`](#3-configuración-del-módulo-app)
4. [AndroidManifest](#4-androidmanifest)
5. [Código Kotlin — lógica de la app](#5-código-kotlin--lógica-de-la-app)
   - 5.1 [Raíz de la app (`com.gridlab.app`)](#51-raíz-de-la-app-comgridlabapp)
   - 5.2 [`ui/`](#52-ui)
   - 5.3 [`ui/theme/`](#53-uitheme)
   - 5.4 [`core/model/`](#54-coremodel)
   - 5.5 [`core/geometry/`](#55-coregeometry)
   - 5.6 [`core/data/database/`](#56-coredatadatabase)
   - 5.7 [`core/data/di/`](#57-coredatadi)
   - 5.8 [`core/data/repository/`](#58-coredatarepository)
   - 5.9 [`core/data/storage/`](#59-coredatastorage)
   - 5.10 [`core/render/`](#510-corerender)
   - 5.11 [`feature/main/`](#511-featuremain)
   - 5.12 [`feature/editor/paper/`](#512-featureeditorpaper)
   - 5.13 [`feature/editor/grid/`](#513-featureeditorgrid)
   - 5.14 [`feature/settings/`](#514-featuresettings)
6. [Tests (`app/src/test`)](#6-tests-appsrctest)
7. [Recursos (`res/`)](#7-recursos-res)
8. [Assets (licencias de terceros)](#8-assets-licencias-de-terceros)
9. [Historial de versiones / pendientes](#9-historial-de-versiones--pendientes)

---

## 1. Raíz del proyecto

| Archivo | Qué es y para qué sirve |
|---|---|
| `build.gradle.kts` (raíz) | Archivo Gradle de nivel superior. Declara (sin aplicar) los plugins que usan los módulos del proyecto (Android, Kotlin, Compose, Serialization, KSP, Hilt, Detekt) y registra una tarea `clean` que borra la carpeta `build/` de todo el proyecto. |
| `settings.gradle.kts` | Le dice a Gradle qué módulos tiene el proyecto (`:app`) y de dónde descargar plugins y dependencias (`google()`, `mavenCentral()`, `gradlePluginPortal()`). Define el nombre del proyecto raíz (`GridLab`). |
| `gradle.properties` | Opciones globales de Gradle/Android/Kotlin: memoria de la JVM de build (`-Xmx10g`), uso de AndroidX, `nonTransitiveRClass`, KSP2, estilo de código Kotlin oficial. |
| `gradle/libs.versions.toml` | **Catálogo central de versiones** (version catalog). Aquí se fija la versión exacta de cada librería y plugin (AGP, Kotlin, KSP, Compose BOM, Hilt, Room, kotlinx.serialization, Coil, Konsist, Detekt, JUnit, markdown-renderer). Cambiar una versión del proyecto se hace aquí, no en los `build.gradle.kts` de los módulos. |
| `gradle/wrapper/gradle-wrapper.properties` | Indica qué versión de Gradle debe descargar y usar el wrapper (`gradlew`). |
| `gradle/wrapper/gradle-wrapper.jar` | Binario del Gradle Wrapper; permite compilar el proyecto sin tener Gradle instalado previamente. |
| `gradlew` / `gradlew.bat` | Scripts (Linux/macOS y Windows respectivamente) que descargan y ejecutan la versión correcta de Gradle automáticamente. |
| `.gitignore` | Lista de archivos/carpetas que Git debe ignorar: `build/`, el keystore real (`*.keystore`), `keystore-base64.txt` y `app/keystore.properties` (para no subir nunca secretos de firma al repositorio). |
| `LICENSE` | Texto de la licencia bajo la que se publica el propio código de GridLab. |
| `README.md` | Presentación pública del proyecto: qué es GridLab (app para reproducir imágenes de referencia usando la técnica de la cuadrícula), objetivos y estado del desarrollo. |
| `PROJECT_CONTEXT.md` | Bitácora de contexto para retomar el desarrollo: qué incluye la versión actual, qué se dejó pendiente. Se actualiza en cada entrega funcional. |
| `config/detekt/detekt.yml` | Configuración de **Detekt** (linter estático de Kotlin): reglas activas y sus umbrales (por ejemplo, `LongMethod` a 60 líneas, `TooManyFunctions` a 20 por archivo/clase). Se aplica solo al módulo `app` vía `app/build.gradle.kts`. |

---

## 2. Integración continua

| Archivo | Qué es y para qué sirve |
|---|---|
| `.github/workflows/ci.yml` | Workflow de **GitHub Actions**. Se dispara en cada `push`/`pull_request` a `main`/`master`. Tiene dos jobs: <br>• **`compile`** (obligatorio, bloquea el workflow si falla): configura JDK 21, decodifica el keystore desde el secreto `KEYSTORE_BASE64` a `app/keystore/gridlab-release.jks`, genera `app/keystore.properties` con los secretos `KEYSTORE_STORE_PASSWORD`, `KEYSTORE_KEY_ALIAS` y `KEYSTORE_KEY_PASSWORD`, compila `assembleDebug` y `assembleRelease`, y sube ambos APKs como artefactos descargables. <br>• **`quality`** (no bloquea el workflow gracias a `continue-on-error: true`): corre los tests unitarios (incluye las reglas de arquitectura de Konsist) y Detekt. <br>**Nota de mantenimiento:** este workflow genera `app/keystore.properties`, pero para que el release quede firmado con la key real, `app/build.gradle.kts` debe leer ese archivo y aplicarlo como `signingConfig` del `buildType release` (ver sección 3 para el estado actual de esto). |

---

## 3. Configuración del módulo `app`

| Archivo | Qué es y para qué sirve |
|---|---|
| `app/build.gradle.kts` | Build file del módulo Android. Define `namespace`/`applicationId` (`com.gridlab.app`), `compileSdk` 37, `minSdk` 27, `targetSdk` 37, `versionCode`/`versionName`, Java/Kotlin 21 como target. Lee `app/keystore.properties` (si existe) y registra un `signingConfigs.release` que se asigna al `buildTypes.release`, de modo que un `assembleRelease` con ese archivo presente produce un APK firmado; sin el archivo, el release queda sin firmar (compila igual, útil para builds locales sin credenciales). Configura Detekt (`buildUponDefaultConfig`, config en `config/detekt/detekt.yml`) y declara todas las dependencias del módulo (Compose, Hilt, Room, kotlinx.serialization, Coil, markdown-renderer, JUnit, Konsist). |
| `app/proguard-rules.pro` | Reglas de ProGuard/R8 para la ofuscación y reducción de código en builds optimizados. Actualmente solo contiene comentarios de ejemplo (sin reglas activas propias del proyecto). |
| `app/keystore/.gitkeep` | Archivo vacío cuyo único propósito es que Git no elimine la carpeta `app/keystore/` (que normalmente está vacía en el repo, porque el `.jks` real nunca se versiona). El CI coloca ahí el keystore descifrado en tiempo de build. |

---

## 4. AndroidManifest

| Archivo | Qué es y para qué sirve |
|---|---|
| `app/src/main/AndroidManifest.xml` | Manifest de la app. Declara la clase `Application` (`.GridLabApp`), el ícono/ícono redondo, el tema global (`Theme.GridLab`), `allowBackup` y `supportsRtl`. Declara una única `Activity` exportada (`.MainActivity`) como punto de entrada (`LAUNCHER`/`MAIN`). No hay más actividades, servicios ni providers: toda la navegación interna ocurre dentro de `MainActivity` vía Compose. |

---

## 5. Código Kotlin — lógica de la app

Esta es la parte central del documento: el código fuente real de GridLab,
organizado por paquete. Cada entrada explica qué hace el archivo y cómo
funciona por dentro.

### 5.1 Raíz de la app (`com.gridlab.app`)

| Archivo | Funcionamiento interno |
|---|---|
| `GridLabApp.kt` | Clase `Application` de la app, anotada con `@HiltAndroidApp`. Es el punto donde Hilt genera el grafo de inyección de dependencias raíz; no contiene lógica propia. |
| `MainActivity.kt` | Única `Activity` de la app (`@AndroidEntryPoint` para que Hilt pueda inyectar ViewModels en las pantallas Compose que cuelgan de ella). En `onCreate` llama a `configureEdgeToEdge()` y monta el árbol Compose dentro de `GridLabTheme`. Define `RootDestination`, una `sealed interface` privada con cuatro estados de navegación manual (sin Navigation Compose): `Home`, `Settings`, `OpenSourceLicenses` y `Editor(documentId)`. Un `remember { mutableStateOf<RootDestination>(Home) }` guarda la pantalla activa y un `when` dentro de un `Scaffold` decide qué composable mostrar: `MainScreen` → `SettingsScreen` → `OpenSourceLicensesScreen`, y `MainScreen`/`Home` puede saltar directo a `GridConfigScreen` (editor) pasando el `documentId` del proyecto abierto. |
| `DemoViewModel.kt` | Archivo de verificación técnica, no de producto. Contiene `MessageProvider` (clase inyectable trivial) y `DemoViewModel` (`@HiltViewModel`), cuyo único propósito es confirmar en tiempo de desarrollo que la cadena Hilt → KSP → `hiltViewModel()` funciona de punta a punta en el dispositivo. No se usa en ninguna pantalla real. |

### 5.2 `ui/`

| Archivo | Funcionamiento interno |
|---|---|
| `ui/EdgeToEdge.kt` | Función de extensión `ComponentActivity.configureEdgeToEdge()` que centraliza toda la configuración edge-to-edge de la app (API 27–37+). Hace, en orden: (1) `enableEdgeToEdge()` + `setDecorFitsSystemWindows(false)`; (2) limpia los flags legacy `FLAG_TRANSLUCENT_NAVIGATION`/`FLAG_TRANSLUCENT_STATUS` que en API 27–28 dejaban un degradado visible; (3) fuerza `statusBarColor`/`navigationBarColor` transparentes; (4) en API 29+ (`Build.VERSION_CODES.Q`), desactiva los scrims de contraste automáticos y el "force dark"; (5) en API 28+ (`P`), pone el contenido dentro del recorte de pantalla (`LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`). Debe usarse junto con los colores transparentes definidos en `res/values/themes.xml` y `res/values-v29/themes.xml`. El color de los íconos de la barra de sistema (claro/oscuro) se aplica aparte, desde el tema de Compose. |

### 5.3 `ui/theme/`

| Archivo | Funcionamiento interno |
|---|---|
| `ui/theme/Color.kt` | Declara **todos los tokens de color en crudo** (valores `Color(0xFF...)`) para los tres temas de la app: Claro (`Light*`), Oscuro (`Dark*`) y Full Negro (`Night*`). Por cada tema define una escala de superficies de atrás hacia adelante (`Bg → Surface → E1 → E2 → E3 → E4`), bordes decorativos, bordes deshabilitados, borde activo, iconografía (deco/inactivo/activo), texto (primario/secundario/terciario/placeholder/deshabilitado/link/inverso) y colores de acción. Este archivo es solo el "diccionario" de colores; quién decide qué token usar según el tema activo es `Theme.kt`. |
| `ui/theme/Theme.kt` | Capa semántica sobre `Color.kt`. Define `GridLabThemeMode` (`Light`/`Dark`/`Night`) y la data class `GridLabSemanticColors` (inmutable, `@Immutable`) con un campo por cada rol semántico (fondo, superficies, bordes, iconos, texto, acciones, etc.). Expone un `CompositionLocal` (`staticCompositionLocalOf`) para que cualquier composable pueda leer `GridLabTheme.colors` sin pasarlos por parámetro. El composable raíz `GridLabTheme { content }` decide el modo de tema (sigue `isSystemInDarkTheme()` salvo que se fuerce uno), arma el `GridLabSemanticColors` correspondiente, aplica el `ColorScheme` de Material3 derivado y ajusta la apariencia de los íconos de la barra de sistema (claro/oscuro) vía `LocalView`/`WindowCompat` con un `SideEffect`. |
| `ui/theme/Type.kt` | Define la familia tipográfica `IbmPlexSans` (`FontFamily`) cargando los 10 archivos `.otf` de `res/font/` (regular, medium, semibold, bold, variante "text" y sus itálicas) y construye el objeto `Typography` de Material3 (`displayLarge` … hasta los estilos más pequeños) usando esa familia, con tamaños y alturas de línea explícitos para cada rol tipográfico. |

### 5.4 `core/model/`

Modelos de dominio puros (sin dependencias de Android), la mayoría `@Serializable` con kotlinx.serialization porque se persisten como JSON dentro de Room (ver `core/data/database`).

| Archivo | Funcionamiento interno |
|---|---|
| `core/model/Document.kt` | Entidad raíz de un proyecto: `id`, `name`, `paper: Paper`, `grid: Grid` (por defecto `Grid.default()`), `referenceImage: ReferenceImage?` (nullable) y `schemaVersion` (constante `CURRENT_SCHEMA_VERSION = 7`, guardada junto con el documento para futuras migraciones de formato). `Document.newDefault(id, name)` crea un documento nuevo con papel A4 y grilla por defecto. |
| `core/model/Paper.kt` | Define el papel físico: `format` (`StandardPaperFormat`), `widthMm`/`heightMm`, `orientation`, márgenes (`marginLeft/Top/Right/BottomMm`, todos en mm) y `displayUnit` (solo afecta cómo se muestra/edita en la UI; el almacenamiento interno siempre es mm). Expone `printableWidthMm`/`printableHeightMm` calculadas restando márgenes (con `coerceAtLeast(0.0)`). `Paper.fromStandard(format, orientation, ...)` arma un papel a partir de un formato ISO estándar, intercambiando ancho/alto si la orientación es `Landscape`; rechaza `StandardPaperFormat.Custom` (para eso se usa el constructor primario directamente). `Paper.defaultA4()` es el atajo usado al crear proyectos nuevos. |
| `core/model/StandardPaperFormat.kt` | Enum con los tamaños ISO 216 (`A0` a `A5`) con su ancho/alto en mm en orientación portrait, más `Custom(null, null)` para tamaños definidos a mano por el usuario. |
| `core/model/PaperOrientation.kt` | Enum simple: `Portrait` / `Landscape`. |
| `core/model/LengthUnit.kt` | Enum de unidades de longitud para mostrar/editar valores: `Millimeter`, `Centimeter`, `Inch`. El almacenamiento interno siempre es milímetros; la conversión la hace `UnitConverter` (sección 5.5). |
| `core/model/Grid.kt` | Configuración de la cuadrícula principal y secundaria de un documento: `rows`/`columns` (por defecto 10×10), `visible`, `lineStyle: GridLineStyle`, y un segundo nivel de subdivisión (`secondaryDivisions`, `secondaryVisible`, `secondaryLineStyle`, con un gris semitransparente por defecto) para dibujar líneas auxiliares dentro de cada celda. `labelsVisible` activa los números de columna (arriba) y letras de fila (izquierda) sobre el área imprimible. Valida en el `init` que `rows`, `columns` y `secondaryDivisions` sean ≥ 1. |
| `core/model/GridLineStyle.kt` | Estilo visual de una línea de grilla: `colorArgb` (color empaquetado ARGB, ej. `0xFF000000` = negro opaco), `strokeWidthMm` (grosor físico en mm) y `opacity` (0.0–1.0, multiplica el alfa del color al dibujar). |
| `core/model/ReferenceImage.kt` | Imagen de referencia vinculada a un documento. `relativePath` es relativo a `filesDir` (ej. `projects/<id>/original.jpg`). Guarda transformación del usuario relativa al encaje ("fit") inicial dentro del área imprimible: `offsetXMm`/`offsetYMm`, `scale`, `rotationDegrees`, `opacity`, `locked` (si está bloqueada, las funciones de `ImageTransformCalculator` no la modifican), `visible`, un recorte no destructivo normalizado (`cropLeft/Top/Right/BottomMm`, en rango 0..1) y `filter: ImageFilter` (efecto de visualización/exportación, nunca modifica el archivo original). El `init` valida rangos de `scale`, `opacity` y que el rectángulo de recorte sea válido (`left < right`, `top < bottom`). `hasCrop` indica si el recorte difiere del completo. `ReferenceImage.withPath(path)` crea una instancia con transform por defecto. |
| `core/model/ImageFilter.kt` | Enum de filtros no destructivos aplicables a la imagen de referencia: `None`, `Grayscale`, `HighContrast`. La matriz de color real de cada uno vive en `core/geometry/ImageFilterMatrices.kt`. |
| `core/model/ImageAvailability.kt` | Enum que describe el estado en disco de la imagen de referencia de un documento, verificado al abrir el proyecto: `None` (el modelo no referencia ninguna imagen), `Present` (el archivo existe y parece usable), `Missing` (el modelo apunta a una ruta pero el archivo no está) y `Corrupt` (el archivo existe pero está vacío o no se puede leer). Es independiente de los campos de transformación/recorte. |
| `core/model/ExportFormat.kt` | Enum de formatos de exportación soportados: `Jpeg`, `Png`, `Pdf`. |

### 5.5 `core/geometry/`

Matemática pura en Kotlin (sin dependencias de Android), usada tanto por los ViewModels como por `DocumentRenderer`. Al ser pura, se testea directamente en `app/src/test` sin necesidad de instrumentación.

| Archivo | Funcionamiento interno |
|---|---|
| `core/geometry/UnitConverter.kt` | Objeto con `toMm(value, unit)` y `fromMm(valueMm, unit)` para convertir entre `LengthUnit` (mm/cm/in) y milímetros, que es la unidad interna de almacenamiento de todo el proyecto. |
| `core/geometry/GridCalculator.kt` | Calcula la geometría resuelta de la grilla en milímetros, relativa al origen del área imprimible (esquina superior izquierda dentro de los márgenes). Produce un `GridLayout` con: tamaño imprimible, tamaño de celda, posiciones X de las líneas verticales primarias y posiciones Y de las horizontales (incluyendo bordes), y, si `secondaryVisible` y `secondaryDivisions > 1`, las posiciones de las líneas secundarias (excluyendo las que coinciden con líneas primarias). `GridCalculator.calculate(paper, grid)` es la única función pública. |
| `core/geometry/GridLabelFormatter.kt` | Da formato a las etiquetas de celda: `columnLabel(indexZeroBased)` devuelve el número de columna en base 1 como texto ("1", "2", …); `rowLabel(indexZeroBased)` devuelve letras estilo hoja de cálculo (A, B, … Z, AA, AB, …) mediante aritmética en base 26. |
| `core/geometry/ExportSizeCalculator.kt` | Calcula tamaños de exportación en píxeles y límites de memoria seguros. `widthPx`/`heightPx` convierten mm→px usando `MM_PER_INCH = 25.4`. `estimatedBytes(widthMm, heightMm, dpi)` estima el peso en memoria de un bitmap `RGB_565` (2 bytes/píxel). `maxSafeDpi(widthMm, heightMm, maxMemoryBytes)` despeja algebraicamente el DPI entero máximo tal que el bitmap quepa en `maxMemoryBytes * MEMORY_SAFETY_FACTOR` (40% del heap máximo de la app). `isDpiSafe(...)` valida un DPI concreto contra ese mismo presupuesto. Esto es lo que evita `OutOfMemoryError` al exportar documentos grandes a DPI alto. |
| `core/geometry/ImageFilterMatrices.kt` | Traduce cada valor de `ImageFilter` a una matriz de color 4×5 (formato `android.graphics.ColorMatrix`/Compose) que `DocumentRenderer` aplica al dibujar la imagen de referencia: `None` → `null` (sin filtro), `Grayscale` → matriz de luminancia estándar (coeficientes 0.299/0.587/0.114 en los tres canales), `HighContrast` → matriz que amplifica el contraste (factor 1.5 con offset −0.25). |
| `core/geometry/ImageTransformCalculator.kt` | Funciones puras que producen una nueva `ReferenceImage` a partir de una transformación, respetando siempre el flag `locked` (si la imagen está bloqueada, la mayoría de estas funciones devuelven la imagen sin cambios): `pan(image, deltaXMm, deltaYMm)` desplaza el offset; `scaleBy(image, factor)` multiplica la escala y la limita a `[0.05, 20.0]`; `withOpacity`, `withLocked`, `withVisible` setean esos campos directamente; `withFilter` cambia el filtro (respeta `locked`); `withRotation(image, degrees)` normaliza el ángulo a `[0, 360)`; `withCrop(image, left, top, right, bottom)` ajusta el rectángulo de recorte normalizado, garantizando con `coerceIn` y reajustes automáticos que el ancho/alto mínimos del recorte sean `MIN_CROP_SPAN = 0.05`; `clearCrop` restaura el recorte completo; `resetTransform(image)` vuelve offset a 0, escala a 1 y rotación a 0, conservando ruta, opacidad, bloqueo, visibilidad y recorte. |
| `core/geometry/ViewportTransform.kt` | Modela la relación entre píxeles de pantalla y milímetros de documento para la vista previa del papel: `originXPx`/`originYPx` (esquina superior izquierda del papel en pantalla) y `scale` (píxeles de pantalla por milímetro). `screenToDocumentMm(xPx, yPx)` y `documentToScreenPx(xMm, yMm)` convierten en ambos sentidos. El companion `distanceMm(a, b)` calcula distancia euclídea entre dos puntos del documento (usado por la herramienta de medición). Exige `scale > 0` en el `init`. |

### 5.6 `core/data/database/`

Capa de persistencia local con **Room**.

| Archivo | Funcionamiento interno |
|---|---|
| `core/data/database/AppDatabase.kt` | `RoomDatabase` abstracta de la app (`@Database(entities = [DocumentEntity::class], version = 3)`). Expone `projectDao(): ProjectDao`. El esquema no se exporta (`exportSchema = false`). |
| `core/data/database/DocumentEntity.kt` | Entidad Room, tabla `documents`. Guarda `id` (clave primaria), `name`, y el resto del modelo serializado como texto JSON: `paperJson` (siempre presente), `gridJson` (JSON de `Grid`, por defecto `"{}"`), `referenceImageJson` (JSON de `ReferenceImage` o cadena vacía si no hay imagen), más `schemaVersion` y `updatedAtEpochMs`. Guardar sub-modelos como JSON en vez de columnas propias evita migraciones de esquema cada vez que cambian los campos internos de `Paper`/`Grid`/`ReferenceImage`. |
| `core/data/database/ProjectDao.kt` | DAO de Room con las operaciones CRUD sobre `documents`: `getById(id)`, `getLatest()` (el proyecto más reciente por `updatedAtEpochMs`, usado para abrir el último proyecto editado), `getAll()` (todos, orden descendente por fecha), `upsert(entity)` (`INSERT` con `OnConflictStrategy.REPLACE`) y `deleteById(id)`. |

### 5.7 `core/data/di/`

Módulos de inyección de dependencias con **Hilt**, instalados en `SingletonComponent` (viven mientras vive la app).

| Archivo | Funcionamiento interno |
|---|---|
| `core/data/di/DatabaseModule.kt` | Construye el singleton de `AppDatabase` con `Room.databaseBuilder(context, AppDatabase::class.java, "gridlab.db")`. Define las migraciones manuales de esquema: `MIGRATION_1_2` agrega la columna `gridJson` (valor por defecto `'{}'`), `MIGRATION_2_3` agrega `referenceImageJson` (valor por defecto `''`). Ambas se registran con `.addMigrations(...)`. También provee `ProjectDao` a partir de la base de datos. |
| `core/data/di/RepositoryModule.kt` | Módulo abstracto que liga la interfaz `ProjectRepository` a su implementación `ProjectRepositoryImpl` mediante `@Binds`, como singleton. Es la única pieza que "conecta" la interfaz pública con la implementación concreta. |

### 5.8 `core/data/repository/`

| Archivo | Funcionamiento interno |
|---|---|
| `core/data/repository/ProjectRepository.kt` | Interfaz pública del repositorio de proyectos: `getDocument(id)`, `getLatestDocument()`, `listDocuments()`, `saveDocument(document)`, `deleteDocument(id)` (documentado explícitamente: primero borra la fila de Room y después los archivos del proyecto en `filesDir`). Es el contrato que usan los ViewModels; nunca conocen `ProjectRepositoryImpl` directamente. |
| `core/data/repository/ProjectRepositoryImpl.kt` | Implementación real. Usa un `Json` de kotlinx.serialization configurado con `ignoreUnknownKeys = true` y `encodeDefaults = true` (tolerante a campos nuevos/faltantes entre versiones del esquema). `toDomain()` (extensión privada de `DocumentEntity`) decodifica `paperJson`/`gridJson`/`referenceImageJson` a los modelos de dominio, con manejo especial de compatibilidad hacia atrás: si `gridJson` está vacío o es `"{}"`, usa `Grid.default()` en vez de intentar decodificar; si `referenceImageJson` está vacío, la imagen de referencia queda `null`. `toEntity()` (extensión privada de `Document`) hace el camino inverso y fija `updatedAtEpochMs = System.currentTimeMillis()` en cada guardado. `deleteDocument(id)` borra primero de `ProjectDao` y luego llama a `ImageStorage.deleteProjectDir(id)` para limpiar los archivos de imagen asociados. |

### 5.9 `core/data/storage/`

| Archivo | Funcionamiento interno |
|---|---|
| `core/data/storage/ImageStorage.kt` | Gestiona la copia y el ciclo de vida de las imágenes de referencia en almacenamiento privado de la app (`filesDir/projects/<documentId>/`). `copyFromUri(documentId, uri)` copia el contenido de un `Uri` (por ejemplo, elegido desde la galería) a `projects/<id>/original_<ext>` usando `ContentResolver`, adivinando la extensión por el MIME type (`guessExtension`: png/webp/jpeg, con jpg como fallback) y devuelve la ruta relativa a `filesDir`. `resolveAbsoluteFile(relativePath)` arma el `File` absoluto. `exists(relativePath)` y `probe(relativePath)` verifican disponibilidad: `probe` devuelve un `ImageAvailability` (`None` si no hay ruta, `Missing` si el archivo no existe, `Corrupt` si el archivo existe pero pesa 0 o no se puede leer su cabecera, `Present` en caso contrario). `deleteRelativeFile(path)` borra un único archivo si existe (usado al reemplazar la imagen de un proyecto). `deleteProjectDir(documentId)` borra recursivamente `projects/<id>/` completo (usado al borrar un proyecto). Todas las operaciones de E/S corren en `Dispatchers.IO`. |

### 5.10 `core/render/`

| Archivo | Funcionamiento interno |
|---|---|
| `core/render/DocumentRenderer.kt` | El motor de dibujo/exportación de la app; es el único archivo que combina papel + grilla + imagen en un bitmap final. `renderToBitmap(document, dpi, imageFile)` crea un `Bitmap` en `Config.RGB_565` (para ahorrar memoria) del tamaño exacto en píxeles para ese DPI (vía `ExportSizeCalculator`), lo pinta de blanco, y dibuja en orden: (1) la imagen de referencia si existe/es visible (`drawReferenceImage`), (2) la grilla si `grid.visible` (`drawGrid`), (3) las etiquetas de celda si `grid.labelsVisible` (`drawGridLabels`). <br>`drawReferenceImage` decodifica primero solo las dimensiones del archivo (`inJustDecodeBounds = true`), calcula el rectángulo de recorte en píxeles de origen a partir del `cropLeft/Top/Right/BottomMm` normalizado, decodifica la imagen real con `inSampleSize` calculado por `calculateInSampleSize` (reduce resolución si la fuente es mucho más grande que el destino, para no gastar memoria de más), ajusta el bitmap recortado al área imprimible manteniendo proporción ("fit"), aplica escala/offset/rotación del usuario alrededor del centro, aplica opacidad y el filtro de color correspondiente (`ImageFilterMatrices` + `ColorMatrixColorFilter`), y dibuja con `canvas.drawBitmap` dentro de un `save()/restoreToCount()` para poder rotar sin afectar el resto del canvas. <br>`drawGrid` usa `GridCalculator.calculate(paper, grid)` para obtener las posiciones de línea y las dibuja con el `Paint` configurado según `GridLineStyle` (color, opacidad, grosor convertido de mm a píxeles); si hay líneas secundarias, las dibuja con un segundo `Paint` más fino. `drawGridLabels` dibuja los números de columna arriba y las letras de fila a la izquierda (usando `GridLabelFormatter`), con lógica para no salirse del bitmap cuando el margen es muy chico (cambia alineación del texto). <br>`exportJpeg`/`exportPng`/`exportPdf` llaman a `renderToBitmap` y después comprimen a disco: JPEG con calidad configurable (`coerceIn(50, 100)`, por defecto 90), PNG sin pérdida, y PDF generando un `PdfDocument` de una sola página del mismo tamaño que el bitmap renderizado. En los tres casos se libera el bitmap (`recycle()`) en un bloque `finally`. |

### 5.11 `feature/main/`

Pantalla de inicio (listado de proyectos).

| Archivo | Funcionamiento interno |
|---|---|
| `feature/main/MainViewModel.kt` | `@HiltViewModel` que gestiona el estado de la pantalla Home. `MainUiState` guarda la lista de proyectos (`ProjectRowUi`, que empareja un `Document` con la ruta absoluta de su miniatura si el archivo de imagen existe), `isLoading`, `isWorking` (operación en curso), `errorMessage`, y el estado de selección múltiple (`isSelectionMode`, `selectedIds`). `refresh()` recarga la lista desde `ProjectRepository.listDocuments()` y resuelve la ruta absoluta de cada miniatura vía `ImageStorage`. `createProject(onCreated)` genera un `UUID`, crea un `Document.newDefault` con nombre `"Project " + id.take(8)`, lo guarda y notifica el nuevo id por callback. `renameProject(id, newName)` recarga el documento, aplica el nuevo nombre (recortado, rechaza vacío) y guarda. La selección múltiple sigue un patrón de dos toques: `onDeleteHeaderClick()` entra en modo selección en el primer toque (devuelve `false`) y en el segundo, si hay elementos seleccionados, devuelve `true` para que la UI muestre el diálogo de confirmación de borrado. `toggleProjectSelection`, `selectAllProjects`, `toggleSelectAllProjects` (selecciona todo o limpia si ya estaba todo seleccionado) y `exitSelectionMode` gestionan ese estado. `confirmDeleteSelected()` borra todos los ids seleccionados vía el repositorio y refresca la lista. |
| `feature/main/MainScreen.kt` | Composable de la pantalla Home. `MainScreen(onOpenProject, onOpenSettings, modifier)` obtiene el `MainViewModel` con `hiltViewModel()` y observa `uiState` con `collectAsStateWithLifecycle()`. Incluye: `HomeTopBar` (barra superior con ícono de ajustes), `HomeHeroSection` (banner con eslogan rotativo: un `LaunchedEffect` cicla cada `TAGLINE_INTERVAL_MS = 2500ms` por el `string-array` `home_taglines` usando `AnimatedContent` con fundido), `HomeNewProjectButton` (botón ancho para crear proyecto), `ProjectsSectionHeader` (encabezado de la lista con acciones de selección/borrado), `ProjectRow` (fila de un proyecto con miniatura vía Coil `AsyncImage`, nombre, y menú de renombrar/eliminar o checkbox en modo selección), `ProjectThumbnail`, `RenameDialog` y `DeleteProjectsDialog` (confirmación, con mensaje distinto para borrado simple vs. múltiple). Toda la lista se renderiza con `LazyColumn`. |

### 5.12 `feature/editor/paper/`

> **Nota de estado:** a día de hoy `PaperConfigScreen.kt` y `PaperConfigViewModel.kt` no están referenciados desde ningún otro punto de la app (`MainActivity` navega directo a `GridConfigScreen`, que ya incorpora también la configuración de papel/grilla/imagen en una sola pantalla). Se documentan igual porque siguen compilando y formando parte del módulo.

| Archivo | Funcionamiento interno |
|---|---|
| `feature/editor/paper/PaperConfigViewModel.kt` | `@HiltViewModel` enfocado solo en la configuración de papel. `PaperConfigUiState` guarda `documentId`, `documentName`, `paper`, `grid`, `referenceImage`, `isLoading`, `isSaving`, `lastSaveSucceeded`. `loadOrCreateDocument()` (llamado en `init`) carga el último documento (`getLatestDocument()`) o, si no hay ninguno, crea uno nuevo con `Document.newDefault(id = UUID random, name = "Untitled")` y lo persiste. `saveDocument()` guarda el estado actual completo. `setFormat(format)` cambia el formato estándar (o pasa a `Custom` sin tocar dimensiones si se elige `Custom`), recalculando ancho/alto vía `Paper.fromStandard` para formatos no custom. `setOrientation(orientation)` recalcula igual, o intercambia ancho/alto manualmente si el formato es `Custom`. `setDisplayUnit`, `setCustomWidth`/`setCustomHeight` (convierten el valor ingresado a mm vía `UnitConverter` y fuerzan `format = Custom`) y `setMarginLeft/Top/Right/Bottom` (vía el helper privado `updateMargin`, también convierte unidades) completan la edición. |
| `feature/editor/paper/PaperConfigScreen.kt` | Composable de edición de papel: `PaperConfigScreen` → `PaperConfigContent` con las secciones `FormatSection` (selector de `StandardPaperFormat`), `OrientationSection`, `UnitSection`, `CustomSizeSection` (campos de ancho/alto cuando el formato es `Custom`), `MarginsSection` (cuatro márgenes), `PrintableAreaSummary` (resumen del área imprimible resultante) y controles auxiliares `NumericField`/`formatNumber` para entrada numérica localizada. |

### 5.13 `feature/editor/grid/`

Pantalla principal del editor: configuración de grilla, imagen de referencia, medición, recorte y exportación. Es el núcleo funcional de GridLab.

| Archivo | Funcionamiento interno |
|---|---|
| `feature/editor/grid/GridConfigViewModel.kt` | El ViewModel más grande del proyecto (`@Suppress("TooManyFunctions")` porque concentra toda la edición del documento). `GridConfigUiState` agrupa: identidad y datos del documento (papel, grilla, imagen), disponibilidad de la imagen en disco (`imageAbsolutePath`, `imageAvailability`), estados de carga/guardado/importación, `isDirty` (hay cambios locales sin persistir), estado de la herramienta de medición (`measureMode`, `measurePoint1Mm`, `measurePoint2Mm` — la extensión `measureDistanceDisplay()` calcula la distancia entre ambos puntos en la unidad de visualización del papel usando `ViewportTransform.distanceMm` + `UnitConverter`), y estado de exportación (`selectedExportDpi`, `maxSafeExportDpi`, `isExporting`, `exportMessage`, `lastExportPath`). <br>**Carga:** `openDocument(id)` cancela cualquier autoguardado pendiente y carga el documento; `applyDocument(document)` sondea la disponibilidad de la imagen con `ImageStorage.probe`, resuelve su ruta absoluta si está `Present`, y recalcula `maxSafeExportDpi`/`selectedExportDpi` según la memoria máxima del dispositivo (`Runtime.getRuntime().maxMemory()`) vía `ExportSizeCalculator.maxSafeDpi`, prefiriendo 150 DPI si es seguro. <br>**Autoguardado:** cualquier edición marca `isDirty = true` y llama a `scheduleAutosave()`, que cancela el job anterior y programa uno nuevo con `delay(AUTOSAVE_DEBOUNCE_MS = 800ms)` antes de persistir (`persistIfDirty`). `saveDocument()` (guardado manual, actualiza las etiquetas de estado) y `flushPendingSave()` (guardado inmediato para `ON_STOP`/navegación atrás, sin tocar las etiquetas de estado) cancelan el autoguardado pendiente y guardan ya. <br>**Imagen:** `importImage(uri)` copia la nueva imagen vía `ImageStorage.copyFromUri`, borra la anterior si cambió de archivo, crea una `ReferenceImage` con transform por defecto (para que un re-enlace no arrastre offsets de una foto distinta) y persiste. `removeReferenceImage()` borra el archivo y limpia la referencia en el documento. Los setters `panImage`, `scaleImageBy`, `setImageOpacity`, `setImageLocked`, `setImageVisible`, `resetImageTransform`, `setImageRotation`, `setImageFilter` delegan en `ImageTransformCalculator` a través del helper privado `updateImage`. <br>**Grilla:** `setRows`/`setColumns` (limitados a `[1, MAX_DIVISIONS = 100]`), `setVisible`, `setStrokeWidthMm` (`[0.05, 5.0]`), `setOpacity` (`[0.05, 1.0]`), `setColorArgb`, `setSecondaryDivisions` (`[1, MAX_SECONDARY_DIVISIONS = 8]`), `setSecondaryVisible`, `setLabelsVisible`, todos vía el helper privado `updateGrid`. <br>**Medición:** `setMeasureMode(enabled)` activa/desactiva la herramienta y limpia los puntos al desactivar. `placeMeasurePoint(xPx, yPx, viewport)` convierte el toque en pantalla a mm de documento (`ViewportTransform.screenToDocumentMm`) y cicla: primer toque → punto 1, segundo → punto 2, tercero → reinicia en punto 1. `clearMeasurePoints()` los borra. <br>**Recorte:** `setCrop`/`clearCrop` delegan en `ImageTransformCalculator`. <br>**Exportación:** `setExportDpi(dpi)`; `exportJpeg/Png/Pdf()` llaman a `exportDocument(format)`, que primero valida con `ExportSizeCalculator.isDpiSafe` (si el DPI elegido excede el presupuesto de memoria seguro, deja `exportMessage = "DPI_TOO_HIGH"` y no exporta), y si es seguro, en `Dispatchers.IO`: fuerza un guardado pendiente (`flushPendingSaveSuspend`), arma el `Document` actual, resuelve el archivo de imagen si existe, construye un nombre de archivo saneado (`Regex("[^a-zA-Z0-9._-]")` reemplaza caracteres no válidos) dentro de `getExternalFilesDir(null)/exports/`, con sufijo `_<dpi>dpi.<ext>`, y llama al método correspondiente de `DocumentRenderer`. En éxito deja `exportMessage = "OK"` y `lastExportPath`; en fallo, `"FAILED"`. |
| `feature/editor/grid/GridConfigScreen.kt` | Composable principal del editor (el archivo más grande del proyecto). `GridConfigScreen(documentId, onBack, modifier)` obtiene el ViewModel, llama `openDocument(documentId)` y usa el ciclo de vida (`DisposableEffect`/observer de `ON_STOP`) para disparar `flushPendingSave()` al salir. `GridConfigContent` arma el scaffold general del editor con las distintas secciones colapsables. Piezas internas destacables: <br>• `DocumentPreview` — el lienzo central interactivo: dibuja el papel (`PaperBackgroundCanvas`), la capa de imagen (`PreviewImageLayer` → `ReferenceImageLayer`, que aplica recorte normalizado con `clipToCropRect` y gestos de pan/zoom/rotación cuando la imagen no está bloqueada) y, encima, la grilla y etiquetas dibujadas directamente sobre `DrawScope` (`drawGridLines`, `drawGridLabels`, más `drawMeasureOverlay` para los puntos/línea de la herramienta de medición). `rememberPreviewMetrics` calcula el `ViewportTransform` según el tamaño disponible en pantalla y el tamaño del papel. <br>• `MeasureSection` — UI de la herramienta de medición (activar/desactivar, mostrar distancia vía `measureDistanceDisplay()`, limpiar puntos). <br>• `ExportSection` — selector de DPI (`SUGGESTED_DPI` de `ExportSizeCalculator`), aviso cuando el DPI elegido supera `maxSafeExportDpi`, y los tres botones de exportación (JPG/PNG/PDF). <br>• `ImageImportSection`, `ImageTransformSection` (con `ImageVisibilityLockChips`, `ImageOpacityControls`, `ImageRotationControls`) — UI para importar/reemplazar imagen y controlar su transformación. <br>Las secciones de grilla en sí (tamaño, visibilidad, grosor, opacidad, color, grilla secundaria, etiquetas, filtro de imagen y recorte) están factorizadas en `GridConfigSections.kt` para mantener este archivo más manejable. |
| `feature/editor/grid/GridConfigSections.kt` | Composables `internal` reutilizados por `GridConfigScreen`, uno por bloque de configuración: `GridSizeSection` (filas/columnas), `GridVisibilitySection`, `GridSecondarySection` (activar grilla secundaria + número de subdivisiones por celda), `GridLabelsSection`, `ImageFilterSection` (chips `None`/`Grayscale`/`HighContrast`), `GridStrokeSection` (grosor de línea), `GridOpacitySection`, `GridColorSection` (paleta de colores predefinidos para la línea), `GridSaveSection` (botón de guardado manual + etiqueta de estado según `isSaving`/`lastSaveSucceeded`), `CropSection` (controles de recorte no destructivo, con `CropEdgeSlider` por cada borde) e `IntField` (campo numérico entero reutilizable). Todos reciben el `GridConfigViewModel` (o el estado puntual necesario) y llaman directamente a sus setters. |

### 5.14 `feature/settings/`

| Archivo | Funcionamiento interno |
|---|---|
| `feature/settings/SettingsScreen.kt` | Pantalla raíz de Ajustes. `SettingsScreen(onBack, onOpenSourceLicenses, modifier)` muestra una lista simple de secciones; actualmente la única entrada es "Licencias de código abierto", que navega a `OpenSourceLicensesScreen`. |
| `feature/settings/OpenSourceLicensesScreen.kt` | Pantalla que renderiza como Markdown el archivo de atribución de licencias de terceros. Con `LocalContext` lee desde `assets/licenses/` el archivo correspondiente al idioma del dispositivo: `third_party_licenses_es.md` si el locale es español, o `third_party_licenses.md` (inglés) en cualquier otro caso; la lectura ocurre en un `LaunchedEffect` y mientras tanto se muestra un `CircularProgressIndicator`; si falla, se muestra `settings_licenses_load_error`. El contenido se pinta con la librería `multiplatform-markdown-renderer` (`Markdown` + `markdownColor`/`markdownTypography`/`markdownDimens` de la variante Material3), con ajustes propios de densidad de tabla (celdas de 220dp, padding reducido, tipografía de tabla más chica) pensados para que coordenadas Maven largas (`groupId:artifactId:version`) sigan siendo legibles sin romper el layout. |

---

## 6. Tests (`app/src/test`)

Tests unitarios JVM puros (sin emulador/dispositivo), ejecutados con `:app:testDebugUnitTest`.

| Archivo | Funcionamiento interno |
|---|---|
| `app/src/test/kotlin/com/gridlab/app/ArchitectureTest.kt` | Tests de **arquitectura** con la librería Konsist: verifican reglas estructurales del código en vez de comportamiento, por ejemplo que los paquetes `feature.*` no se importen entre sí y que `core` no dependa de `feature`. Corre dentro de la suite de tests unitarios normal (el job `quality` del CI lo incluye). |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ExportSizeCalculatorTest.kt` | Verifica `widthPx`/`heightPx` para A4 a 150 DPI y, por extensión, la conversión mm→px de `ExportSizeCalculator`. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridCalculatorTest.kt` | Verifica que para A4 portrait con grilla 10×10 el tamaño de celda calculado sea el esperado (21.0 × 29.7 mm), entre otros casos de `GridCalculator.calculate`. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridLabelFormatterTest.kt` | Verifica `columnLabel` (base 1: "1", "10", …) y `rowLabel` (letras: "A", "Z", "AA", …) de `GridLabelFormatter`. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ViewportTransformTest.kt` | Verifica el roundtrip `screenToDocumentMm` → `documentToScreenPx` y el cálculo de `distanceMm` entre dos puntos conocidos. |

---

## 7. Recursos (`res/`)

Recursos Android estándar: su comportamiento no cambia por tratarse de GridLab en particular, pero se listan para que cualquiera pueda ubicar qué contiene cada carpeta.

| Carpeta / archivo | Contenido |
|---|---|
| `res/drawable-nodpi/gridlab_banner_goku.jpg` | Imagen del banner mostrado en la pantalla de inicio (`HomeHeroSection`). |
| `res/drawable-nodpi/gridlab_icon.png` | Ícono de marca usado dentro de la UI (no es el ícono de lanzador del sistema, ese vive en `mipmap-anydpi-v26`). |
| `res/drawable/ic_add.xml` | Ícono vectorial "+", botón de nuevo proyecto. |
| `res/drawable/ic_arrow_back.xml` | Ícono de flecha atrás, usado en las barras superiores de Ajustes/Licencias. |
| `res/drawable/ic_brush_filled.xml` | Ícono de pincel (relacionado con la temática de dibujo/arte de la app). |
| `res/drawable/ic_circle_filled.xml` | Ícono de círculo relleno, usado como indicador/bullet en la UI. |
| `res/drawable/ic_close.xml` | Ícono "cerrar" (X). |
| `res/drawable/ic_delete_filled.xml` | Ícono de papelera, acción de borrar proyecto(s). |
| `res/drawable/ic_edit_filled.xml` | Ícono de lápiz, acción de renombrar. |
| `res/drawable/ic_image_filled.xml` | Ícono de imagen, sección de imagen de referencia. |
| `res/drawable/ic_launcher_background.xml` | Capa de fondo del ícono adaptativo de la app. |
| `res/drawable/ic_launcher_foreground.xml` | Capa de primer plano del ícono adaptativo de la app. |
| `res/drawable/ic_photo_library_filled.xml` | Ícono de galería de fotos, acción de importar imagen. |
| `res/drawable/ic_radio_button_checked.xml` / `ic_radio_button_unchecked.xml` | Par de íconos de radio button (seleccionado/no seleccionado), usados en selección de formato/opciones. |
| `res/drawable/ic_settings_filled.xml` | Ícono de engranaje, acceso a Ajustes. |
| `res/font/ibmplexsans_*.otf` (10 archivos) | Pesos y estilos de la fuente **IBM Plex Sans** (regular, "text", medium, semibold, bold, cada uno con su itálica) que arma `ui/theme/Type.kt`. |
| `res/mipmap-anydpi-v26/ic_launcher.xml` / `ic_launcher_round.xml` | Definiciones del ícono adaptativo de lanzador (API 26+), combinan `ic_launcher_background.xml` + `ic_launcher_foreground.xml`. |
| `res/values/strings.xml` | Todos los textos de la UI en inglés (idioma base): títulos de sección, etiquetas de papel/grilla/imagen/medición/recorte, textos de exportación, mensajes de error, textos del listado de proyectos y de Ajustes/Licencias, y el `string-array` `home_taglines` con los eslóganes rotativos de la portada. |
| `res/values/themes.xml` | Tema base `Theme.GridLab` (API < 29): barras de sistema transparentes y `windowDrawsSystemBarBackgrounds`. |
| `res/values-v29/themes.xml` | Variante del mismo tema para API 29+: además desactiva los scrims de contraste automáticos (`enforceStatusBarContrast`/`enforceNavigationBarContrast`) y el "force dark" (`forceDarkAllowed`), en línea con lo que aplica en tiempo de ejecución `ui/EdgeToEdge.kt`. |

---

## 8. Assets (licencias de terceros)

| Archivo | Contenido |
|---|---|
| `app/src/main/assets/licenses/third_party_licenses.md` | Texto en **inglés** con la atribución de licencias de todas las librerías de terceros usadas por GridLab. Se muestra en `OpenSourceLicensesScreen` cuando el idioma del dispositivo no es español. |
| `app/src/main/assets/licenses/third_party_licenses_es.md` | Misma atribución, traducida al **español**. Se muestra cuando el locale del dispositivo es español. |

---

## 9. Historial de versiones / pendientes

| Versión | Contenido entregado |
|---|---|
| 0.23.0 | Pantalla de Ajustes accesible desde el ícono de la Home; bloque de licencias de código abierto con renderizado Markdown desde `assets/licenses/`; tabla de licencias ajustada para coordenadas Maven largas; navegación raíz manual en `MainActivity` (Home → Settings → OpenSourceLicenses; Editor sin cambios); exportación a JPG/PNG/PDF; edge-to-edge API 27–37. |

| Pendiente (deferred) | Detalle |
|---|---|
| 0.24 | Información de almacenamiento / datos de la app; limpieza de Detekt; `values-es` para los strings de la UI (hoy todo el texto de `strings.xml` está solo en inglés). |
