# GridLab — File Index

This document describes the responsibility of each project file based on the repository's current structure. All paths are relative to the repository root (`GridLab/`).

## Contents

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

| File | Purpose |
|---|---|
| `build.gradle.kts` | Declares the root-level Gradle plugins and shared build tasks for the project. |
| `settings.gradle.kts` | Configures plugin and dependency repositories and includes the single Android application module, :app. |
| `gradle.properties` | Contains project-wide Gradle, Android, Kotlin, and JVM build settings. |
| `gradle/libs.versions.toml` | Central version catalog for Gradle plugins and application dependencies. |
| `gradle/wrapper/gradle-wrapper.properties` | Pins the Gradle distribution used by the project wrapper. |
| `gradle/wrapper/gradle-wrapper.jar` | Gradle Wrapper executable JAR, used by the wrapper scripts. |
| `gradlew / gradlew.bat` | Wrapper launch scripts for Unix-like systems and Windows. |
| `.gitignore` | Lists generated files, local signing material, and other files that Git must not track. |
| `LICENSE` | The GNU General Public License version 3.0 terms for GridLab's own source code. |
| `README.md` | Public project overview, feature summary, platform requirements, contribution guidance, and documentation links. |

## 2. Continuous integration

| File | Purpose |
|---|---|
| `.github/workflows/ci.yml` | GitHub Actions workflow that runs the unit tests and Android lint, then builds debug and release APKs and uploads the APKs and verification reports. |

## 3. Android app module

| File | Purpose |
|---|---|
| `app/build.gradle.kts` | Configures the Android application, SDK levels, version, Java/Kotlin targets, release signing, code shrinking, and dependencies. |
| `app/proguard-rules.pro` | Project-specific ProGuard/R8 rules used by the optimized release build. |
| `app/keystore/.gitkeep` | Keeps the otherwise-empty local keystore directory in version control; the actual signing key is not stored in the repository. |
| `app/src/main/AndroidManifest.xml` | Declares the application class, launcher activity, app icons, theme, and Android application metadata. |

## 4. Application entry point

| File | Purpose |
|---|---|
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | Application class annotated with @HiltAndroidApp; initializes the root Hilt dependency graph. |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | Single activity and Compose navigation host. Connects Home, the editor, Settings, language/appearance, About, and open-source license screens; also coordinates theme changes. |

## 5. Core — data and persistence

| File | Purpose |
|---|---|
| `core/data/database/AppDatabase.kt` | Room database definition and access point for the project DAO. |
| `core/data/database/DocumentEntity.kt` | Room entity that stores a project document and its serialized data. |
| `core/data/database/ProjectDao.kt` | Room DAO queries for retrieving, inserting/updating, and deleting project records. |
| `core/data/di/DatabaseModule.kt` | Hilt providers for the Room database and ProjectDao. |
| `core/data/di/RepositoryModule.kt` | Binds ProjectRepository to its ProjectRepositoryImpl implementation in the application-wide Hilt component. |
| `core/data/repository/ProjectRepository.kt` | Repository contract for reading, listing, saving, and deleting project documents. |
| `core/data/repository/ProjectRepositoryImpl.kt` | Implements the project repository using the DAO and maps between Room entities and domain documents. |
| `core/data/storage/ImageStorage.kt` | Copies imported image URIs into app-private project storage, resolves image paths, checks image availability, and removes image files or project directories. |

## 6. Core — design system and preferences

| File | Purpose |
|---|---|
| `core/designsystem/AppLocale.kt` | Applies the selected application language through Android's locale configuration. |
| `core/designsystem/EdgeToEdge.kt` | Centralizes edge-to-edge window and system-bar configuration for supported Android versions. |
| `core/designsystem/theme/Color.kt` | Defines the raw color values used by the application's light, dark, and black theme palettes. |
| `core/designsystem/theme/Theme.kt` | Defines theme modes, semantic color roles, the theme CompositionLocal, and the root GridLabTheme composable. |
| `core/designsystem/theme/ThemeModeExt.kt` | Provides helpers to cycle through theme modes and obtain the matching theme icon resource. |
| `core/designsystem/theme/ThemeReveal.kt` | Implements the circular theme-transition reveal, its animation state, overlay, and activity bitmap capture helper. |
| `core/designsystem/theme/Type.kt` | Builds the app's Material 3 typography from the bundled IBM Plex Sans font files. |
| `core/preferences/AppLanguage.kt` | Defines the supported app language values and maps language tags to those values. |
| `core/preferences/ThemePreferencesRepository.kt` | Persists and exposes the selected theme mode and language through Preferences DataStore flows. |
| `core/preferences/di/PreferencesModule.kt` | Provides the Preferences DataStore instance through Hilt. |

