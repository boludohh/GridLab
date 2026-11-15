# Índice de archivos — GridLab

Este documento describe la responsabilidad de cada archivo del proyecto según la estructura actual del repositorio. Las rutas son relativas a la raíz del repositorio (`GridLab/`).

## Índice

1. [Project root]
2. [Continuous integration]
3. [Android app module]
4. [Application entry point]
5. [Core — data and persistence]
6. [Core — design system and preferences]
7. [Core — domain models]
8. [Core — geometry and calculations]
9. [Feature — Home and projects]
10. [Feature — Editor]
11. [Feature — Paper configuration screen]
12. [Feature — Export]
13. [Feature — Settings and information]
14. [Unit tests]

---

## 1. Project root

| Archivo | Función |
|---|---|
| `build.gradle.kts` | Declara los plugins Gradle de nivel raíz y las tareas de compilación compartidas del proyecto. |
| `settings.gradle.kts` | Configura los repositorios de plugins y dependencias e incluye el único módulo de aplicación Android, :app. |
| `gradle.properties` | Contiene la configuración global de Gradle, Android, Kotlin y la JVM de compilación. |
| `gradle/libs.versions.toml` | Catálogo centralizado de versiones para los plugins de Gradle y las dependencias de la aplicación. |
| `gradle/wrapper/gradle-wrapper.properties` | Fija la distribución de Gradle que utiliza el wrapper del proyecto. |
| `gradle/wrapper/gradle-wrapper.jar` | JAR ejecutable de Gradle Wrapper, utilizado por los scripts del wrapper. |
| `gradlew / gradlew.bat` | Scripts de ejecución del wrapper para sistemas Unix y Windows. |
| `.gitignore` | Enumera archivos generados, material local de firma y otros archivos que Git no debe versionar. |
| `LICENSE` | Los términos de la Licencia Pública General de GNU versión 3.0 aplicables al código fuente de GridLab. |
| `README.md` | Presentación pública del proyecto, resumen de funciones, requisitos de plataforma, pautas de contribución y enlaces a la documentación. |

## 2. Continuous integration

| Archivo | Función |
|---|---|
| `.github/workflows/ci.yml` | Workflow de GitHub Actions que compila los APK debug y release y ejecuta los tests unitarios y las comprobaciones de Detekt. |

## 3. Android app module

| Archivo | Función |
|---|---|
| `app/build.gradle.kts` | Configura la aplicación Android, los niveles de SDK, la versión, los objetivos Java/Kotlin, la firma release, la reducción de código y las dependencias. |
| `app/proguard-rules.pro` | Reglas ProGuard/R8 específicas del proyecto utilizadas por la compilación release optimizada. |
| `app/keystore/.gitkeep` | Mantiene en Git el directorio local de keystore aunque esté vacío; la clave real de firma no se almacena en el repositorio. |
| `app/src/main/AndroidManifest.xml` | Declara la clase Application, la actividad de inicio, los iconos, el tema y los metadatos de la aplicación Android. |

## 4. Application entry point

| Archivo | Función |
|---|---|
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | Clase Application anotada con @HiltAndroidApp; inicializa el grafo raíz de dependencias de Hilt. |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | Actividad única y contenedor de navegación Compose. Conecta Inicio, el editor, Ajustes, idioma/apariencia, Acerca de y licencias de código abierto; también coordina los cambios de tema. |

## 5. Core — data and persistence

| Archivo | Función |
|---|---|
| `core/data/database/AppDatabase.kt` | Definición de la base de datos Room y punto de acceso al DAO de proyectos. |
| `core/data/database/DocumentEntity.kt` | Entidad Room que almacena un documento de proyecto y sus datos serializados. |
| `core/data/database/ProjectDao.kt` | Consultas DAO de Room para recuperar, insertar/actualizar y eliminar registros de proyectos. |
| `core/data/di/DatabaseModule.kt` | Proveedores Hilt para la base de datos Room y ProjectDao. |
| `core/data/di/RepositoryModule.kt` | Vincula ProjectRepository con su implementación ProjectRepositoryImpl en el componente Hilt de toda la aplicación. |
| `core/data/repository/ProjectRepository.kt` | Contrato del repositorio para leer, listar, guardar y eliminar documentos de proyectos. |
| `core/data/repository/ProjectRepositoryImpl.kt` | Implementa el repositorio de proyectos mediante el DAO y convierte entre entidades Room y documentos de dominio. |
| `core/data/storage/ImageStorage.kt` | Copia URI de imágenes importadas al almacenamiento privado de proyectos, resuelve rutas, comprueba la disponibilidad de imágenes y elimina archivos o directorios de proyectos. |

