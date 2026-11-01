# File responsibility registry

Status values: `implemented` | `in progress` | `planned` | `deprecated` | `removed`.

---

## Root

| Path | Purpose | Status |
|------|---------|--------|
| `build.gradle.kts` | Root plugins | implemented |
| `gradle/libs.versions.toml` | Version catalog | implemented |
| `config/detekt/detekt.yml` | Detekt config | implemented |
| `.github/workflows/ci.yml` | CI compile + quality | implemented |
| `PROJECT_CONTEXT.md` | Session context | implemented |
| `docs/architecture/FILES.md` | This registry | implemented |

---

## Application

| Path | Purpose | Status |
|------|---------|--------|
| `app/build.gradle.kts` | Module deps (Room, Hilt, serialization) | implemented |
| `app/src/main/kotlin/com/gridlab/app/MainActivity.kt` | Hosts GridConfigScreen | implemented |
| `app/src/main/kotlin/com/gridlab/app/GridLabApp.kt` | `@HiltAndroidApp` | implemented |
| `app/src/main/res/values/strings.xml` | User-facing strings | implemented |
| `app/src/test/kotlin/com/gridlab/app/ArchitectureTest.kt` | Konsist | implemented |

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| `LengthUnit.kt`, `PaperOrientation.kt`, `StandardPaperFormat.kt`, `Paper.kt` | Paper model | implemented |
| `Grid.kt`, `GridLineStyle.kt` | Main grid model | implemented |
| `Document.kt` | Document (paper + grid + schemaVersion) | implemented |

---

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| `UnitConverter.kt` | Unit conversion | implemented |
| `GridCalculator.kt` | Grid line positions in mm | implemented |
| tests for both | Unit tests | implemented |

---

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| `DocumentEntity.kt` | Room entity (paperJson + gridJson) | implemented |
| `AppDatabase.kt` | DB version 2 | implemented |
| `ProjectDao.kt` | DAO | implemented |
| `ProjectRepository(.kt|Impl.kt)` | Load/save document | implemented |
| `DatabaseModule.kt` | Hilt + Migration 1→2 | implemented |
| `RepositoryModule.kt` | Hilt bind | implemented |

---

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| `paper/PaperConfigScreen.kt`, `PaperConfigViewModel.kt` | Paper config UI | implemented |
| `grid/GridConfigScreen.kt`, `GridConfigViewModel.kt` | Grid config + Canvas preview | implemented |

---

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Image import (Coil), transform, crop | 0.7.0+ | planned |
| Projects list CRUD | later | planned |
