package com.gridlab.app.feature.editor.paper

import androidx.lifecycle.ViewModel
import com.gridlab.app.core.geometry.UnitConverter
import com.gridlab.app.core.model.LengthUnit
import com.gridlab.app.core.model.Paper
import com.gridlab.app.core.model.PaperOrientation
import com.gridlab.app.core.model.StandardPaperFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PaperConfigUiState(
    val paper: Paper = Paper.defaultA4(),
)

@HiltViewModel
class PaperConfigViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PaperConfigUiState())
    val uiState: StateFlow<PaperConfigUiState> = _uiState.asStateFlow()

    fun setFormat(format: StandardPaperFormat) {
        _uiState.update { state ->
            val current = state.paper
            val updated = if (format == StandardPaperFormat.Custom) {
                current.copy(format = StandardPaperFormat.Custom)
            } else {
                Paper.fromStandard(
                    format = format,
                    orientation = current.orientation,
                    displayUnit = current.displayUnit,
                    marginLeftMm = current.marginLeftMm,
                    marginTopMm = current.marginTopMm,
                    marginRightMm = current.marginRightMm,
                    marginBottomMm = current.marginBottomMm,
                )
            }
            state.copy(paper = updated)
        }
    }

    fun setOrientation(orientation: PaperOrientation) {
        _uiState.update { state ->
            val current = state.paper
            val updated = if (current.format == StandardPaperFormat.Custom) {
                current.copy(
                    orientation = orientation,
                    widthMm = current.heightMm,
                    heightMm = current.widthMm,
                )
            } else {
                Paper.fromStandard(
                    format = current.format,
                    orientation = orientation,
                    displayUnit = current.displayUnit,
                    marginLeftMm = current.marginLeftMm,
                    marginTopMm = current.marginTopMm,
                    marginRightMm = current.marginRightMm,
                    marginBottomMm = current.marginBottomMm,
                )
            }
            state.copy(paper = updated)
        }
    }

    fun setDisplayUnit(unit: LengthUnit) {
        _uiState.update { state ->
            state.copy(paper = state.paper.copy(displayUnit = unit))
        }
    }

    fun setCustomWidth(valueInDisplayUnit: Double) {
        _uiState.update { state ->
            val mm = UnitConverter.toMm(valueInDisplayUnit, state.paper.displayUnit)
            state.copy(
                paper = state.paper.copy(
                    format = StandardPaperFormat.Custom,
                    widthMm = mm.coerceAtLeast(1.0),
                ),
            )
        }
    }

    fun setCustomHeight(valueInDisplayUnit: Double) {
        _uiState.update { state ->
            val mm = UnitConverter.toMm(valueInDisplayUnit, state.paper.displayUnit)
            state.copy(
                paper = state.paper.copy(
                    format = StandardPaperFormat.Custom,
                    heightMm = mm.coerceAtLeast(1.0),
                ),
            )
        }
    }

    fun setMarginLeft(valueInDisplayUnit: Double) {
        updateMargin { copy(marginLeftMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginTop(valueInDisplayUnit: Double) {
        updateMargin { copy(marginTopMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginRight(valueInDisplayUnit: Double) {
        updateMargin { copy(marginRightMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    fun setMarginBottom(valueInDisplayUnit: Double) {
        updateMargin { copy(marginBottomMm = UnitConverter.toMm(valueInDisplayUnit, displayUnit).coerceAtLeast(0.0)) }
    }

    private fun updateMargin(transform: Paper.() -> Paper) {
        _uiState.update { state ->
            state.copy(paper = state.paper.transform())
        }
    }
}

