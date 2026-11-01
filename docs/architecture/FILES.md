# File responsibility registry

This document is the source-of-truth inventory of project files, their paths, responsibilities, and relevant relationships. Keep it synchronized with structural changes (see development working rules).

Status values: `implemented` | `in progress` | `planned` | `deprecated` | `removed`.

---

## Root

| Path | Purpose | Status |
|------|---------|--------|
| `settings.gradle.kts` | Project name and module inclusion | implemented |
| `build.gradle.kts` | Root plugins (AGP, Kotlin, Compose, KSP, Hilt) | implemented |
| `gradle.properties` | Gradle JVM args, AndroidX, built-in Kotlin disabled, KSP2 flag | implemented |
| `gradle/libs.versions.toml` | Version catalog | implemented |
| `gradle/wrapper/` | Gradle wrapper (9.8.2) | implemented |
| `.github/workflows/ci.yml` | CI: compile-only job with Gradle cache and keystore setup | implemented |
| `PROJECT_CONTEXT.md` | Durable product/architecture context for sessions | implemented |
| `docs/architecture/FILES.md` | This registry | implemented |
| `LICENSE` | GPL-3.0 | implemented |
| `README.md` | Project introduction | implemented |

---

## Module `:app`

| Path | Purpose | Status |
|------|---------|--------|
| `app/build.gradle.kts` | App module config, Compose, Hilt, KSP, dependencies | implemented |
| `app/proguard-rules.pro` | R8/ProGuard rules (empty baseline) | implemented |
| `app/src/main/AndroidManifest.xml` | Application (`GridLabApp`) and MainActivity declaration | implemented |
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | `@HiltAndroidApp` Application class | implemented |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | `@AndroidEntryPoint` entry point; shows injected ViewModel message | implemented |
| `app/src/main/kotlin/com/gridlab/app/DemoViewModel.kt` | Trivial `@HiltViewModel` + `MessageProvider` to verify injection chain | implemented |
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Color.kt` | Semantic color tokens (Light / Dark / Night) | implemented |
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Theme.kt` | `GridLabTheme`, Material3 schemes, `LocalGridLabColors` | implemented |
| `app/src/main/kotlin/com/gridlab/app/ui/theme/Type.kt` | Typography using IBM Plex Sans | implemented |
| `app/src/main/res/values/strings.xml` | User-facing strings | implemented |
| `app/src/main/res/values/themes.xml` | Platform theme `Theme.GridLab` | implemented |
| `app/src/main/res/font/` | IBM Plex Sans family | implemented |
| `app/src/main/res/drawable/` | Launcher background / foreground | implemented |
| `app/src/main/res/mipmap-anydpi-v26/` | Adaptive icons | implemented |

---

## Planned (not yet present)

| Path | Purpose | Status |
|------|---------|--------|
| `app/src/main/kotlin/com/gridlab/app/core/...` | Core packages (model, geometry, render, data, designsystem, common, navigation) | planned |
| `app/src/main/kotlin/com/gridlab/app/feature/...` | Feature packages (home, editor, proyectos, configuracion) | planned |
| Room, Coil, Konsist, Detekt | Later stages (0.3.0+) | planned |
