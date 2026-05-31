package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import ru.plumsoftware.finance.ui.theme.Dimens

/**
 * Верхняя панель в стиле экрана редактора категории:
 * слева — «назад» с подписью, по центру заголовок [bodyLarge], справа — опциональное действие.
 */
@Composable
fun IosEditorTopBar(
    title: String,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionEnabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpacingM)
            .padding(vertical = Dimens.SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IosNavigationTextButton(
            text = backLabel,
            onClick = onBack,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = title,
            style = typography.bodyLarge,
            color = colors.onSurface,
        )
        if (actionLabel != null && onAction != null) {
            IosTextButton(
                text = actionLabel,
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                color = if (actionEnabled) colors.primary else colors.outlineVariant,
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
