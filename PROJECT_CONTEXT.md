# GridLab — Project Context

## What is GridLab

Android app for grid-drawing technique. Paper, grid, and reference image are independent.

Device: Samsung Galaxy A15. Workflow: MT Manager + Termux + GitHub Actions.

## Architecture

- Single module, MVVM + UDF, Hilt, Konsist, Detekt
- `core/model`, `core/geometry`, `core/data` (Room)
- `feature/editor/paper`, `feature/editor/grid`

## Current version: 0.6.0

- Main grid: `Grid` + `GridLineStyle`, `GridCalculator` (pure Kotlin + tests)
- Canvas preview of paper + grid lines
- Config UI: rows, columns, visibility, stroke (mm), opacity, color
- Persistence: `gridJson` column (Room DB v2, migration 1→2)
- Document.schemaVersion = 2

## Next

0.7.0 — Import and show reference image (Coil).
