# GridLab — Project Context

## Current version: 0.8.0

- Coil 3.6.3 for reference image display (downsampling)
- Import from system photo picker (no storage permission)
- Private copy under `filesDir/projects/<documentId>/`
- Image transform: offset (mm), scale (relative to fit), opacity, lock, visibility
- Gestures: drag to pan, pinch to scale (when unlocked)
- Image drawn under grid; transform relative to initial fit placement
- Room DB v3 + migration 2→3 (`referenceImageJson`); domain schemaVersion = 4
- Document.schemaVersion = 4

## Next

0.9.0 — Distance measurement (ViewportTransform).
