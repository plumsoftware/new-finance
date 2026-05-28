package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun IosAlertDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f),
            shape = shapes.small,
            color = colors.surface,
            tonalElevation = 0.dp,
            shadowElevation = Dimens.elevationDialog,
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(
                        horizontal = Dimens.paddingLarge - 4.dp,
                        vertical = Dimens.paddingLarge - 4.dp,
                    ),
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = message,
                            style = typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.paddingSmall),
                        )
                    } ?: Text(
                        text = message,
                        style = typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                HorizontalDivider(color = colors.outline.copy(alpha = 0.5f))
                IosTextButton(
                    text = stringResource(R.string.ok),
                    onClick = onDismiss,
                    style = typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = Dimens.spacingRow + 4.dp,
                    ),
                )
            }
        }
    }
}
