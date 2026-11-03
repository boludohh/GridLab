# File responsibility registry

---

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| ProjectRepository | CRUD + safe delete | implemented |
| ImageStorage | copy + deleteProjectDir | implemented |

## feature/projects

| Path | Purpose | Status |
|------|---------|--------|
| ProjectListScreen / ViewModel | Project list CRUD | implemented |

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| GridConfigViewModel | Edits + debounced autosave + flushPendingSave | implemented (0.12.0) |
| GridConfigScreen | ON_STOP / Back flush | implemented (0.12.0) |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Image missing/corrupt handling | 0.13.0 | planned |