## 7. Core — domain models

| File | Purpose |
|---|---|
| `core/model/Document.kt` | Project-level domain model containing its identity, name, paper, grid, reference image, and schema version. |
| `core/model/ExportFormat.kt` | Enumerates the supported export formats. |
| `core/model/Grid.kt` | Stores the main and secondary grid configuration and label visibility. |
| `core/model/GridLineStyle.kt` | Stores a grid line's color, physical stroke width, and opacity. |
| `core/model/ImageAvailability.kt` | Represents whether a referenced image is absent, present, missing, or unreadable. |
| `core/model/ImageFilter.kt` | Enumerates the non-destructive reference-image filters. |
| `core/model/LengthUnit.kt` | Enumerates the measurement units used to display and edit paper dimensions. |
| `core/model/Paper.kt` | Stores paper dimensions, standard/custom format, orientation, margins, and display unit; dimensions are represented internally in millimeters. |
| `core/model/PaperOrientation.kt` | Enumerates portrait and landscape paper orientations. |
| `core/model/ReferenceImage.kt` | Stores the reference image path and its transform, visibility, lock, opacity, rotation, crop, and filter settings. |
| `core/model/StandardPaperFormat.kt` | Defines the supported standard paper formats and the custom-size option. |

## 8. Core — geometry and calculations

| File | Purpose |
|---|---|
| `core/geometry/ExportSizeCalculator.kt` | Converts physical paper dimensions and DPI into export pixel dimensions and estimates whether an export is safe for the device memory budget. |
| `core/geometry/GridCalculator.kt` | Calculates printable-area dimensions, grid cell sizes, and main/secondary grid line positions. |
| `core/geometry/GridLabelFormatter.kt` | Formats grid column labels as numbers and row labels as spreadsheet-style letters. |
| `core/geometry/ImageFilterMatrices.kt` | Provides color matrices for the supported reference-image filters. |
| `core/geometry/ImageTransformCalculator.kt` | Contains immutable calculations for moving, scaling, rotating, cropping, locking, showing, and filtering a reference image. |
| `core/geometry/ReferenceImagePlacement.kt` | Calculates the cropped source rectangle and the destination rectangle used to fit a reference image into the printable area. |
| `core/geometry/UnitConverter.kt` | Converts lengths between millimeters, centimeters, and inches. |
| `core/geometry/ViewportTransform.kt` | Converts coordinates between the screen preview and document space, and calculates distances in document units. |

## 9. Feature — Home and projects

| File | Purpose |
|---|---|
| `feature/main/MainScreen.kt` | Compose Home screen showing the app introduction, project creation, project list, thumbnails, and project rename/delete/selection actions. |
| `feature/main/MainViewModel.kt` | Holds Home UI state and coordinates project listing, creation, renaming, multi-selection, and deletion through the repository. |

## 10. Feature — Editor

| File | Purpose |
|---|---|
| `feature/editor/EditorScreen.kt` | Main editor UI. Displays the persistent paper preview, editor toolbar, and the selected Paper, Grid, Image, Measure, or Export panel. |
| `feature/editor/EditorViewModel.kt` | Owns editor state and operations: document loading and saving, paper/grid configuration, image import and transforms, measurement, and export orchestration. |
| `feature/editor/components/HsvColorPicker.kt` | Reusable HSV color picker and color field used to select custom colors in the editor. |
| `feature/editor/panels/EditorNumericFields.kt` | Reusable numeric input composables for editor settings. |
| `feature/editor/panels/EditorPanelExport.kt` | Editor panel for choosing export resolution and exporting the document in a supported format. |
| `feature/editor/panels/EditorPanelGrid.kt` | Editor panel for main/secondary grid settings and grid labels. |
| `feature/editor/panels/EditorPanelImage.kt` | Editor panel for importing and configuring the reference image, including visibility, lock, opacity, position, crop, rotation, and filters. |
| `feature/editor/panels/EditorPanelMeasure.kt` | Editor panel for enabling measurement, showing the measured distance, and clearing measurement points. |
| `feature/editor/panels/EditorPanelPaper.kt` | Editor panel for paper format, orientation, dimensions, units, and margins. |
| `feature/editor/panels/EditorToolbar.kt` | Defines the editor tool identifiers and the toolbar used to switch between editor panels. |
| `feature/editor/panels/GridConfigSections.kt` | Contains reusable grid, secondary-grid, label, line-style, visibility, save-state, and crop control sections used by the editor. |

