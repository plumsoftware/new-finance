package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.plumsoftware.finance.ui.components.ios.IosNavigationTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolHeader(
    title: String,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpacingM)
            .padding(vertical = Dimens.SpacingXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IosNavigationTextButton(
            text = stringResource(R.string.back),
            onClick = onBack,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = title,
            style = typography.bodyLarge,
            color = colors.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}
