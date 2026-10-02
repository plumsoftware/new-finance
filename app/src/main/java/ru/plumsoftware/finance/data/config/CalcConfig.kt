package ru.plumsoftware.finance.data.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.plumsoftware.finance.BuildConfig

/** Условия программ, которые меняются (§8.5): ставка и лимит семейной ипотеки. */
data class CalcParams(
    val familyMortgageRatePercent: Double = DEFAULT_FAMILY_RATE,
    val familyMortgageLimitRub: Double = DEFAULT_FAMILY_LIMIT,
) {
    companion object {
        const val DEFAULT_FAMILY_RATE = 6.0
        const val DEFAULT_FAMILY_LIMIT = 6_000_000.0
    }
}

/**
 * Удалённая конфигурация калькуляторов. Без Firebase (нет google-services.json) — значения по умолчанию.
 * Ключи Remote Config: `family_mortgage_rate`, `family_mortgage_limit`.
 */
class CalcConfigRepository {
    private val _params = MutableStateFlow(CalcParams())
    val params: StateFlow<CalcParams> = _params.asStateFlow()

    fun refresh() {
        if (!BuildConfig.FIREBASE_ENABLED) return
        runCatching {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 12 * 60 * 60 })
            rc.setDefaultsAsync(
                mapOf(
                    KEY_RATE to CalcParams.DEFAULT_FAMILY_RATE,
                    KEY_LIMIT to CalcParams.DEFAULT_FAMILY_LIMIT,
                ),
            )
            apply(rc)
            rc.fetchAndActivate().addOnCompleteListener { apply(rc) }
        }
    }

    private fun apply(rc: FirebaseRemoteConfig) {
        val rate = rc.getDouble(KEY_RATE).takeIf { it > 0 } ?: CalcParams.DEFAULT_FAMILY_RATE
        val limit = rc.getDouble(KEY_LIMIT).takeIf { it > 0 } ?: CalcParams.DEFAULT_FAMILY_LIMIT
        _params.value = CalcParams(rate, limit)
    }

    private companion object {
        const val KEY_RATE = "family_mortgage_rate"
        const val KEY_LIMIT = "family_mortgage_limit"
    }
}
