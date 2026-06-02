package ru.plumsoftware.finance.ui

sealed class AppRoute(val route: String) {
    object Onboarding : AppRoute("onboarding")
    object Home : AppRoute("home")
    object History : AppRoute("history")
    object SmartSavings : AppRoute("smart_savings")
    object SmartSavingsCreate : AppRoute("smart_savings/create") // Базовый роут
    object Goals : AppRoute("goals")
    object GoalsCreate : AppRoute("goals/create")
    object Achievements : AppRoute("achievements")
    object Analytics : AppRoute("analytics")
    object Settings : AppRoute("settings")
    object Categories : AppRoute("settings/categories")
    object Limits : AppRoute("limits")
    object Notifications : AppRoute("notifications")
    object AddTransaction : AppRoute("add_transaction")
    object Export : AppRoute("settings/export")
    object Recurring : AppRoute("settings/recurring")
    object Permissions : AppRoute("settings/permissions")
    object ImportPicker : AppRoute("settings/import")

    object ImportPreview : AppRoute("import_preview/{encodedPath}") {
        fun route(encodedPath: String) =
            "import_preview/${android.net.Uri.encode(encodedPath)}"
    }

    companion object {
        const val SMART_DETAIL = "smart_savings/detail/{assetId}"
        fun smartDetail(assetId: Long) = "smart_savings/detail/$assetId"

        const val SMART_CREATE_WITH_ARGS = "smart_savings/create?assetId={assetId}"
        fun smartCreate(assetId: Long? = null) =
            if (assetId != null) "smart_savings/create?assetId=$assetId" else "smart_savings/create"

        const val GOAL_DETAIL = "goals/detail/{goalId}"
        fun goalDetail(goalId: Long) = "goals/detail/$goalId"

        const val GOAL_CREATE_WITH_ARGS = "goals/create?goalId={goalId}"
        fun goalCreate(goalId: Long? = null) =
            if (goalId != null) "goals/create?goalId=$goalId" else "goals/create"

        const val ADD_TRANSACTION_WITH_ARGS = "add_transaction?quickCategory={quickCategory}"
        fun addTransaction(quickCategory: String? = null): String =
            if (quickCategory.isNullOrBlank()) "add_transaction"
            else "add_transaction?quickCategory=${android.net.Uri.encode(quickCategory)}"

        const val CATEGORY_EDIT = "settings/categories/edit?categoryId={categoryId}&type={type}"
        fun categoryEdit(categoryId: Long? = null, type: String? = null): String {
            val idPart = if (categoryId != null) "categoryId=$categoryId" else "categoryId="
            val typePart = if (type != null) "&type=$type" else "&type="
            return "settings/categories/edit?$idPart$typePart"
        }
    }
}