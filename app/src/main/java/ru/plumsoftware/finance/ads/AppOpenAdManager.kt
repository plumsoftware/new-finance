package ru.plumsoftware.finance.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import java.lang.ref.WeakReference

/**
 * Настройки показа рекламы при открытии приложения.
 */
data class AppOpenAdConfig(
    /** Не показывать рекламу новым пользователям: сколько запусков приложения нужно до первого показа. */
    val minLaunchesBeforeFirstAd: Int = 0,
    /** Минимальный интервал между двумя показами. */
    val cooldownMs: Long = 0L,
    /** Показывать при возврате, только если приложение было в фоне не менее этого времени. */
    val minBackgroundMs: Long = 3_000L,
    /** Загруженное объявление старше этого возраста считается устаревшим и выбрасывается. */
    val maxAdAgeMs: Long = 3 * 60 * 60_000L,
    /** Пауза после неудачной загрузки, прежде чем пробовать снова (чтобы не спамить запросами). */
    val loadRetryDelayMs: Long = 60_000L,
)

/**
 * Менеджер рекламы при открытии приложения (App Open Ad, Yandex Mobile Ads SDK 8).
 *
 * Что делает:
 *  - предзагружает объявление;
 *  - при горячем старте (возврат из фона) сам показывает рекламу через [ProcessLifecycleOwner];
 *  - при холодном старте показывает рекламу по явному вызову [showOnLaunch] (с таймаутом,
 *    чтобы не задерживать запуск, если реклама не успела загрузиться);
 *  - соблюдает частоту показов, «защиту новичков» и минимальное время в фоне.
 *
 * Все вызовы должны быть из главного потока.
 */
