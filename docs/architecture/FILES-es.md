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
| `build.gradle.kts` | Declara los plugins de Gradle de nivel raíz y las tareas compartidas del proyecto. |
| `settings.gradle.kts` | Configura los repositorios de plugins y dependencias e incluye el único módulo de aplicación Android, :app. |
| `gradle.properties` | Contiene la configuración de Gradle, Android, Kotlin y JVM de todo el proyecto. |
| `gradle/libs.versions.toml` | Catálogo central de versiones para los plugins y dependencias de la aplicación. |
| `gradle/wrapper/gradle-wrapper.properties` | Fija la distribución de Gradle usada por el wrapper del proyecto. |
| `gradle/wrapper/gradle-wrapper.jar` | JAR ejecutable del wrapper de Gradle, usado por los scripts del wrapper. |
| `gradlew / gradlew.bat` | Scripts de lanzamiento del wrapper para sistemas tipo Unix y Windows. |
| `.gitignore` | Enumera los archivos generados, el material de firma local y otros archivos que Git no debe rastrear. |
| `LICENSE` | Términos de la licencia GNU General Public License versión 3.0 para el código fuente de GridLab. |
| `README.md` | Descripción general pública del proyecto, resumen de funciones, requisitos de plataforma, guía de contribución y enlaces a la documentación. |

## 2. Continuous integration

| Archivo | Función |
|---|---|
| `.github/workflows/ci.yml` | Workflow de GitHub Actions que ejecuta los tests unitarios y el lint de Android, y después compila los APK debug y release y sube los APK y los informes de verificación. |

## 3. Android app module

| Archivo | Función |
|---|---|
| `app/build.gradle.kts` | Configura la aplicación Android, los niveles de SDK, la versión, los objetivos Java/Kotlin, la firma de release, la reducción de código y las dependencias. |
| `app/proguard-rules.pro` | Reglas de ProGuard/R8 específicas del proyecto usadas por la compilación de release optimizada. |
| `app/keystore/.gitkeep` | Mantiene en el control de versiones el directorio de keystore local, que de otro modo estaría vacío; la clave de firma real no se almacena en el repositorio. |
| `app/src/main/AndroidManifest.xml` | Declara la clase de aplicación, la actividad de lanzamiento, los iconos de la app, el tema y los metadatos de la aplicación Android. |

## 4. Application entry point

| Archivo | Función |
|---|---|
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | Clase de aplicación anotada con @HiltAndroidApp; inicializa el grafo raíz de dependencias de Hilt. |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | Actividad única y host de navegación de Compose. Conecta las pantallas de inicio, editor, ajustes, idioma/apariencia, Acerca de y licencias de código abierto; también coordina los cambios de tema. |

## 5. Core — data and persistence

| Archivo | Función |
|---|---|
| `core/data/database/AppDatabase.kt` | Definición de la base de datos Room y punto de acceso al DAO de proyectos. |
| `core/data/database/DocumentEntity.kt` | Entidad de Room que almacena un documento de proyecto y sus datos serializados. |
| `core/data/database/ProjectDao.kt` | Consultas del DAO de Room para recuperar, insertar/actualizar y eliminar registros de proyectos. |
| `core/data/di/DatabaseModule.kt` | Proveedores de Hilt para la base de datos Room y el ProjectDao. |
| `core/data/di/RepositoryModule.kt` | Vincula ProjectRepository con su implementación ProjectRepositoryImpl en el componente Hilt de toda la aplicación. |
| `core/data/repository/ProjectRepository.kt` | Contrato del repositorio para leer, listar, guardar y eliminar documentos de proyecto. |
| `core/data/repository/ProjectRepositoryImpl.kt` | Implementa el repositorio de proyectos usando el DAO y mapea entre entidades de Room y documentos de dominio. |
| `core/data/storage/ImageStorage.kt` | Copia las URI de imágenes importadas al almacenamiento privado de proyectos de la app, resuelve rutas de imagen, comprueba la disponibilidad de imágenes y elimina archivos de imagen o directorios de proyecto. |

## 6. Core — design system and preferences

