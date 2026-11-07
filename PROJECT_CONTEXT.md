# GridLab — Project Context

## Current version: 0.24.0 — Custom HSV color picker

- Shared HSV color picker in `core/ui/HsvColorPicker.kt` (square + hue bar + dialog field)
- User-selectable colors for:
  - Paper fill
  - Primary grid lines
  - Secondary grid lines
  - Grid labels
  - Measure overlay (session tool color)
- Paper.colorArgb and Grid.labelColorArgb persisted with the document (serialization defaults keep old docs loading)
- Preview and DocumentRenderer respect paper / label / grid colors
- Editor shell (toolbar + bottom panels) from 0.23 unchanged in structure

## Previous (0.23.0)

- Settings + open-source licenses Markdown
- multiplatform-markdown-renderer 0.46.0 + m3
- CI: compile debug+release artifacts; quality gated on compile with continue-on-error

## Deferred

Storage info, Detekt cleanup, values-es for UI strings, full semantic-color migration in editor chrome.
