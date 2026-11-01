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
- Borders between `core/` and `feature/` enforced with **Konsist**
- Dependency injection: **Hilt**
- Code quality: **Detekt** (+ formatting plugin)
- Persistence: Room planned in 0.5.0; models already carry `schemaVersion`

---

## Current public version plan (high level)

- **0.1.0**: project skeleton, theme, CI compile-only
- **0.2.0**: Hilt + KSP end-to-end
- **0.3.0**: Konsist + Detekt + quality CI job
- **0.4.0** (current): Document + Paper model, UnitConverter, paper config UI (in-memory), unit tests
- **0.5.0**: Room persistence for Document/Paper
- **0.6.0**: Main grid
- Later: image, projects, export, remaining features → 1.0.0

---

## Toolchain (confirmed)

| Component | Version / value |
|-----------|-----------------|
| Gradle | 9.8.2 |
| AGP | 9.4.1 |
| Kotlin | 2.4.21 |
| KSP | 2.3.12 |
| Hilt | 2.60.1 |
| Konsist | 0.18.1 |
| Detekt | 1.23.8 |
| Compose BOM | 2026.09.00 |
| compileSdk / targetSdk | 37 |
| minSdk | 27 |
| JVM target | 21 |
| License | GPL-3.0 |

---

## Paper model notes (0.4.0)

- Internal unit: **millimeters**
- Formats: A0–A5 (ISO 216) + Custom
- Orientation swaps width/height for standard formats
- Margins stored in mm; printable area = size − margins (floored at 0)
- `displayUnit` only affects UI input/output via `UnitConverter`
- No persistence yet — state lives in `PaperConfigViewModel`

---

## Next step after 0.4.0

0.5.0 — Room: `AppDatabase`, document entity, DAO, `ProjectRepository`, JSON serialization of Paper with `schemaVersion`.

