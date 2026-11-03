# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| `ImageAvailability.kt` | None / Present / Missing / Corrupt | implemented (0.13.0) |

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| `ImageStorage.probe` | On-disk availability check | implemented (0.13.0) |
| `ImageStorage.deleteRelativeFile` | Remove one image file | implemented (0.13.0) |

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| GridConfigViewModel | removeReferenceImage; re-link via import | implemented (0.13.0) |
| GridConfigScreen | Missing/corrupt UI | implemented (0.13.0) |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| DocumentRenderer JPG export | 0.14.0 | planned |