| Archivo | Función |
|---|---|
| `core/designsystem/AppLocale.kt` | Aplica el idioma de la aplicación seleccionado a través de la configuración regional de Android. |
| `core/designsystem/EdgeToEdge.kt` | Centraliza la configuración de ventana de borde a borde y de las barras del sistema para las versiones compatibles de Android. |
| `core/designsystem/theme/Color.kt` | Define los valores de color sin procesar usados por las paletas de tema claro, oscuro y negro de la aplicación. |
| `core/designsystem/theme/Theme.kt` | Define los modos de tema, los roles semánticos de color, el CompositionLocal del tema y el composable raíz GridLabTheme. |
| `core/designsystem/theme/ThemeModeExt.kt` | Proporciona ayudantes para recorrer los modos de tema y obtener el recurso de icono de tema correspondiente. |
| `core/designsystem/theme/ThemeReveal.kt` | Implementa la revelación circular de transición de tema, su estado de animación, la superposición y el ayudante de captura de bitmap de la actividad. |
| `core/designsystem/theme/Type.kt` | Construye la tipografía de Material 3 de la app a partir de los archivos de fuente IBM Plex Sans incluidos. |
| `core/preferences/AppLanguage.kt` | Define los valores de idioma compatibles de la app y asigna etiquetas de idioma a esos valores. |
| `core/preferences/ThemePreferencesRepository.kt` | Persiste y expone el modo de tema y el idioma seleccionados mediante flujos de Preferences DataStore. |
| `core/preferences/di/PreferencesModule.kt` | Proporciona la instancia de Preferences DataStore a través de Hilt. |

## 7. Core — domain models

| Archivo | Función |
|---|---|
| `core/model/Document.kt` | Modelo de dominio de nivel de proyecto que contiene su identidad, nombre, papel, cuadrícula, imagen de referencia y versión de esquema. |
| `core/model/ExportFormat.kt` | Enumera los formatos de exportación compatibles. |
| `core/model/Grid.kt` | Almacena la configuración de la cuadrícula principal y secundaria y la visibilidad de las etiquetas. |
| `core/model/GridLineStyle.kt` | Almacena el color, el grosor físico del trazo y la opacidad de una línea de cuadrícula. |
| `core/model/ImageAvailability.kt` | Representa si una imagen referenciada está ausente, presente, perdida o ilegible. |
| `core/model/ImageFilter.kt` | Enumera los filtros no destructivos de la imagen de referencia. |
| `core/model/LengthUnit.kt` | Enumera las unidades de medida usadas para mostrar y editar las dimensiones del papel. |
| `core/model/Paper.kt` | Almacena las dimensiones del papel, el formato estándar/personalizado, la orientación, los márgenes y la unidad de visualización; las dimensiones se representan internamente en milímetros. |
| `core/model/PaperOrientation.kt` | Enumera las orientaciones vertical y horizontal del papel. |
| `core/model/ReferenceImage.kt` | Almacena la ruta de la imagen de referencia y su configuración de transformación, visibilidad, bloqueo, opacidad, rotación, recorte y filtro. |
| `core/model/StandardPaperFormat.kt` | Define los formatos de papel estándar compatibles y la opción de tamaño personalizado. |

## 8. Core — geometry and calculations

| Archivo | Función |
|---|---|
| `core/geometry/ExportSizeCalculator.kt` | Convierte las dimensiones físicas del papel y los DPI en dimensiones de píxeles de exportación y estima si una exportación es segura para el presupuesto de memoria del dispositivo. |
| `core/geometry/GridCalculator.kt` | Calcula las dimensiones del área imprimible, los tamaños de celda de la cuadrícula y las posiciones de las líneas principales y secundarias. |
| `core/geometry/GridLabelFormatter.kt` | Formatea las etiquetas de columna de la cuadrícula como números y las etiquetas de fila como letras al estilo de hoja de cálculo. |
| `core/geometry/ImageFilterMatrices.kt` | Proporciona las matrices de color para los filtros de imagen de referencia compatibles. |
| `core/geometry/ImageTransformCalculator.kt` | Contiene los cálculos inmutables para mover, escalar, rotar, recortar, bloquear, mostrar y filtrar una imagen de referencia. |
| `core/geometry/ReferenceImagePlacement.kt` | Calcula el rectángulo de origen recortado y el rectángulo de destino usados para encajar una imagen de referencia en el área imprimible. |
| `core/geometry/UnitConverter.kt` | Convierte longitudes entre milímetros, centímetros y pulgadas. |
| `core/geometry/ViewportTransform.kt` | Convierte coordenadas entre la vista previa en pantalla y el espacio del documento, y calcula distancias en unidades de documento. |

