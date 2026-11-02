# GridLab — Project Context

## Current version: 0.10.0

- Coil 3.6.3 for reference image display (downsampling)
- Import from system photo picker (no storage permission)
- Private copy under `filesDir/projects/<documentId>/`
- Image transform: offset (mm), scale (relative to fit), opacity, lock, visibility
- Non-destructive crop: normalized edges (0..1) on ReferenceImage; original file untouched
- Gestures: drag to pan, pinch to scale (when unlocked)
- Image drawn under grid; transform relative to initial fit placement
- Distance measurement via ViewportTransform (screen ↔ document mm)
- Room DB v3; domain schemaVersion = 5
- Document.schemaVersion = 5

## Next

0.11.0 — Guides and rulers.
