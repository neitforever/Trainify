package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.utils.ClearFocusOnKeyboardDismiss
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun IntMetricInputDialog(
    showDialog: Boolean,
    title: String,
    label: String,
    initialValue: Int,
    minValue: Int,
    maxValue: Int,
    stepValue: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    if (!showDialog) return

    val safeMin = minOf(minValue, maxValue)
    val safeMax = maxOf(minValue, maxValue)
    val safeStep = stepValue.coerceAtLeast(1)
    val initialClamped = remember(initialValue, safeMin, safeMax) {
        initialValue.coerceIn(safeMin, safeMax)
    }

    var inputText by remember(initialClamped) { mutableStateOf(initialClamped.toString()) }
    val parsedValue = inputText.trim().toIntOrNull()
    val isInputBlank = inputText.isBlank()
    val isFormatError = !isInputBlank && parsedValue == null
    val isRangeError = parsedValue != null && parsedValue !in safeMin..safeMax
    val isFieldError = isFormatError || isRangeError
    val canSave = parsedValue != null && parsedValue in safeMin..safeMax
    val fallbackValue = 0.coerceIn(safeMin, safeMax)
    val currentValue = parsedValue?.coerceIn(safeMin, safeMax)
    val stepBaseValue = currentValue ?: fallbackValue

    NumericMetricInputDialogLayout(
        title = title,
        label = label,
        inputText = inputText,
        displayText = (currentValue ?: fallbackValue).toString(),
        minText = safeMin.toString(),
        maxText = safeMax.toString(),
        stepText = safeStep.toString(),
        isFieldError = isFieldError,
        isDisplayError = isFieldError,
        canSave = canSave,
        keyboardType = KeyboardType.Number,
        canDecrease = stepBaseValue > safeMin,
        canIncrease = stepBaseValue < safeMax,
        onInputFocusGained = {
            inputText = ""
        },
        onInputChange = { newText ->
            val sanitized = sanitizeIntInput(newText, safeMin).take(6)
            val value = sanitized.toIntOrNull()
            if (sanitized.isBlank() || sanitized == "-" || (value != null && value in safeMin..safeMax)) {
                inputText = sanitized
            }
        },
        onDecrease = {
            inputText = (stepBaseValue - safeStep).coerceIn(safeMin, safeMax).toString()
        },
        onIncrease = {
            inputText = (stepBaseValue + safeStep).coerceIn(safeMin, safeMax).toString()
        },
        onDismiss = onDismiss,
        onSave = {
            val value = inputText.trim().toIntOrNull()
            if (value != null && value in safeMin..safeMax) {
                onSave(value)
            }
        }
    )
}

@Composable
fun FloatMetricInputDialog(
    showDialog: Boolean,
    title: String,
    label: String,
    initialValue: Float,
    minValue: Float,
    maxValue: Float,
    stepValue: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    if (!showDialog) return

    val safeMin = minOf(minValue, maxValue)
    val safeMax = maxOf(minValue, maxValue)
    val safeStep = if (stepValue <= 0f) 1f else stepValue
    val displayDecimals = remember(safeStep, safeMin, safeMax) {
        maxOf(decimalPlaces(safeStep), decimalPlaces(safeMin), decimalPlaces(safeMax)).coerceIn(0, 2)
    }
    val inputDecimals = remember(title, label, displayDecimals) {
        if (isInclineMetric(title, label)) displayDecimals else maxOf(displayDecimals, 1)
    }
    val currentDisplayDecimals = if (isInclineMetric(title, label)) displayDecimals else inputDecimals
    val scale = remember(inputDecimals) { decimalScale(inputDecimals) }
    val initialRounded = remember(initialValue, safeMin, safeMax, scale) {
        roundToScale(initialValue.coerceIn(safeMin, safeMax), scale)
    }

    var inputText by remember(initialRounded, currentDisplayDecimals) {
        mutableStateOf(formatMetricFloat(initialRounded, currentDisplayDecimals))
    }
    val parsedValue = parseMetricFloat(inputText)
    val isInputBlank = inputText.isBlank()
    val isFormatError = !isInputBlank && parsedValue == null
    val isRangeError = parsedValue != null && (parsedValue < safeMin || parsedValue > safeMax)
    val isFieldError = isFormatError || isRangeError
    val canSave = parsedValue != null && parsedValue >= safeMin && parsedValue <= safeMax
    val fallbackValue = 0f.coerceIn(safeMin, safeMax)
    val currentValue = parsedValue?.coerceIn(safeMin, safeMax)
    val stepBaseValue = currentValue ?: fallbackValue
    val minText = formatMetricFloat(safeMin, displayDecimals)
    val maxText = formatMetricFloat(safeMax, displayDecimals)
    val stepText = formatMetricFloat(safeStep, displayDecimals)

    NumericMetricInputDialogLayout(
        title = title,
        label = label,
        inputText = inputText,
        displayText = formatMetricFloat(currentValue ?: fallbackValue, currentDisplayDecimals),
        minText = minText,
        maxText = maxText,
        stepText = stepText,
        isFieldError = isFieldError,
        isDisplayError = isFieldError,
        canSave = canSave,
        keyboardType = if (inputDecimals == 0) KeyboardType.Number else KeyboardType.Decimal,
        canDecrease = stepBaseValue > safeMin + 0.0001f,
        canIncrease = stepBaseValue < safeMax - 0.0001f,
        onInputFocusGained = {
            inputText = ""
        },
        onInputChange = { newText ->
            val sanitized = sanitizeDecimalInput(newText, inputDecimals, safeMin).take(8)
            val value = parseMetricFloat(sanitized)
            if (sanitized.isBlank() || sanitized == "-" || (value != null && value >= safeMin && value <= safeMax)) {
                inputText = sanitized
            }
        },
        onDecrease = {
            inputText = formatMetricFloat(
                roundToScale((stepBaseValue - safeStep).coerceIn(safeMin, safeMax), scale),
                currentDisplayDecimals
            )
        },
        onIncrease = {
            inputText = formatMetricFloat(
                roundToScale((stepBaseValue + safeStep).coerceIn(safeMin, safeMax), scale),
                currentDisplayDecimals
            )
        },
        onDismiss = onDismiss,
        onSave = {
            val value = parseMetricFloat(inputText)
            if (value != null && value >= safeMin && value <= safeMax) {
                onSave(roundToScale(value.coerceIn(safeMin, safeMax), scale))
            }
        }
    )
}