## 9. Feature — Home and projects

| Archivo | Función |
|---|---|
| `feature/main/MainScreen.kt` | Pantalla de inicio en Compose que muestra la introducción de la app, la creación de proyectos, la lista de proyectos, las miniaturas y las acciones de renombrar/eliminar/seleccionar proyectos. |
| `feature/main/MainViewModel.kt` | Mantiene el estado de la interfaz de inicio y coordina el listado, la creación, el renombrado, la selección múltiple y la eliminación de proyectos a través del repositorio. |

## 10. Feature — Editor

| Archivo | Función |
|---|---|
| `feature/editor/EditorScreen.kt` | Interfaz principal del editor. Muestra la vista previa persistente del papel, la barra de herramientas del editor y el panel seleccionado de Papel, Cuadrícula, Imagen, Medición o Exportación. |
| `feature/editor/EditorViewModel.kt` | Posee el estado y las operaciones del editor: carga y guardado de documentos, configuración de papel y cuadrícula, importación y transformaciones de la imagen, medición y orquestación de la exportación. |
| `feature/editor/components/HsvColorPicker.kt` | Selector de color HSV reutilizable y campo de color usados para elegir colores personalizados en el editor. |
| `feature/editor/panels/EditorNumericFields.kt` | Composables reutilizables de entrada numérica para los ajustes del editor. |
| `feature/editor/panels/EditorPanelExport.kt` | Panel del editor para elegir la resolución de exportación y exportar el documento en un formato compatible. |
| `feature/editor/panels/EditorPanelGrid.kt` | Panel del editor para los ajustes de la cuadrícula principal y secundaria y las etiquetas de la cuadrícula. |
| `feature/editor/panels/EditorPanelImage.kt` | Panel del editor para importar y configurar la imagen de referencia, incluida la visibilidad, el bloqueo, la opacidad, la posición, el recorte, la rotación y los filtros. |
| `feature/editor/panels/EditorPanelMeasure.kt` | Panel del editor para activar la medición, mostrar la distancia medida y limpiar los puntos de medición. |
| `feature/editor/panels/EditorPanelPaper.kt` | Panel del editor para el formato del papel, la orientación, las dimensiones, las unidades y los márgenes. |
| `feature/editor/panels/EditorToolbar.kt` | Define los identificadores de herramientas del editor y la barra de herramientas usada para cambiar entre paneles del editor. |
| `feature/editor/panels/GridConfigSections.kt` | Contiene secciones reutilizables de cuadrícula, cuadrícula secundaria, etiquetas, estilo de línea, visibilidad, estado de guardado y control de recorte usadas por el editor. |

## 11. Feature — Paper configuration screen

| Archivo | Función |
|---|---|
| `feature/editor/paper/PaperConfigScreen.kt` | Pantalla Compose independiente para editar el formato del papel, la orientación, las unidades, las dimensiones personalizadas y los márgenes. |
| `feature/editor/paper/PaperConfigViewModel.kt` | Estado de la interfaz y operaciones de configuración del papel para PaperConfigScreen, incluida la conversión de unidades y el dimensionado de papel estándar/personalizado. |

## 12. Feature — Export

| Archivo | Función |
|---|---|
| `feature/editor/export/DocumentRenderer.kt` | Renderiza el papel, la imagen de referencia, la cuadrícula y las etiquetas configurados en un bitmap y escribe la salida en JPG, PNG o PDF de una página. |
| `feature/editor/export/ExportStorage.kt` | Proporciona destinos/flujos de salida para la exportación y gestiona las comprobaciones de permisos de almacenamiento de la plataforma requeridas por el flujo de exportación. |