## 11. Feature — Paper configuration screen

| File | Purpose |
|---|---|
| `feature/editor/paper/PaperConfigScreen.kt` | Standalone Compose screen for editing paper format, orientation, units, custom dimensions, and margins. |
| `feature/editor/paper/PaperConfigViewModel.kt` | UI state and paper-setting operations for PaperConfigScreen, including unit conversion and standard/custom paper sizing. |

## 12. Feature — Export

| File | Purpose |
|---|---|
| `feature/editor/export/DocumentRenderer.kt` | Renders the configured paper, reference image, grid, and labels into a bitmap and writes JPG, PNG, or single-page PDF output. |
| `feature/editor/export/ExportStorage.kt` | Provides export output destinations/streams and handles the platform storage-permission checks required by the export flow. |

## 13. Feature — Settings and information

| File | Purpose |
|---|---|
| `feature/settings/SettingsScreen.kt` | Settings screen with navigation to language/appearance, About, and open-source license information. |
| `feature/settings/LanguageAppearanceScreen.kt` | Screen for selecting the app language and cycling between the available theme modes. |
| `feature/settings/AboutScreen.kt` | Displays GridLab identity, app version, developer information, and external project/social links. |
| `feature/settings/OpenSourceLicensesScreen.kt` | Loads the localized third-party license document from app assets and renders it as Markdown. |

## 14. Unit tests

| File | Purpose |
|---|---|
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ExportSizeCalculatorTest.kt` | Tests export pixel sizing, memory-safety estimates, and invalid export inputs. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridCalculatorTest.kt` | Tests grid layout calculations, printable margins, orientations, and secondary divisions. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/GridLabelFormatterTest.kt` | Tests numeric column labels and alphabetic row labels. |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/ViewportTransformTest.kt` | Tests screen/document coordinate conversion and document-space distance calculations. |

## 15. Android resources (`app/src/main/res/`)

