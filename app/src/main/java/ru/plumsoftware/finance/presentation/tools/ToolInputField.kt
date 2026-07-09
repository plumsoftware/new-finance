package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun ToolInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Number,
    isError: Boolean = false,
    errorText: String? = null,
    isIntegerOnly: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.SpacingS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        // Очищаем и нормализуем строку (запятые в точки, убираем дубли точек) перед отправкой в ViewModel
                        val normalized = newValue.replace(",", ".")
                        val filtered = if (isIntegerOnly) {
                            normalized.filter { it.isDigit() }
                        } else {
                            val firstDotIndex = normalized.indexOf('.')
                            normalized.filterIndexed { index, char ->
                                char.isDigit() || (char == '.' && index == firstDotIndex)
                            }
                        }
                        onValueChange(filtered)
                    },
                    textStyle = typography.bodyLarge.copy(
                        color = if (isError) colors.error else colors.onSurface,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    // Применяем визуальную трансформацию (курсор теперь не прыгает)
                    visualTransformation = ThousandsSeparatorTransformation(isIntegerOnly),
                    modifier = Modifier.width(130.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = typography.bodyLarge,
                                    color = colors.outlineVariant,
                                    textAlign = TextAlign.End
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (suffix.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                    Text(
                        text = suffix,
                        style = typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.width(36.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }
        }
        if (isError && errorText != null) {
            Text(
                text = errorText,
                style = typography.labelSmall,
                color = colors.error,
                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
            )
        }
    }
}

class ThousandsSeparatorTransformation(private val isIntegerOnly: Boolean = false) :
    VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = formatInputDigits(originalText, isIntegerOnly)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, originalText.length)
                val originalPrefix = originalText.substring(0, safeOffset)
                val formattedPrefix = formatInputDigits(originalPrefix, isIntegerOnly)
                return formattedPrefix.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                val formattedPrefix = formatted.substring(0, safeOffset)
                val originalPrefixLength = formattedPrefix.replace(" ", "").length
                return originalPrefixLength
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}