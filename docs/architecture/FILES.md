# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| Paper, Grid, Document | Document model | implemented |
| `ReferenceImage.kt` | Reference image path + transform | implemented (0.8.0) |

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| UnitConverter, GridCalculator | Pure geometry | implemented |
| `ImageTransformCalculator.kt` | Pan, scale, opacity, lock, visibility helpers | implemented (0.8.0) |
| `ViewportTransform.kt` | Screen ↔ document mm mapping; distance | implemented (0.9.0) |

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| Room v3 (`referenceImageJson`) | Persistence | implemented |
| `ImageStorage.kt` | Copy gallery URI → filesDir/projects/<id>/ | implemented |
| ProjectRepository | Load/save document | implemented |

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| paper/* | Paper config | implemented |
| grid/* | Grid config + image + transform + distance measurement | implemented (0.9.0) |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Non-destructive crop | 0.10.0 | planned |