| File | Purpose |
|---|---|
| `drawable-nodpi/gridlab_banner_goku.jpg` | Home hero/banner artwork displayed on the Home screen. |
| `drawable-nodpi/gridlab_icon.png` | GridLab brand icon PNG used inside the app UI and on the About screen. |
| `drawable/ic_add.xml` | Add action icon, used for creating a project. |
| `drawable/ic_add_photo_alternate_filled.xml` | Image-add icon for reference-image import. |
| `drawable/ic_architecture.xml` | Architecture/documentation icon. |
| `drawable/ic_arrow_back.xml` | Back navigation icon. |
| `drawable/ic_article_filled.xml` | Article/document icon. |
| `drawable/ic_aspect_ratio_filled.xml` | Aspect-ratio or paper-proportion icon. |
| `drawable/ic_border_outer.xml` | Outer-border icon used for paper/grid-related controls. |
| `drawable/ic_brush_filled.xml` | Brush icon for drawing-related visual identity. |
| `drawable/ic_chevron_right.xml` | Forward chevron used in navigation rows. |
| `drawable/ic_circle_filled.xml` | Filled circular indicator. |
| `drawable/ic_close.xml` | Close or dismiss icon. |
| `drawable/ic_code.xml` | Source-code icon. |
| `drawable/ic_crop.xml` | Image crop icon. |
| `drawable/ic_dark_mode_filled.xml` | Dark theme icon. |
| `drawable/ic_delete_filled.xml` | Delete action icon. |
| `drawable/ic_edit_filled.xml` | Edit or rename action icon. |
| `drawable/ic_file_export_filled.xml` | File export icon. |
| `drawable/ic_github.xml` | GitHub brand icon. |
| `drawable/ic_grid_3x3.xml` | 3-by-3 grid preset icon. |
| `drawable/ic_grid_4x4.xml` | 4-by-4 grid preset icon. |
| `drawable/ic_grid_on_outline.xml` | Grid visibility/outline icon. |
| `drawable/ic_high_quality_filled.xml` | High-quality or resolution icon. |
| `drawable/ic_image_filled.xml` | Reference-image icon. |
| `drawable/ic_info_filled.xml` | Information icon. |
| `drawable/ic_instagram.xml` | Instagram brand icon. |
| `drawable/ic_ios_share.xml` | Share/export action icon. |
| `drawable/ic_label_filled.xml` | Grid-label icon. |
| `drawable/ic_launcher_background.xml` | Background layer for the adaptive launcher icon. |
| `drawable/ic_launcher_foreground.xml` | Foreground layer for the adaptive launcher icon. |
| `drawable/ic_license.xml` | Open-source license icon. |
| `drawable/ic_light_mode_filled.xml` | Light theme icon. |
| `drawable/ic_line_weight.xml` | Line thickness/stroke-width icon. |
| `drawable/ic_lock_filled.xml` | Lock state icon for the reference image. |
| `drawable/ic_new_releases.xml` | Release/update information icon. |
| `drawable/ic_night_mode_filled.xml` | Black/night theme icon. |
| `drawable/ic_opacity.xml` | Opacity control icon. |
| `drawable/ic_open_with.xml` | Move/position control icon. |
| `drawable/ic_palette_filled.xml` | Color palette icon. |
| `drawable/ic_photo_library_filled.xml` | Photo library/import icon. |
| `drawable/ic_radio_button_checked.xml` | Selected radio option icon. |
| `drawable/ic_radio_button_unchecked.xml` | Unselected radio option icon. |
| `drawable/ic_rotate_right.xml` | Rotate image icon. |
| `drawable/ic_screen_rotation_alt.xml` | Screen/paper orientation icon. |
| `drawable/ic_settings_filled.xml` | Settings navigation icon. |
| `drawable/ic_square_foot_filled.xml` | Measurement/dimension icon. |
| `drawable/ic_straighten_filled.xml` | Ruler/measurement tool icon. |
| `drawable/ic_tiktok.xml` | TikTok brand icon. |
| `drawable/ic_tune.xml` | Adjustment controls icon. |
| `drawable/ic_visibility_filled.xml` | Visibility toggle icon. |
| `font/ibmplexsans_bold.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_bold_italic.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_medium.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_medium_italic.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_regular.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_regular_italic.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_semibold.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_semibold_italic.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_text.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `font/ibmplexsans_text_italic.otf` | Bundled IBM Plex Sans font face used by the app typography. |
| `mipmap-anydpi-v26/ic_launcher.xml` | Adaptive launcher icon definition for Android 8.0 and later. |
| `mipmap-anydpi-v26/ic_launcher_round.xml` | Adaptive launcher icon definition for Android 8.0 and later. |
| `values-es/strings.xml` | Spanish UI string resources. |
| `values-v29/themes.xml` | Android theme resources for this API-level resource configuration. |
| `values/strings.xml` | Default (English) UI strings and string arrays. |
| `values/themes.xml` | Android theme resources for this API-level resource configuration. |
| `xml/locales_config.xml` | Android XML configuration resource for supported application locales. |

## 16. Assets

| File | Purpose |
|---|---|
| `app/src/main/assets/licenses/third_party_licenses.md` | Third-party dependency license and attribution text in English, displayed when the app language is not Spanish. |
| `app/src/main/assets/licenses/third_party_licenses_es.md` | Spanish translation of the third-party dependency license and attribution text. |

## Documentation

| File | Purpose |
|---|---|
| `docs/architecture/FILES.md` | English version of this file-responsibility index. |
| `docs/architecture/FILES-es.md` | Spanish version of this file-responsibility index. |
| `docs/assets/gridlab_icon.png` | Copy of the PNG app logo referenced by the public GitHub README. |