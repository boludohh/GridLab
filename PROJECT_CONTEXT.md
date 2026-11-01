# GridLab — Project Context

This document preserves durable product, architecture, and convention context between chatbot sessions. Keep it accurate and concise.

---

## What is GridLab

Android app for artists and students who use the grid-drawing technique to reproduce reference images on paper.

**Central product decision:** paper, grid, and reference image are independent of each other. Moving or scaling the image does not affect paper or grid; the app does not auto-crop the image to fit the paper. This differentiates GridLab from GridArt Drawing (market reference).

Target test device: Samsung Galaxy A15 4G (4 GB RAM). Development workflow has no desktop PC or emulator — files are edited in MT Manager, Git runs in Termux, builds run exclusively on GitHub Actions, APK is installed on the physical device.

---

## Architecture (summary)

- Single Gradle module `:app`
- Pattern: MVVM + unidirectional data flow (UDF)
- Pure-Kotlin geometry layer (`core/geometry`) for testable calculations
- Rendering layer (`core/render`) that draws paper → image → grid via `android.graphics.Canvas` (shared by screen, export bitmap, PDF)
- Package layout:
  - `core/` — designsystem, common, model, geometry, render, data (Room + repositories + Hilt modules), navigation
  - `feature/` — home, editor (canvas, grid, image, paper), proyectos, configuracion
- Borders between `core/` and `feature/` (and between features) are enforced later with Konsist
- Dependency injection: Hilt (from 0.2.0)
- Persistence: Room with JSON serialization of Paper/Grid/ReferenceImage + explicit `schemaVersion`
- Images: private copy per project under `filesDir/projects/<id>/`, original resolution kept, non-destructive crop relative to original, missing/corrupt image handling

---

## Current public version plan (high level)

- **0.1.0** (this release): project skeleton, Material 3 theme with semantic colors (Light / Dark / Night), IBM Plex Sans, compile-only CI, base docs
- **0.2.0**: Hilt + KSP end-to-end
- **0.3.0**: Konsist + Detekt + full CI job
- **0.4.0–0.5.0**: Document + Paper model and Room persistence
- **0.6.0**: Main grid
- **0.7.0–0.10.0**: Reference image (import, transform, measurement, non-destructive crop)
- **0.11.0–0.13.0**: Project CRUD, autosave, missing-image robustness
- **0.14.0–0.15.0**: Export (JPG + memory-safe DPI)
- **0.16.0**: First end-to-end usable build
- Later 0.x: secondary grid, rotation, labels, PNG/PDF, settings, storage info
- **1.0.0**: when the original specification is complete and stable

Semantic Versioning follows the actual diff against the last release (not file count). Pre-1.0 is unstable by SemVer convention.

---

## Toolchain (confirmed for this project)

| Component | Version / value |
|-----------|-----------------|
| Gradle | 9.8.2 (wrapper) |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |
| compileSdk | 37 |
| targetSdk | 37 |
| minSdk | 27 |
| JVM target | 17 |
| License | GPL-3.0 |

`android.builtInKotlin=false` and `ksp.useKSP2=true` are set in `gradle.properties`.

---

## Theme & design system (0.1.0)

Three themes with a full semantic token set (no traditional primary/pink/violet):

- **Light (Claro)**
- **Dark (Oscuro)**
- **Night (Full Negro)**

Tokens cover layers (bg → surface → e1–e4), borders, icons, text, actions, states, controls, and colored status (error / warning / success). Exposed via `GridLabTheme` + `LocalGridLabColors` / `GridLabTheme.colors`. Material3 `ColorScheme` is mapped from the same tokens for compatibility with Material components.

Typography uses the bundled IBM Plex Sans family.

---

## Development constraints (must not be forgotten)

- User has no PC: deliver **complete files** only (NEW or REPLACEMENT), never diffs or snippets with “rest unchanged”.
- Builds are expensive (edit → push → CI → install → test). Prefer small, always-compilable phases.
- All developer documentation, KDoc, comments, and commit messages are in **English**.
- User-facing strings live in `strings.xml`.
- Before modifying code, the current file contents must be known; do not invent paths or APIs.
- New dependencies require explicit approval, license check, and R8 review.

---

## Next step after 0.1.0

0.2.0 — Hilt + KSP end-to-end (GridLabApp, @AndroidEntryPoint, a trivial injected ViewModel visible on screen).