## 6. Core — design system and preferences

| Archivo | Función |
|---|---|
| `core/designsystem/AppLocale.kt` | Aplica el idioma seleccionado de la aplicación mediante la configuración regional de Android. |
| `core/designsystem/EdgeToEdge.kt` | Centraliza la configuración edge-to-edge de la ventana y las barras del sistema para las versiones de Android compatibles. |
| `core/designsystem/theme/Color.kt` | Define los valores de color base utilizados por las paletas clara, oscura y negra de la aplicación. |
| `core/designsystem/theme/Theme.kt` | Define los modos de tema, los roles semánticos de color, el CompositionLocal del tema y el composable raíz GridLabTheme. |
| `core/designsystem/theme/ThemeModeExt.kt` | Proporciona funciones auxiliares para alternar los modos de tema y obtener el recurso de icono correspondiente. |
| `core/designsystem/theme/ThemeReveal.kt` | Implementa la transición circular entre temas, su estado de animación, la superposición y la función auxiliar para capturar el bitmap de la actividad. |
| `core/designsystem/theme/Type.kt` | Construye la tipografía Material 3 de la aplicación a partir de los archivos de fuente IBM Plex Sans incluidos. |
| `core/preferences/AppLanguage.kt` | Define los idiomas compatibles con la aplicación y relaciona las etiquetas de idioma con esos valores. |
| `core/preferences/ThemePreferencesRepository.kt` | Almacena y expone el modo de tema y el idioma seleccionados mediante flujos de Preferences DataStore. |
| `core/preferences/di/PreferencesModule.kt` | Proporciona la instancia de Preferences DataStore mediante Hilt. |

## 7. Core — domain models

| Archivo | Función |
|---|---|
| `core/model/Document.kt` | Modelo de dominio del proyecto que contiene su identidad, nombre, papel, cuadrícula, imagen de referencia y versión del esquema. |
| `core/model/ExportFormat.kt` | Enumera los formatos de exportación compatibles. |
| `core/model/Grid.kt` | Almacena la configuración de la cuadrícula principal y secundaria y la visibilidad de las etiquetas. |
| `core/model/GridLineStyle.kt` | Almacena el color, el grosor físico del trazo y la opacidad de una línea de cuadrícula. |
| `core/model/ImageAvailability.kt` | Representa si una imagen de referencia está ausente, disponible, perdida o no se puede leer. |
| `core/model/ImageFilter.kt` | Enumera los filtros no destructivos de la imagen de referencia. |
| `core/model/LengthUnit.kt` | Enumera las unidades de medida utilizadas para mostrar y editar las dimensiones del papel. |
| `core/model/Paper.kt` | Almacena las dimensiones del papel, el formato estándar/personalizado, la orientación, los márgenes y la unidad de visualización; internamente las dimensiones se representan en milímetros. |
| `core/model/PaperOrientation.kt` | Enumera las orientaciones vertical y horizontal del papel. |
| `core/model/ReferenceImage.kt` | Almacena la ruta de la imagen de referencia y sus ajustes de transformación, visibilidad, bloqueo, opacidad, rotación, recorte y filtro. |
| `core/model/StandardPaperFormat.kt` | Define los formatos de papel estándar compatibles y la opción de tamaño personalizado. |

## 8. Core — geometry and calculations