@Composable
private fun NumericMetricInputDialogLayout(
    title: String,
    label: String,
    inputText: String,
    displayText: String,
    minText: String,
    maxText: String,
    stepText: String,
    isFieldError: Boolean,
    isDisplayError: Boolean,
    canSave: Boolean,
    keyboardType: KeyboardType,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onInputFocusGained: () -> Unit,
    onInputChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var wasInputFocused by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ClearFocusOnKeyboardDismiss {
                    wasInputFocused = false
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp, letterSpacing = 0.sp),
                            color = if (isDisplayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricStepButton(
                        enabled = canDecrease,
                        isIncrease = false,
                        onClick = onDecrease
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && !wasInputFocused) {
                                    onInputFocusGained()
                                }
                                wasInputFocused = focusState.isFocused
                            },
                        singleLine = true,
                        isError = isFieldError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                            }
                        ),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.Transparent,
                            errorCursorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    MetricStepButton(
                        enabled = canIncrease,
                        isIncrease = true,
                        onClick = onIncrease
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        MetricBoundText(stringResource(R.string.metric_min_label), minText)
                        MetricBoundText(stringResource(R.string.metric_step_label), stepText)
                        MetricBoundText(stringResource(R.string.metric_max_label), maxText)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = onSave
            ) {
                Text(
                    text = stringResource(R.string.save),
                    color = if (!canSave) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun MetricStepButton(
    enabled: Boolean,
    isIncrease: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (enabled) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
        },
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        ),
        modifier = Modifier.size(50.dp)
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isIncrease) R.drawable.ic_plus else R.drawable.ic_minus
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
private fun MetricBoundText(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun sanitizeIntInput(value: String, min: Int): String {
    val builder = StringBuilder()
    value.forEachIndexed { index, char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '-' && index == 0 && min < 0 -> builder.append(char)
        }
    }
    return builder.toString()
}

private fun sanitizeDecimalInput(value: String, decimals: Int, min: Float): String {
    val normalized = value.replace(',', '.')
    val builder = StringBuilder()
    var hasDot = false
    var fractionLength = 0

    normalized.forEachIndexed { index, char ->
        when {
            char.isDigit() -> {
                if (hasDot) {
                    if (fractionLength < decimals.coerceAtLeast(0)) {
                        builder.append(char)
                        fractionLength++
                    }
                } else {
                    builder.append(char)
                }
            }
            char == '-' && index == 0 && min < 0f -> builder.append(char)
            char == '.' && decimals > 0 && !hasDot -> {
                builder.append(char)
                hasDot = true
            }
        }
    }

    return builder.toString()
}

private fun isInclineMetric(title: String, label: String): Boolean {
    val text = "$title $label".lowercase(Locale.getDefault())
    return text.contains("incline") || text.contains("inc") || text.contains("наклон") ||
        text.contains("нахіл") || text.contains("нах.")
}

private fun parseMetricFloat(value: String): Float? {
    return value.trim().replace(',', '.').toFloatOrNull()
}

private fun snapIntValue(value: Int, min: Int, max: Int, step: Int): Int {
    val clamped = value.coerceIn(min, max)
    val offset = clamped - min
    val snapped = min + (offset.toFloat() / step).roundToInt() * step
    return snapped.coerceIn(min, max)
}

private fun snapFloatValue(value: Float, min: Float, max: Float, step: Float, scale: Float): Float {
    val clamped = value.coerceIn(min, max)
    val offset = clamped - min
    val snapped = min + (offset / step).roundToInt() * step
    return roundToScale(snapped.coerceIn(min, max), scale)
}

private fun formatMetricFloat(value: Float, decimals: Int): String {
    return if (decimals <= 0 || abs(value % 1f) < 0.0001f) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.${decimals}f", value).trimEnd('0').trimEnd('.')
    }
}

private fun decimalPlaces(value: Float): Int {
    val text = String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
    val dotIndex = text.indexOf('.')
    return if (dotIndex == -1) 0 else text.length - dotIndex - 1
}

private fun decimalScale(decimals: Int): Float {
    var scale = 1f
    repeat(decimals.coerceAtLeast(0)) { scale *= 10f }
    return scale
}

private fun roundToScale(value: Float, scale: Float): Float {
    return (value * scale).roundToInt() / scale
}
