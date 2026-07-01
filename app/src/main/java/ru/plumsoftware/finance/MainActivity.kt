package ru.plumsoftware.finance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.data.firebase.NotificationDisplayHelper
import ru.plumsoftware.finance.data.firebase.PushNotificationPersistService
import ru.plumsoftware.finance.navigation.AppDeepLinks
import ru.plumsoftware.finance.navigation.parseNotificationDeepLink
import ru.plumsoftware.finance.presentation.FinanceApp
import ru.plumsoftware.finance.presentation.common.BiometricHelper
import ru.plumsoftware.finance.presentation.importdata.OpenDocumentWithInitialUri
import ru.plumsoftware.finance.presentation.security.AppLockViewModel
import ru.plumsoftware.finance.presentation.security.BiometricLockOverlay
import ru.plumsoftware.finance.util.ImportFileHelper
import ru.plumsoftware.finance.util.downloadsInitialUri
import ru.plumsoftware.finance.util.requiresOpenDocument

class MainActivity : FragmentActivity() {

    private val appLockViewModel: AppLockViewModel by viewModel()
    private val pendingImportLocalPath = mutableStateOf<String?>(null)
    private val pendingDeepLinkIntent = mutableStateOf<Intent?>(null)

    private lateinit var importPickerLauncher: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOOXML()

        importPickerLauncher = registerForActivityResult(OpenDocumentWithInitialUri()) { uri ->
            if (uri == null) return@registerForActivityResult
            ImportFileHelper.copyToCacheSync(this, uri)?.let { path ->
                pendingImportLocalPath.value = path
            }
        }

        enableEdgeToEdge()
        setContent {
            AppRoot(
                appLockViewModel = appLockViewModel,
                pendingImportLocalPath = pendingImportLocalPath.value,
                onPendingImportConsumed = { pendingImportLocalPath.value = null },
                pendingDeepLinkIntent = pendingDeepLinkIntent.value,
                onPendingDeepLinkConsumed = { pendingDeepLinkIntent.value = null },
            )
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun initOOXML(){
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        PushNotificationPersistService.enqueueFromTapIfNeeded(this, intent)

        if (AppDeepLinks.isAppDeepLink(intent)) {
            pendingDeepLinkIntent.value = intent
            return
        }

        val fcmData = intent.extras?.keySet()
            ?.filterNot { it.startsWith("google.") || it.startsWith("gcm.") }
            ?.associate { key -> key to intent.extras?.getString(key).orEmpty() }
            .orEmpty()
        parseNotificationDeepLink(fcmData)?.let { uri ->
            pendingDeepLinkIntent.value = Intent(Intent.ACTION_VIEW, uri)
            return
        }

        val uri = extractUriFromIntent(intent) ?: return

        if (uri.requiresOpenDocument()) {
            openPickerAtDownloads()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val tempPath = ImportFileHelper.copyToCacheSync(applicationContext, uri)
            if (tempPath != null) {
                withContext(Dispatchers.Main) {
                    pendingImportLocalPath.value = tempPath
                }
            } else {
                withContext(Dispatchers.Main) {
                    openPickerAtDownloads()
                }
            }
        }
    }

    private fun openPickerAtDownloads() {
        importPickerLauncher.launch(downloadsInitialUri())
    }

    private fun extractUriFromIntent(intent: Intent?): Uri? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> intent.data
        }
    }
}

@Composable
private fun AppRoot(
    appLockViewModel: AppLockViewModel,
    pendingImportLocalPath: String? = null,
    onPendingImportConsumed: () -> Unit = {},
    pendingDeepLinkIntent: Intent? = null,
    onPendingDeepLinkConsumed: () -> Unit = {},
) {
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
        FinanceApp(
            modifier = Modifier.fillMaxSize(),
            pendingImportLocalPath = pendingImportLocalPath,
            onPendingImportConsumed = onPendingImportConsumed,
            pendingDeepLinkIntent = pendingDeepLinkIntent,
            onPendingDeepLinkConsumed = onPendingDeepLinkConsumed,
        )
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