| Archivo | Función |
|---|---|
| `core/geometry/ExportSizeCalculator.kt` | Convierte las dimensiones físicas del papel y los DPI en dimensiones de píxeles para exportación y estima si la exportación es segura según la memoria disponible del dispositivo. |
| `core/geometry/GridCalculator.kt` | Calcula las dimensiones del área imprimible, el tamaño de las celdas y las posiciones de las líneas principales y secundarias. |
| `core/geometry/GridLabelFormatter.kt` | Formatea las etiquetas de columnas como números y las filas como letras al estilo de las hojas de cálculo. |
| `core/geometry/ImageFilterMatrices.kt` | Proporciona las matrices de color para los filtros compatibles de la imagen de referencia. |
| `core/geometry/ImageTransformCalculator.kt` | Contiene cálculos inmutables para mover, escalar, rotar, recortar, bloquear, mostrar y aplicar filtros a una imagen de referencia. |
| `core/geometry/ReferenceImagePlacement.kt` | Calcula el rectángulo de origen recortado y el rectángulo de destino utilizado para ajustar una imagen de referencia al área imprimible. |
| `core/geometry/UnitConverter.kt` | Convierte longitudes entre milímetros, centímetros y pulgadas. |
| `core/geometry/ViewportTransform.kt` | Convierte coordenadas entre la vista previa en pantalla y el espacio del documento, y calcula distancias en las unidades del documento. |

## 9. Feature — Home and projects

| Archivo | Función |
|---|---|
| `feature/main/MainScreen.kt` | Pantalla Inicio en Compose que muestra la presentación de la app, la creación y lista de proyectos, miniaturas y acciones para renombrar, eliminar o seleccionar proyectos. |
| `feature/main/MainViewModel.kt` | Mantiene el estado de Inicio y coordina el listado, creación, renombrado, selección múltiple y eliminación de proyectos mediante el repositorio. |

## 10. Feature — Editor

| Archivo | Función |
|---|---|
| `feature/editor/EditorScreen.kt` | Interfaz principal del editor. Muestra la vista previa permanente del papel, la barra de herramientas y el panel seleccionado de Papel, Cuadrícula, Imagen, Medición o Exportación. |
| `feature/editor/EditorViewModel.kt` | Gestiona el estado y las operaciones del editor: carga y guardado del documento, configuración de papel/cuadrícula, importación y transformación de imágenes, medición y coordinación de exportaciones. |
| `feature/editor/components/HsvColorPicker.kt` | Selector de color HSV reutilizable y campo de color utilizados para elegir colores personalizados en el editor. |
| `feature/editor/panels/EditorNumericFields.kt` | Composables reutilizables de entrada numérica para los ajustes del editor. |
| `feature/editor/panels/EditorPanelExport.kt` | Panel del editor para elegir la resolución de exportación y exportar el documento en un formato compatible. |
| `feature/editor/panels/EditorPanelGrid.kt` | Panel del editor para los ajustes de la cuadrícula principal/secundaria y sus etiquetas. |
| `feature/editor/panels/EditorPanelImage.kt` | Panel del editor para importar y configurar la imagen de referencia, incluida su visibilidad, bloqueo, opacidad, posición, recorte, rotación y filtros. |
| `feature/editor/panels/EditorPanelMeasure.kt` | Panel del editor para activar la medición, mostrar la distancia medida y borrar los puntos. |
| `feature/editor/panels/EditorPanelPaper.kt` | Panel del editor para el formato, orientación, dimensiones, unidades y márgenes del papel. |
| `feature/editor/panels/EditorToolbar.kt` | Define los identificadores de herramientas del editor y la barra utilizada para cambiar entre sus paneles. |
| `feature/editor/panels/GridConfigSections.kt` | Contiene secciones reutilizables de controles de cuadrícula, cuadrícula secundaria, etiquetas, estilo de línea, visibilidad, estado de guardado y recorte utilizadas por el editor. |

## 11. Feature — Paper configuration screen

| Archivo | Función |
|---|---|
| `feature/editor/paper/PaperConfigScreen.kt` | Pantalla Compose independiente para editar el formato, orientación, unidades, dimensiones personalizadas y márgenes del papel. |
| `feature/editor/paper/PaperConfigViewModel.kt` | Estado de UI y operaciones de configuración del papel para PaperConfigScreen, incluida la conversión de unidades y los tamaños estándar/personalizados. |

## 12. Feature — Export

| Archivo | Función |
|---|---|
| `feature/editor/export/DocumentRenderer.kt` | Renderiza el papel configurado, la imagen de referencia, la cuadrícula y las etiquetas en un bitmap y genera archivos JPG, PNG o PDF de una página. |
| `feature/editor/export/ExportStorage.kt` | Proporciona destinos y flujos de salida para exportaciones y gestiona las comprobaciones de permisos de almacenamiento de la plataforma requeridas por el flujo de exportación. |

