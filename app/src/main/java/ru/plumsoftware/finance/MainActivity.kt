package ru.plumsoftware.finance

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.FinanceApp
import ru.plumsoftware.finance.presentation.common.BiometricHelper
import ru.plumsoftware.finance.presentation.security.AppLockViewModel
import ru.plumsoftware.finance.presentation.security.BiometricLockOverlay

class MainActivity : FragmentActivity() {

    private val appLockViewModel: AppLockViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot(appLockViewModel = appLockViewModel)
        }
    }
}

@Composable
private fun AppRoot(appLockViewModel: AppLockViewModel) {
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val activity = LocalContext.current as FragmentActivity
    val biometricHelper = remember(activity) { BiometricHelper(activity) }
    val isLocked = appLockViewModel.isLocked
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, settings.biometricEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> appLockViewModel.onPause()
                Lifecycle.Event.ON_RESUME -> {
                    if (settings.biometricEnabled && appLockViewModel.appWasInBackground) {
                        appLockViewModel.lock()
                        biometricHelper.authenticate(
                            onSuccess = { appLockViewModel.unlock() },
                            onError = { /* stay locked */ },
                        )
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FinanceApp(modifier = Modifier.fillMaxSize())
        if (settings.biometricEnabled && isLocked) {
            BiometricLockOverlay(
                onUnlockClick = {
                    biometricHelper.authenticate(
                        onSuccess = { appLockViewModel.unlock() },
                        onError = { /* ignore */ },
                    )
                },
            )
        }
    }
}
