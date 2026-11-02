# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| Paper, Grid, Document | Document model | implemented |
| `ReferenceImage.kt` | Reference image path + transform (offset, scale, opacity, lock, visibility, rotationDegrees prepared) | implemented (0.8.0) |

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| UnitConverter, GridCalculator | Pure geometry | implemented |
| `ImageTransformCalculator.kt` | Pan, scale, opacity, lock, visibility helpers | implemented (0.8.0) |

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
| grid/* | Grid config + image import + transform UI + Canvas/Coil preview with gestures | implemented (0.8.0) |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Distance measurement | 0.9.0 | planned |
| Non-destructive crop | 0.10.0 | planned |
