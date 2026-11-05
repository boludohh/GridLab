# File responsibility registry

---

## Status: 0.23.0

| Path | Purpose |
|------|---------|
| `feature/settings/SettingsScreen.kt` | Settings root screen; section list with Open-source licenses entry |
| `feature/settings/OpenSourceLicensesScreen.kt` | Renders third-party licenses Markdown from assets; tuned table denseness (cell width, padding, typography) for readable long coordinates |
| `MainActivity.kt` | Root navigation coordinator (Home / Settings / OpenSourceLicenses / Editor) |
| `feature/main/MainScreen.kt` | Home; settings icon opens Settings |
| `res/drawable/ic_arrow_back.xml` | Back affordance for Settings / licenses screens |
| `assets/licenses/third_party_licenses.md` | English third-party license attribution (primary) |
| `assets/licenses/third_party_licenses_es.md` | Spanish third-party license attribution |
| `ExportFormat.kt` | Jpeg / Png / Pdf |
| `DocumentRenderer.exportPng` | PNG write |
| `DocumentRenderer.exportPdf` | PdfDocument single page |
| GridConfigViewModel.exportDocument | Shared export path |

## Deferred

| Versions | Features |
|----------|----------|
| 0.24 | Storage info / app data |