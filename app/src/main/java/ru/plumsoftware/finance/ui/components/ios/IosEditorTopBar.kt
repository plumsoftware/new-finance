package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.theme.FinanceTheme

/**
 * App bar редакторов в стиле редизайна (§6): «назад» (`ic_back`), заголовок, справа — текстовое действие.
 * [backLabel] оставлен для совместимости: в новом дизайне подпись у стрелки не показывается.
 */
@Composable
fun IosEditorTopBar(
    title: String,
    @Suppress("UNUSED_PARAMETER") backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionEnabled: Boolean = true,
) {
    val c = FinanceTheme.colors
    SubScreenAppBar(
        title = title,
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (actionLabel != null && onAction != null) {
                TextAction(
                    text = actionLabel,
                    onClick = { if (actionEnabled) onAction() },
                    color = if (actionEnabled) c.primary else c.textDisabled,
                )
            }
        },
    )
}
