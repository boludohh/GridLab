package com.gridlab.app.feature.editor.grid

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale

/**
 * Shared numeric input fields for the editor panels.
 *
 * The visible text lives in local state while the field is focused, so the user
 * can type intermediate values ("", "28.", ".5") and delete freely without the
 * cursor jumping. The parsed value is emitted to the caller only when the text
 * is a valid number. The text is normalized from the model value when the field
 * loses focus, or when the model changes while the field is not focused.
 */

/** Parses user text leniently: trims blanks and accepts a comma as decimal separator. */
internal fun parseDecimalInput(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()

/** Formats a double for display: whole numbers without decimals, otherwise two decimals. */
internal fun formatDecimalForDisplay(value: Double): String =
    if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }

/** Decimal input field (paper sizes, margins). Accepts values like 28.5 or 28,5. */
@Composable
internal fun EditorDecimalField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorNumericField(
        label = label,
        displayValue = formatDecimalForDisplay(value),
        keyboardType = KeyboardType.Decimal,
        onTextChanged = { text -> parseDecimalInput(text)?.let(onValueChange) },
        modifier = modifier,
    )
}

/** Integer input field (grid rows/columns). */
@Composable
internal fun EditorIntegerField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorNumericField(
        label = label,
        displayValue = value.toString(),
        keyboardType = KeyboardType.Number,
        onTextChanged = { text -> text.trim().toIntOrNull()?.let(onValueChange) },
        modifier = modifier,
    )
}

@Composable
private fun EditorNumericField(
    label: String,
    displayValue: String,
    keyboardType: KeyboardType,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(displayValue) }
    var focused by remember { mutableStateOf(false) }

    // Reflect external model changes only while the user is not editing this field.
    LaunchedEffect(displayValue) {
        if (!focused) text = displayValue
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onTextChanged(newText)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.onFocusChanged { state ->
            focused = state.isFocused
            if (!state.isFocused) {
                text = displayValue
            }
        },
    )
}