## 13. Feature — Settings and information

| Archivo | Función |
|---|---|
| `feature/settings/SettingsScreen.kt` | Pantalla de ajustes con navegación a idioma/apariencia, Acerca de e información de licencias de código abierto. |
| `feature/settings/LanguageAppearanceScreen.kt` | Pantalla para seleccionar el idioma de la app y alternar entre los modos de tema disponibles. |
| `feature/settings/AboutScreen.kt` | Muestra la identidad de GridLab, la versión de la app, la información del desarrollador y los enlaces externos al proyecto y a las redes sociales. |
| `feature/settings/OpenSourceLicensesScreen.kt` | Carga el documento localizado de licencias de terceros desde los assets de la app y lo renderiza como Markdown. |

## 14. Unit tests

| Archivo | Función |
|---|---|
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ExportSizeCalculatorTest.kt` | Prueba el dimensionado en píxeles de la exportación, las estimaciones de seguridad de memoria y las entradas de exportación no válidas. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridCalculatorTest.kt` | Prueba los cálculos de diseño de la cuadrícula, los márgenes imprimibles, las orientaciones y las divisiones secundarias. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridLabelFormatterTest.kt` | Prueba las etiquetas numéricas de columna y las etiquetas alfabéticas de fila. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ViewportTransformTest.kt` | Prueba la conversión de coordenadas entre pantalla y documento y los cálculos de distancia en el espacio del documento. |

## 15. Recursos Android (`app/src/main/res/`)

