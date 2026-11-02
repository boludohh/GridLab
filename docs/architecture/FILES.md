# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| Paper, Grid, Document | Document model | implemented |
| `ReferenceImage.kt` | Path, transform, non-destructive crop | implemented (0.10.0) |

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| UnitConverter, GridCalculator | Pure geometry | implemented |
| `ImageTransformCalculator.kt` | Pan, scale, opacity, lock, crop helpers | implemented (0.10.0) |
| `ViewportTransform.kt` | Screen ↔ document mm; distance | implemented (0.9.0) |

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| Room v3 (`referenceImageJson`) | Persistence | implemented |
| `ImageStorage.kt` | Copy gallery URI → filesDir | implemented |
| ProjectRepository | Load/save document | implemented |

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| paper/* | Paper config | implemented |
| grid/GridConfigScreen.kt | Preview, image, measure | implemented |
| grid/GridConfigSections.kt | Grid + crop UI sections | implemented (0.10.0) |
| grid/GridConfigViewModel.kt | State + actions | implemented |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Guides / rulers | 0.11.0 | planned |
