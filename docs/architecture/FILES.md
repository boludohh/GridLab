# File responsibility registry

---

## core/model

| Path | Purpose | Status |
|------|---------|--------|
| Document, Paper, Grid, ReferenceImage | Domain model | implemented |

## core/geometry

| Path | Purpose | Status |
|------|---------|--------|
| UnitConverter, GridCalculator, ViewportTransform, ImageTransformCalculator | Pure geometry | implemented |

## core/data

| Path | Purpose | Status |
|------|---------|--------|
| ProjectDao | get/list/upsert/delete | implemented (0.11.0) |
| ProjectRepository | CRUD + safe delete | implemented (0.11.0) |
| ImageStorage | copy + deleteProjectDir | implemented (0.11.0) |

## feature/projects

| Path | Purpose | Status |
|------|---------|--------|
| ProjectListScreen / ViewModel | List, create, rename, delete | implemented (0.11.0) |

## feature/editor

| Path | Purpose | Status |
|------|---------|--------|
| grid/* | Editor for one document | implemented |
| paper/* | Paper config | implemented |

## Planned

| Path | Purpose | Status |
|------|---------|--------|
| Autosave | 0.12.0 | planned |
