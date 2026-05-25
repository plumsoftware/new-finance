package ru.plumsoftware.finance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.home.HomePlaceholderScreen
import ru.plumsoftware.finance.presentation.onboarding.OnboardingScreen

@Composable
fun FinanceApp(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository = koinInject(),
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        if (!settings.onboardingCompleted) {
            OnboardingScreen()
        } else {
            HomePlaceholderScreen()
        }
    }
}