class AppOpenAdManager(
    private val application: Application,
    private val adUnitId: String,
    private val config: AppOpenAdConfig = AppOpenAdConfig(),
    /** Дополнительное условие, например «не показывать, пока приложение заблокировано биометрией». */
    private val shouldShow: () -> Boolean = { true },
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private companion object {
        const val TAG = "YandexAds"
        const val PREFS = "app_open_ad_prefs"
        const val KEY_LAUNCH_COUNT = "launch_count"
        const val ADS_PACKAGE_PREFIX = "com.yandex.mobile.ads"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val loader = AppOpenAdLoader(application)

    private var loadedAd: AppOpenAd? = null
    private var loadedAtMs = 0L
    private var isLoading = false
    private var isShowing = false
    private var lastShownAtMs = 0L
    private var lastLoadFailedAtMs = 0L

    /** Момент ухода приложения в фон; null — приложение ещё ни разу не уходило в фон (холодный старт). */
    private var backgroundedAtMs: Long? = null
    private var suppressNextForeground = false
    private var registered = false

    private var currentActivity: WeakReference<Activity>? = null
    private var launchWaiter: LaunchWaiter? = null

    private class LaunchWaiter(
        val activity: WeakReference<Activity>,
        val onFinished: () -> Unit,
        val timeoutRunnable: Runnable,
    )

    // ---------------------------------------------------------------------------------------
    // Публичный API
    // ---------------------------------------------------------------------------------------

    /**
     * Подписывается на жизненный цикл приложения. Вызывать один раз из [Application.onCreate].
     * Также увеличивает счётчик запусков (нужен для «защиты новичков»).
     */
    fun register() {
        if (registered) return
        registered = true
        incrementLaunchCount()
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "AppOpen register: launchCount=${launchCount()}")
    }

    /**
     * Предзагрузка объявления. Безопасно вызывать многократно.
     * Рекомендуется вызвать в колбэке `YandexAds.initialize { ... }`.
     */
    fun preload() {
        if (adUnitId.isBlank()) {
            Log.d(TAG, "AppOpen preload: skipped, adUnitId is blank")
            return
        }
        if (isLoading) {
            Log.d(TAG, "AppOpen preload: skipped, already loading")
            return
        }
        if (isLoadedAdFresh()) {
            Log.d(TAG, "AppOpen preload: skipped, fresh ad already loaded")
            return
        }
        val sinceFail = System.currentTimeMillis() - lastLoadFailedAtMs
        if (lastLoadFailedAtMs != 0L && sinceFail < config.loadRetryDelayMs) {
            Log.d(TAG, "AppOpen preload: skipped, retry delay (passed=${sinceFail}ms)")
            return
        }
        clearLoaded()
        isLoading = true
        Log.d(TAG, "AppOpen preload: start loading, adUnitId=$adUnitId")
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : AppOpenAdLoadListener {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    Log.d(TAG, "AppOpen onAdLoaded")
                    isLoading = false
                    lastLoadFailedAtMs = 0L
                    loadedAd = appOpenAd
                    loadedAtMs = System.currentTimeMillis()
                    onLoadedForLaunchWaiter()
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    // По рекомендации Яндекса НЕ перезапрашиваем рекламу сразу из этого колбэка.
                    Log.e(
                        TAG,
                        "AppOpen onAdFailedToLoad: code=${adRequestError.code}, description=${adRequestError.description}",
                    )
                    isLoading = false
                    lastLoadFailedAtMs = System.currentTimeMillis()
                    finishLaunchWaiter()
                }
            },
        )
    }

    /**
     * Показ при холодном старте. Вызывать из экрана запуска (после splash), один раз за запуск процесса:
     * ```
     * if (savedInstanceState == null) appOpenAdManager.showOnLaunch(this) { /* продолжить */ }
     * ```
     * Если реклама не готова — ждёт её не дольше [timeoutMs], затем вызывает [onFinished] и
     * приложение продолжает работу без рекламы (объявление при этом догрузится для горячего старта).
     * [onFinished] вызывается ровно один раз в любом случае.
     */
    fun showOnLaunch(activity: Activity, timeoutMs: Long = 5_000L, onFinished: () -> Unit = {}) {
        if (!isEligible(backgroundDurationMs = null)) {
            preload()
            onFinished()
            return
        }
        val ad = takeFreshLoadedAd()
        if (ad != null) {
            show(activity, ad, onFinished)
            return
        }
        if (launchWaiter != null) {
            onFinished()
            return
        }
        val timeout = Runnable {
            Log.d(TAG, "AppOpen showOnLaunch: timeout, continue without ad")
            finishLaunchWaiter()
        }
        launchWaiter = LaunchWaiter(WeakReference(activity), onFinished, timeout)
        mainHandler.postDelayed(timeout, timeoutMs)
        preload()
        // Если загрузка не стартовала (например, пауза после ошибки) — не ждём таймаут
        if (!isLoading && loadedAd == null) finishLaunchWaiter()
    }

    /**
     * Пропустить показ при ближайшем возврате в приложение. Нужно, когда пользователь ушёл из приложения
     * из-за клика по другой рекламе (браузер, Google Play) — иначе при возврате сразу откроется App Open.
     * Пример: вызвать из `onAdClicked()` в [InterstitialAdManager].
     */
    fun suppressNextForeground() {
        suppressNextForeground = true
    }

    /** Освободить ресурсы (обычно не нужно — менеджер живёт, пока живёт процесс). */
    fun destroy() {
        Log.d(TAG, "AppOpen destroy")
        mainHandler.removeCallbacksAndMessages(null)
        launchWaiter = null
        clearLoaded()
        if (registered) {
            application.unregisterActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
            registered = false
        }
    }

    // ---------------------------------------------------------------------------------------
    // Жизненный цикл процесса: горячий старт
    // ---------------------------------------------------------------------------------------

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMs = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val backgroundedAt = backgroundedAtMs
        if (backgroundedAt == null) {
            // Холодный старт: реклама показывается через showOnLaunch(), здесь только готовим объявление.
            preload()
            return
        }
        val backgroundDuration = System.currentTimeMillis() - backgroundedAt
        backgroundedAtMs = null
        // post: к этому моменту onActivityResumed уже отработает, и currentActivity будет актуален
        mainHandler.post { showOnForeground(backgroundDuration) }
    }

    private fun showOnForeground(backgroundDurationMs: Long) {
        if (suppressNextForeground) {
            Log.d(TAG, "AppOpen foreground: skipped, suppressed")
            suppressNextForeground = false
            preload()
            return
        }
        val activity = currentActivity?.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG, "AppOpen foreground: skipped, no active activity")
            return
        }
        if (!isEligible(backgroundDurationMs)) {
            preload()
            return
        }
        val ad = takeFreshLoadedAd()
        if (ad == null) {
            Log.d(TAG, "AppOpen foreground: skipped, ad not ready")
            preload()
            return
        }
        show(activity, ad, onFinished = {})
    }

    // ---------------------------------------------------------------------------------------
    // Показ
    // ---------------------------------------------------------------------------------------

    private fun show(activity: Activity, ad: AppOpenAd, onFinished: () -> Unit) {
        isShowing = true
        ad.setAdEventListener(object : AppOpenAdEventListener {
            override fun onAdShown() {
                Log.d(TAG, "AppOpen onAdShown")
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.e(TAG, "AppOpen onAdFailedToShow: description=${adError.description}")
                release(ad)
                onFinished()
            }

            override fun onAdDismissed() {
                Log.d(TAG, "AppOpen onAdDismissed")
                lastShownAtMs = System.currentTimeMillis()
                release(ad)
                onFinished()
            }

            override fun onAdClicked() {
                Log.d(TAG, "AppOpen onAdClicked")
                // Клик уводит пользователя из приложения — при возврате не показываем App Open повторно
                suppressNextForeground = true
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d(TAG, "AppOpen onAdImpression: rawData=${impressionData?.rawData}")
            }
        })
        Log.d(TAG, "AppOpen show")
        ad.show(activity)
    }

    /** Очистка после показа и предзагрузка следующего объявления. */
    private fun release(ad: AppOpenAd) {
        ad.setAdEventListener(null)
        isShowing = false
        preload()
    }

    // ---------------------------------------------------------------------------------------
    // Ожидание рекламы на холодном старте
    // ---------------------------------------------------------------------------------------

    private fun onLoadedForLaunchWaiter() {
        val waiter = launchWaiter ?: return
        launchWaiter = null
        mainHandler.removeCallbacks(waiter.timeoutRunnable)
        val activity = waiter.activity.get()
        val ad = takeFreshLoadedAd()
        if (activity != null && !activity.isFinishing && !activity.isDestroyed && ad != null) {
            show(activity, ad, waiter.onFinished)
        } else {
            waiter.onFinished()
        }
    }

    private fun finishLaunchWaiter() {
        val waiter = launchWaiter ?: return
        launchWaiter = null
        mainHandler.removeCallbacks(waiter.timeoutRunnable)
        waiter.onFinished()
    }

    // ---------------------------------------------------------------------------------------
    // Правила показа
    // ---------------------------------------------------------------------------------------

    /** [backgroundDurationMs] = null означает холодный старт. */
    private fun isEligible(backgroundDurationMs: Long?): Boolean {
        if (adUnitId.isBlank()) return false
        if (isShowing) {
            Log.d(TAG, "AppOpen eligible=false: ad is showing")
            return false
        }
        if (!shouldShow()) {
            Log.d(TAG, "AppOpen eligible=false: shouldShow() returned false")
            return false
        }
        if (launchCount() < config.minLaunchesBeforeFirstAd) {
            Log.d(TAG, "AppOpen eligible=false: new user (launches=${launchCount()}, need=${config.minLaunchesBeforeFirstAd})")
            return false
        }
        val sinceLastShow = System.currentTimeMillis() - lastShownAtMs
        if (lastShownAtMs != 0L && sinceLastShow < config.cooldownMs) {
            Log.d(TAG, "AppOpen eligible=false: cooldown (passed=${sinceLastShow}ms)")
            return false
        }
        if (backgroundDurationMs != null && backgroundDurationMs < config.minBackgroundMs) {
            Log.d(TAG, "AppOpen eligible=false: short background (${backgroundDurationMs}ms)")
            return false
        }
        return true
    }

    private fun isLoadedAdFresh(): Boolean =
        loadedAd != null && System.currentTimeMillis() - loadedAtMs <= config.maxAdAgeMs

    /** Забирает загруженное объявление (если оно не устарело). Повторно использовать его нельзя. */
    private fun takeFreshLoadedAd(): AppOpenAd? {
        val ad = loadedAd ?: return null
        if (!isLoadedAdFresh()) {
            Log.d(TAG, "AppOpen loaded ad is stale, dropping")
            clearLoaded()
            return null
        }
        loadedAd = null
        return ad
    }

    private fun clearLoaded() {
        loadedAd?.setAdEventListener(null)
        loadedAd = null
        loadedAtMs = 0L
    }

    // ---------------------------------------------------------------------------------------
    // Счётчик запусков
    // ---------------------------------------------------------------------------------------

    private fun prefs() = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun launchCount(): Int = prefs().getInt(KEY_LAUNCH_COUNT, 0)
    private fun incrementLaunchCount() {
        prefs().edit().putInt(KEY_LAUNCH_COUNT, launchCount() + 1).apply()
    }

    // ---------------------------------------------------------------------------------------
    // Отслеживание текущей Activity (нужна для ad.show(activity))
    // ---------------------------------------------------------------------------------------

    private fun Activity.isAdsSdkActivity() = javaClass.name.startsWith(ADS_PACKAGE_PREFIX)

    override fun onActivityResumed(activity: Activity) {
        if (!activity.isAdsSdkActivity()) currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity?.get() === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}