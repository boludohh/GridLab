# File responsibility registry

This document is the source-of-truth inventory of project files, their paths, responsibilities, and relevant relationships. Keep it synchronized with structural changes (see development working rules).

Status values: `implemented` | `in progress` | `planned` | `deprecated` | `removed`.

---

## Root

| Path | Purpose | Status |
|------|---------|--------|
| `settings.gradle.kts` | Project name and module inclusion | implemented |
| `build.gradle.kts` | Root plugins (AGP, Kotlin, Compose, KSP, Hilt, Detekt) | implemented |
| `gradle.properties` | Gradle JVM args, AndroidX, built-in Kotlin disabled, KSP2 flag | implemented |
| `gradle/libs.versions.toml` | Version catalog | implemented |
| `gradle/wrapper/` | Gradle wrapper (9.8.2) | implemented |
| `config/detekt/detekt.yml` | Detekt configuration | implemented |
| `.github/workflows/ci.yml` | CI: compile-only job + quality job (tests + Detekt), APK artifact | implemented |
| `PROJECT_CONTEXT.md` | Durable product/architecture context for sessions | implemented |
| `docs/architecture/FILES.md` | This registry | implemented |
| `LICENSE` | GPL-3.0 | implemented |
| `README.md` | Project introduction | implemented |

---

## Module `:app` — application entry

| Path | Purpose | Status |
|------|---------|--------|
| `app/build.gradle.kts` | App module config, Compose, Hilt, KSP, Detekt, test deps | implemented |
| `app/proguard-rules.pro` | R8/ProGuard rules (empty baseline) | implemented |
| `app/src/main/AndroidManifest.xml` | Application (`GridLabApp`) and MainActivity declaration | implemented |
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | `@HiltAndroidApp` Application class | implemented |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | `@AndroidEntryPoint` entry point; hosts PaperConfigScreen | implemented |
| `app/src/main/kotlin/com/gridlab/app/DemoViewModel.kt` | Temporary Hilt verification ViewModel (kept until replaced) | implemented |
| `app/src/main/res/values/strings.xml` | User-facing strings | implemented |
| `app/src/main/res/values/themes.xml` | Platform theme `Theme.GridLab` | implemented |
| `app/src/main/res/font/` | IBM Plex Sans family | implemented |
| `app/src/main/res/drawable/` | Launcher background / foreground | implemented |
| `app/src/main/res/mipmap-anydpi-v26/` | Adaptive icons | implemented |
| `app/src/test/kotlin/com/gridlab/app/ArchitectureTest.kt` | Konsist architecture boundary tests | implemented |

---

## Module `:app` — theme

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Color.kt` | Semantic color tokens (Light / Dark / Night) | implemented |
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Theme.kt` | `GridLabTheme`, Material3 schemes, `LocalGridLabColors` | implemented |
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Type.kt` | Typography using IBM Plex Sans | implemented |

---

## Module `:app` — core/model (0.4.0)

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/core/model/LengthUnit.kt` | mm / cm / in display units | implemented |
| `app/src/main/kotlin/com/gridlab/app/core/model/PaperOrientation.kt` | Portrait / Landscape | implemented |
| `app/src/main/kotlin/com/gridlab/app/core/model/StandardPaperFormat.kt` | A0–A5 + Custom (ISO 216 mm) | implemented |
| `app/src/main/kotlin/com/gridlab/app/core/model/Paper.kt` | Paper model; printable area from margins | implemented |
| `app/src/main/kotlin/com/gridlab/app/core/model/Document.kt` | Root document; schemaVersion for future Room | implemented |
| `app/src/test/kotlin/com/gridlab/app/core/model/PaperTest.kt` | Paper dimension and printable-area tests | implemented |

---

## Module `:app` — core/geometry (0.4.0)

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/core/geometry/UnitConverter.kt` | Convert between mm and display units | implemented |
| `app/src/test/kotlin/com/gridlab/app/core/geometry/UnitConverterTest.kt` | Unit conversion tests | implemented |

---

## Module `:app` — feature/editor/paper (0.4.0)

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/feature/editor/paper/PaperConfigViewModel.kt` | In-memory paper configuration state | implemented |
| `app/src/main/kotlin/com/gridlab/app/feature/editor/paper/PaperConfigScreen.kt` | Paper configuration UI | implemented |

---

## Planned (not yet present)

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/core/data/...` | Room, repositories (0.5.0) | planned |
| `app/src/main/kotlin/com/gridlab/app/core/geometry/GridCalculator.kt` | Grid cell math (0.6.0) | planned |
| `app/src/main/kotlin/com/gridlab/app/core/render/...` | DocumentRenderer (0.14.0+) | planned |
| Other feature packages | home, canvas, image, proyectos, configuracion | planned |

