# GridLab — Project Context

## Current version: 0.23.0 — Settings + open-source licenses

- Settings screen reachable from the home settings icon
- Open-source licenses block opens a Markdown view of `assets/licenses/third_party_licenses.md` (or `_es.md` when locale is Spanish)
- multiplatform-markdown-renderer 0.46.0 + `-m3` for Material 3 themed Markdown
- Manual root navigation in MainActivity: Home → Settings → OpenSourceLicenses; Editor unchanged
- Semantic colors used for surfaces, text, icons, and Markdown colors

- MainScreen + MainViewModel (home)
- Banner, rotating EN taglines (2.5s), full-width New project with icon
- Project list CRUD on home
- Editor + JPG/PNG/PDF export unchanged
- Edge-to-edge (API 27–37) via configureEdgeToEdge + system bar icon appearance by theme mode

## Deferred

0.24 storage, Detekt cleanup, values-es for UI strings.
