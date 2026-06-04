package ru.plumsoftware.finance.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.about),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.SpacingL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
        }
    }
}