| Archivo | Función |
|---|---|
| `drawable-nodpi/gridlab_banner_goku.jpg` | Ilustración de portada principal mostrada en la pantalla de inicio. |
| `drawable-nodpi/gridlab_icon.png` | Icono PNG de la marca GridLab usado dentro de la interfaz de la app y en la pantalla Acerca de. |
| `drawable/ic_add.xml` | Icono de la acción añadir, usado para crear un proyecto. |
| `drawable/ic_add_photo_alternate_filled.xml` | Icono de añadir imagen para la importación de la imagen de referencia. |
| `drawable/ic_architecture.xml` | Icono de arquitectura/documentación. |
| `drawable/ic_arrow_back.xml` | Icono de navegación hacia atrás. |
| `drawable/ic_article_filled.xml` | Icono de artículo/documento. |
| `drawable/ic_aspect_ratio_filled.xml` | Icono de relación de aspecto o proporción del papel. |
| `drawable/ic_border_outer.xml` | Icono de borde exterior usado en los controles de papel y cuadrícula. |
| `drawable/ic_brush_filled.xml` | Icono de pincel para la identidad visual relacionada con el dibujo. |
| `drawable/ic_chevron_right.xml` | Chevron hacia delante usado en las filas de navegación. |
| `drawable/ic_circle_filled.xml` | Indicador circular relleno. |
| `drawable/ic_close.xml` | Icono de cerrar o descartar. |
| `drawable/ic_code.xml` | Icono de código fuente. |
| `drawable/ic_crop.xml` | Icono de recorte de imagen. |
| `drawable/ic_dark_mode_filled.xml` | Icono de tema oscuro. |
| `drawable/ic_delete_filled.xml` | Icono de la acción eliminar. |
| `drawable/ic_edit_filled.xml` | Icono de la acción editar o renombrar. |
| `drawable/ic_file_export_filled.xml` | Icono de exportación de archivo. |
| `drawable/ic_github.xml` | Icono de la marca GitHub. |
| `drawable/ic_grid_3x3.xml` | Icono de la cuadrícula predefinida de 3 por 3. |
| `drawable/ic_grid_4x4.xml` | Icono de la cuadrícula predefinida de 4 por 4. |
| `drawable/ic_grid_on_outline.xml` | Icono de visibilidad/contorno de la cuadrícula. |
| `drawable/ic_high_quality_filled.xml` | Icono de alta calidad o resolución. |
| `drawable/ic_image_filled.xml` | Icono de imagen de referencia. |
| `drawable/ic_info_filled.xml` | Icono de información. |
| `drawable/ic_instagram.xml` | Icono de la marca Instagram. |
| `drawable/ic_ios_share.xml` | Icono de la acción compartir/exportar. |
| `drawable/ic_label_filled.xml` | Icono de etiquetas de cuadrícula. |
| `drawable/ic_launcher_background.xml` | Capa de fondo del icono adaptativo del lanzador. |
| `drawable/ic_launcher_foreground.xml` | Capa frontal del icono adaptativo del lanzador. |
| `drawable/ic_license.xml` | Icono de licencia de código abierto. |
| `drawable/ic_light_mode_filled.xml` | Icono de tema claro. |
| `drawable/ic_line_weight.xml` | Icono de grosor de línea/trazo. |
| `drawable/ic_lock_filled.xml` | Icono de estado de bloqueo de la imagen de referencia. |
| `drawable/ic_new_releases.xml` | Icono de información de versión/actualización. |
| `drawable/ic_night_mode_filled.xml` | Icono de tema negro/nocturno. |
| `drawable/ic_opacity.xml` | Icono de control de opacidad. |
| `drawable/ic_open_with.xml` | Icono de control de movimiento/posición. |
| `drawable/ic_palette_filled.xml` | Icono de paleta de colores. |
| `drawable/ic_photo_library_filled.xml` | Icono de biblioteca de fotos/importación. |
| `drawable/ic_radio_button_checked.xml` | Icono de opción de radio seleccionada. |
| `drawable/ic_radio_button_unchecked.xml` | Icono de opción de radio no seleccionada. |
| `drawable/ic_rotate_right.xml` | Icono de rotar imagen. |
| `drawable/ic_screen_rotation_alt.xml` | Icono de orientación de pantalla/papel. |
| `drawable/ic_settings_filled.xml` | Icono de navegación a ajustes. |
| `drawable/ic_square_foot_filled.xml` | Icono de medición/dimensión. |
| `drawable/ic_straighten_filled.xml` | Icono de herramienta de regla/medición. |
| `drawable/ic_tiktok.xml` | Icono de la marca TikTok. |
| `drawable/ic_tune.xml` | Icono de controles de ajuste. |
| `drawable/ic_visibility_filled.xml` | Icono de alternancia de visibilidad. |
| `font/ibmplexsans_bold.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_bold_italic.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_medium.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_medium_italic.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_regular.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_regular_italic.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_semibold.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_semibold_italic.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_text.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `font/ibmplexsans_text_italic.otf` | Fuente IBM Plex Sans incluida, usada por la tipografía de la app. |
| `mipmap-anydpi-v26/ic_launcher.xml` | Definición del icono adaptativo del lanzador para Android 8.0 y posteriores. |
| `mipmap-anydpi-v26/ic_launcher_round.xml` | Definición del icono adaptativo del lanzador para Android 8.0 y posteriores. |
| `values-es/strings.xml` | Recursos de cadenas de la interfaz en español. |
| `values-v29/themes.xml` | Recursos de tema de Android para esta configuración de recursos por nivel de API. |
| `values/strings.xml` | Cadenas de la interfaz y arrays de cadenas predeterminados (inglés). |
| `values/themes.xml` | Recursos de tema de Android para esta configuración de recursos por nivel de API. |
| `xml/locales_config.xml` | Recurso de configuración XML de Android para los idiomas compatibles de la aplicación. |

## 16. Assets

| Archivo | Función |
|---|---|
| `app/src/main/assets/licenses/third_party_licenses.md` | Texto de licencias y atribuciones de dependencias de terceros en inglés, mostrado cuando el idioma de la app no es español. |
| `app/src/main/assets/licenses/third_party_licenses_es.md` | Traducción al español del texto de licencias y atribuciones de dependencias de terceros. |

## Documentación

| Archivo | Función |
|---|---|
| `docs/architecture/FILES.md` | Versión en inglés de este índice de responsabilidad de archivos. |
| `docs/architecture/FILES-es.md` | Versión en español de este índice de responsabilidad de archivos. |
| `docs/assets/gridlab_icon.png` | Copia del logotipo PNG de la app referenciado por el README público de GitHub. |