## 13. Feature — Settings and information

| Archivo | Función |
|---|---|
| `feature/settings/SettingsScreen.kt` | Pantalla de Ajustes con navegación a idioma/apariencia, Acerca de e información de licencias de código abierto. |
| `feature/settings/LanguageAppearanceScreen.kt` | Pantalla para seleccionar el idioma de la aplicación y alternar entre los modos de tema disponibles. |
| `feature/settings/AboutScreen.kt` | Muestra la identidad de GridLab, la versión de la aplicación, información del desarrollador y enlaces externos del proyecto y redes sociales. |
| `feature/settings/OpenSourceLicensesScreen.kt` | Carga desde los assets el documento localizado de licencias de terceros y lo muestra como Markdown. |

## 14. Unit tests

| Archivo | Función |
|---|---|
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ExportSizeCalculatorTest.kt` | Pruebas del cálculo de píxeles de exportación, estimaciones de seguridad de memoria y entradas de exportación no válidas. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridCalculatorTest.kt` | Pruebas de los cálculos de distribución de cuadrícula, márgenes imprimibles, orientaciones y subdivisiones secundarias. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridLabelFormatterTest.kt` | Pruebas de las etiquetas numéricas de columnas y alfabéticas de filas. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ViewportTransformTest.kt` | Pruebas de conversión de coordenadas pantalla/documento y cálculos de distancias en el espacio del documento. |

## 15. Recursos Android (`app/src/main/res/`)

