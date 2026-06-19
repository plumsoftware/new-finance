package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun InitialBalanceDialog(
    title: String,
    message: String,
    currencyCode: String,
    amountDigits: String,
    isSaving: Boolean,
    dismissLabel: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dialogShape = RoundedCornerShape(Dimens.cornerRadiusCard)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.SpacingL,
                    vertical = Dimens.SpacingL,
                ),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = dialogShape,
                color = colors.surface,
                tonalElevation = 0.dp,
                shadowElevation = Dimens.elevationDialog,
            ) {
                Column {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = Dimens.SpacingXl - 4.dp,
                            vertical = Dimens.SpacingXl - 4.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = title,
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
                                .padding(top = Dimens.SpacingXs),
                        )
                        Text(
                            text = MoneyFormat.formatEntryDisplay(
                                context,
                                amountDigits,
                                currencyCode
                            ),
                            style = typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(top = Dimens.SpacingM),
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = Dimens.SpacingS)
                                .clip(shape = RoundedCornerShape(Dimens.cornerRadiusChip))
                        ) {
                            FinanceNumPad(
                                onDigit = onDigit,
                                onBackspace = onBackspace
                            )
                        }
                    }
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IosTextButton(
                            text = dismissLabel,
                            onClick = onDismiss,
                            enabled = !isSaving,
                            color = colors.onSurfaceVariant,
                            style = typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                vertical = Dimens.RadiusS + 4.dp,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(44.dp)
                                .background(colors.outline.copy(alpha = 0.5f)),
                        )
                        IosTextButton(
                            text = stringResource(R.string.save),
                            onClick = onSave,
                            enabled = !isSaving,
                            color = colors.primary,
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                vertical = Dimens.RadiusS + 4.dp,
                            ),
                        )
                    }
                }
            }
        }
    }
}
