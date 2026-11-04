# File responsibility registry

---

## Status: 0.16.0 (end-to-end usable)

Core flow is complete for daily use on device:

`projects → editor (grid + image + measure) → autosave → export JPG`

## core/

| Area | Status |
|------|--------|
| model, geometry, data, storage, render | implemented |

## feature/

| Area | Status |
|------|--------|
| projects | list CRUD |
| editor/grid | full editor + export |
| editor/paper | exists; not linked in main nav yet |

## Planned (Stage 6, batched)

| Versions | Features |
|----------|----------|
| 0.17 + 0.18 | Rotation UI, secondary grid |
| 0.19 + 0.20 | Labels, image filters (if in scope) |
| 0.21 + 0.22 | PNG + PDF export |
| 0.23 + 0.24 | Settings, storage info |
