<div align="center">

[🇬🇧 English](README.md) | [🇪🇸 Español](README-es.md)

<img src="docs/assets/gridlab_icon.png" alt="GridLab app icon" width="128" />

# GridLab

**Turn reference images into grid-based drawings.**

An open-source Android tool for artists, students, and anyone who wants to reproduce an image on paper using the grid drawing technique.

[Source code](https://github.com/boludohh/GridLab) · [Releases](https://github.com/boludohh/GridLab/releases) · [Report an issue](https://github.com/boludohh/GridLab/issues)

</div>

---

## What is GridLab?

GridLab helps you transfer a reference image onto physical paper with the **grid drawing technique**. It brings the paper layout, drawing grid, and reference image together in one workspace, so you can configure them independently and see how they fit before drawing.

Create a project, choose the paper you plan to use, set the grid divisions, position your reference image, and export the result as a guide for your drawing process.

GridLab is designed to keep the process flexible: you are not restricted to a fixed paper size, a square grid, or a single way of arranging your reference.

## Features

### Paper setup
- Choose ISO paper formats from A0 to A5, or define a custom paper size.
- Switch between portrait and landscape orientation.
- Set paper margins and check the resulting printable area.
- Work in millimeters, centimeters, or inches.

### Grid configuration
- Set the number of rows and columns independently.
- Adjust line thickness, opacity, and color.
- Show or hide the grid.
- Add secondary subdivisions inside each main cell.
- Display column numbers and row labels to make locating cells easier.

### Reference image
- Import an image from your device.
- Move and scale the image to position it within the printable area.
- Adjust opacity, rotation, and visibility.
- Lock the image to avoid accidental changes.
- Apply non-destructive grayscale or high-contrast filters.
- Crop the reference without modifying the original image file.

### Measurement
- Place two points on the paper preview to measure the distance between them.
- View the measurement using the selected paper unit.

### Projects and export
- Keep separate projects for different drawings.
- Reopen and continue editing saved project configurations.
- Export the composed drawing guide as **JPG, PNG, or PDF**.
- Select an export resolution (DPI); GridLab checks the device's available memory and indicates a safe resolution.

## How it works

1. **Create a project.** Each project stores its own paper, grid, and reference-image settings.
2. **Set up your paper.** Select a standard format or enter custom dimensions, orientation, units, and margins.
3. **Configure the grid.** Choose rows and columns, then customize its appearance and optional subdivisions or labels.
4. **Add your reference.** Import an image and adjust its position, scale, rotation, opacity, crop, or filter.
5. **Use the preview.** Review the relationship between the paper, reference image, and grid as you make adjustments.
6. **Export your guide.** Save a JPG, PNG, or PDF and use it as a reference while drawing on paper.

## Availability

- **Platform:** Android
- **Minimum Android version:** Android 8.1 (API 27)
- **Languages:** English and Spanish
- **License:** GNU General Public License v3.0 (GPL-3.0)

GridLab is an Android application built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Hilt.

## Project status

This is the **first public release** of GridLab. The initial release focuses on providing the core grid-drawing workflow: project management, paper and grid configuration, reference-image adjustments, measurement, and export.

The interface and the overall experience may evolve in future releases. Bug reports, reproducible issues, and practical suggestions are welcome through the [GitHub issue tracker](https://github.com/boludohh/GridLab/issues).

## Documentation

- [File index (English)](docs/architecture/FILES.md)
- [Índice de archivos (Español)](docs/architecture/FILES-es.md)

## Contributing

Contributions and issue reports are welcome. Before proposing a change, please review the project structure and the file responsibilities documented above. When reporting a problem, include the Android version, device model when relevant, steps to reproduce the issue, and the expected versus actual behavior.

## License

GridLab is distributed under the [GNU General Public License v3.0](LICENSE).

Third-party libraries and their licenses are listed inside the application under **Settings → Open-source licenses** and in the corresponding files under `app/src/main/assets/licenses/`.
