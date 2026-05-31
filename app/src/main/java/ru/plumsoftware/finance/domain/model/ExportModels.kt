package ru.plumsoftware.finance.domain.model

import androidx.annotation.StringRes
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.Transaction

enum class ExportFormat(
    @StringRes val labelRes: Int,
    val mimeType: String,
    val extension: String,
) {
    CSV(R.string.export_format_csv, "text/csv", "csv"),
    JSON(R.string.export_format_json, "application/json", "json"),
    PDF(R.string.export_format_pdf, "application/pdf", "pdf"),
}

enum class ExportPeriod(@StringRes val labelRes: Int) {
    THIS_MONTH(R.string.period_this_month),
    LAST_3_MONTHS(R.string.period_last_3m),
    THIS_YEAR(R.string.period_this_year),
    ALL_TIME(R.string.period_all_time),
    CUSTOM(R.string.period_custom),
}

data class ExportOptions(
    val transactions: Boolean = true,
    val categories: Boolean = true,
    val assets: Boolean = true,
)

data class ExportData(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val assets: List<SmartAsset> = emptyList(),
    val currencyCode: String = "RUB",
)

sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data class Success(val uri: android.net.Uri) : ExportState()
    data class Error(val message: String?) : ExportState()
}