| Archivo | Función |
|---|---|
| `drawable-nodpi/gridlab_banner_goku.jpg` | Ilustración/banner principal mostrado en la pantalla Inicio. |
| `drawable-nodpi/gridlab_icon.png` | PNG del icono de marca de GridLab utilizado dentro de la interfaz y en la pantalla Acerca de. |
| `drawable/ic_add.xml` | Icono de acción agregar, utilizado para crear un proyecto. |
| `drawable/ic_add_photo_alternate_filled.xml` | Icono de agregar imagen para importar una imagen de referencia. |
| `drawable/ic_architecture.xml` | Icono de arquitectura/documentación. |
| `drawable/ic_arrow_back.xml` | Icono de navegación hacia atrás. |
| `drawable/ic_article_filled.xml` | Icono de artículo/documento. |
| `drawable/ic_aspect_ratio_filled.xml` | Icono de proporción de aspecto o proporciones del papel. |
| `drawable/ic_border_outer.xml` | Icono de borde exterior utilizado en controles relacionados con papel/cuadrícula. |
| `drawable/ic_brush_filled.xml` | Icono de pincel para la identidad visual relacionada con el dibujo. |
| `drawable/ic_chevron_right.xml` | Chevron hacia adelante utilizado en filas de navegación. |
| `drawable/ic_circle_filled.xml` | Indicador circular relleno. |
| `drawable/ic_close.xml` | Icono para cerrar o descartar. |
| `drawable/ic_code.xml` | Icono de código fuente. |
| `drawable/ic_crop.xml` | Icono de recorte de imagen. |
| `drawable/ic_dark_mode_filled.xml` | Icono del tema oscuro. |
| `drawable/ic_delete_filled.xml` | Icono de acción eliminar. |
| `drawable/ic_edit_filled.xml` | Icono de acción editar o renombrar. |
| `drawable/ic_file_export_filled.xml` | Icono de exportación de archivo. |
| `drawable/ic_github.xml` | Icono de la marca GitHub. |
| `drawable/ic_grid_3x3.xml` | Icono de cuadrícula preestablecida de 3 por 3. |
| `drawable/ic_grid_4x4.xml` | Icono de cuadrícula preestablecida de 4 por 4. |
| `drawable/ic_grid_on_outline.xml` | Icono de contorno/visibilidad de cuadrícula. |
| `drawable/ic_high_quality_filled.xml` | Icono de alta calidad o resolución. |
| `drawable/ic_image_filled.xml` | Icono de imagen de referencia. |
| `drawable/ic_info_filled.xml` | Icono de información. |
| `drawable/ic_instagram.xml` | Icono de la marca Instagram. |
| `drawable/ic_ios_share.xml` | Icono de acción compartir/exportar. |
| `drawable/ic_label_filled.xml` | Icono de etiquetas de cuadrícula. |
| `drawable/ic_launcher_background.xml` | Capa de fondo del icono adaptable del lanzador. |
| `drawable/ic_launcher_foreground.xml` | Capa frontal del icono adaptable del lanzador. |
| `drawable/ic_license.xml` | Icono de licencias de código abierto. |
| `drawable/ic_light_mode_filled.xml` | Icono del tema claro. |
| `drawable/ic_line_weight.xml` | Icono de grosor de línea/trazo. |
| `drawable/ic_lock_filled.xml` | Icono del estado de bloqueo de la imagen de referencia. |
| `drawable/ic_new_releases.xml` | Icono de información de versiones/actualizaciones. |
| `drawable/ic_night_mode_filled.xml` | Icono del tema negro/nocturno. |
| `drawable/ic_opacity.xml` | Icono de control de opacidad. |
| `drawable/ic_open_with.xml` | Icono de control para mover/posicionar. |
| `drawable/ic_palette_filled.xml` | Icono de paleta de colores. |
| `drawable/ic_photo_library_filled.xml` | Icono de biblioteca de fotos/importación. |
| `drawable/ic_radio_button_checked.xml` | Icono de opción de radio seleccionada. |
| `drawable/ic_radio_button_unchecked.xml` | Icono de opción de radio no seleccionada. |
| `drawable/ic_rotate_right.xml` | Icono para rotar la imagen. |
| `drawable/ic_screen_rotation_alt.xml` | Icono de orientación de pantalla/papel. |
| `drawable/ic_settings_filled.xml` | Icono de navegación a Ajustes. |
| `drawable/ic_square_foot_filled.xml` | Icono de medición/dimensiones. |
| `drawable/ic_straighten_filled.xml` | Icono de regla/herramienta de medición. |
| `drawable/ic_tiktok.xml` | Icono de la marca TikTok. |
| `drawable/ic_tune.xml` | Icono de controles de ajuste. |
| `drawable/ic_visibility_filled.xml` | Icono de alternancia de visibilidad. |
| `font/ibmplexsans_bold.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_bold_italic.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_medium.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_medium_italic.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_regular.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_regular_italic.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_semibold.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_semibold_italic.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_text.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `font/ibmplexsans_text_italic.otf` | Estilo de fuente IBM Plex Sans incluido y utilizado por la tipografía de la aplicación. |
| `mipmap-anydpi-v26/ic_launcher.xml` | Definición del icono adaptable del lanzador para Android 8.0 y versiones posteriores. |
| `mipmap-anydpi-v26/ic_launcher_round.xml` | Definición del icono adaptable del lanzador para Android 8.0 y versiones posteriores. |
| `values-es/strings.xml` | Recursos de cadenas de la interfaz en español. |
| `values-v29/themes.xml` | Recursos del tema Android para esta configuración de nivel de API. |
| `values/strings.xml` | Textos predeterminados de la interfaz (inglés) y arreglos de cadenas. |
| `values/themes.xml` | Recursos del tema Android para esta configuración de nivel de API. |
| `xml/locales_config.xml` | Recurso de configuración XML de Android para los idiomas compatibles de la aplicación. |

## 16. Assets

| Archivo | Función |
|---|---|
| `app/src/main/assets/licenses/third_party_licenses.md` | Texto en inglés de licencias y atribuciones de dependencias de terceros; se muestra cuando el idioma de la app no es español. |
| `app/src/main/assets/licenses/third_party_licenses_es.md` | Traducción al español del texto de licencias y atribuciones de dependencias de terceros. |

## Documentación

| Archivo | Función |
|---|---|
| `docs/architecture/FILES.md` | Versión en inglés de este índice de responsabilidades de archivos. |
| `docs/architecture/FILES-es.md` | Versión en español de este índice de responsabilidades de archivos. |
| `docs/assets/gridlab_icon.png` | Copia del logo PNG utilizada por el README público de GitHub. |
