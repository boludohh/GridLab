# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| Paper, Grid, Document | Document model | implemented |
| `ReferenceImage.kt` | Reference image path (private copy) | implemented |

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| UnitConverter, GridCalculator | Pure geometry | implemented |

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
| grid/* | Grid config + image import + Canvas/Coil preview | implemented |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Image transform (move/scale) | 0.8.0 | planned |

