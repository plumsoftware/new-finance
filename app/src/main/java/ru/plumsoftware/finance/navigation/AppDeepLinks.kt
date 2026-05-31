package ru.plumsoftware.finance.navigation

import android.content.Intent
import android.net.Uri

/**
 * Custom URI scheme for in-app navigation.
 *
 * Format: `finance://app/{path}?{query}`
 *
 * See [docs/DEEPLINKS.md] for the full list.
 */
object AppDeepLinks {
    const val SCHEME = "finance"
    const val HOST = "app"
    const val BASE = "$SCHEME://$HOST"

    fun home(): Uri = uri("home")
    fun history(): Uri = uri("history")
    fun analytics(): Uri = uri("analytics")
    fun settings(): Uri = uri("settings")
    fun onboarding(): Uri = uri("onboarding")
    fun smartSavings(): Uri = uri("smart_savings")
    fun smartSavingsCreate(assetId: Long? = null): Uri =
        if (assetId != null) uri("smart_savings/create", "assetId" to assetId.toString())
        else uri("smart_savings/create")
    fun smartSavingsDetail(assetId: Long): Uri = uri("smart_savings/detail/$assetId")
    fun limits(): Uri = uri("limits")
    fun notifications(): Uri = uri("notifications")
    fun addTransaction(): Uri = uri("add_transaction")
    fun categories(): Uri = uri("settings/categories")
    fun categoryEdit(categoryId: Long? = null, type: String? = null): Uri {
        val params = buildList {
            if (categoryId != null) add("categoryId" to categoryId.toString())
            if (type != null) add("type" to type)
        }
        return uri("settings/categories/edit", *params.toTypedArray())
    }
    fun export(): Uri = uri("settings/export")
    fun recurring(): Uri = uri("settings/recurring")
    fun permissions(): Uri = uri("settings/permissions")
    fun importPicker(): Uri = uri("settings/import")

    fun isAppDeepLink(uri: Uri?): Boolean =
        uri?.scheme == SCHEME && uri.host == HOST

    fun isAppDeepLink(intent: Intent?): Boolean {
        if (intent == null) return false
        if (intent.action != Intent.ACTION_VIEW) return false
        return isAppDeepLink(intent.data)
    }

    private fun uri(path: String, vararg query: Pair<String, String>): Uri {
        val builder = Uri.parse("$BASE/$path").buildUpon()
        query.forEach { (key, value) -> builder.appendQueryParameter(key, value) }
        return builder.build()
    }
}
