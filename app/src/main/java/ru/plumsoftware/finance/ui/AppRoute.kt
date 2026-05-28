package ru.plumsoftware.finance.ui

sealed class AppRoute(val route: String) {
    object Onboarding : AppRoute("onboarding")
    object Home : AppRoute("home")
    object History : AppRoute("history")
    object SmartSavings : AppRoute("smart_savings")
    object SmartSavingsCreate : AppRoute("smart_savings/create") // Базовый роут
    object Analytics : AppRoute("analytics")
    object Settings : AppRoute("settings")
    object Categories : AppRoute("settings/categories")
    object AddTransaction : AppRoute("add_transaction")

    companion object {
        const val SMART_DETAIL = "smart_savings/detail/{assetId}"
        fun smartDetail(assetId: Long) = "smart_savings/detail/$assetId"

        const val SMART_CREATE_WITH_ARGS = "smart_savings/create?assetId={assetId}"
        fun smartCreate(assetId: Long? = null) =
            if (assetId != null) "smart_savings/create?assetId=$assetId" else "smart_savings/create"

        const val CATEGORY_EDIT = "settings/categories/edit?categoryId={categoryId}&type={type}"
        fun categoryEdit(categoryId: Long? = null, type: String? = null): String {
            val idPart = if (categoryId != null) "categoryId=$categoryId" else "categoryId="
            val typePart = if (type != null) "&type=$type" else "&type="
            return "settings/categories/edit?$idPart$typePart"
        }
    }
}