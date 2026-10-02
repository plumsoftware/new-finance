package ru.plumsoftware.finance.presentation.export

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.report.ReportFormat
import ru.plumsoftware.finance.data.report.ReportKind
import ru.plumsoftware.finance.data.report.ReportRequest
import ru.plumsoftware.finance.data.report.ReportService
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.ui.ds.ButtonOutlined
import ru.plumsoftware.finance.ui.ds.ButtonPrimary
import ru.plumsoftware.finance.ui.ds.FBottomSheet
import ru.plumsoftware.finance.ui.ds.FSwitch
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Нижняя панель выгрузки (§6.6): «Поделиться отчётом» из Аналитики или «Экспорт данных» из Настроек.
 */
@Composable
fun ReportExportSheet(
    kind: ReportKind,
    range: DateRange,
    isWholeMonth: Boolean,
    periodLabel: String,
    onDismiss: () -> Unit,
    excludedIds: Set<Long> = emptySet(),
    excludedNames: List<String> = emptyList(),
    onChangePeriod: (() -> Unit)? = null,
    onOpenBackup: (() -> Unit)? = null,
    service: ReportService = koinInject(),
) {
    val c = FinanceTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalMascotSnackbar.current
    var format by rememberSaveable { mutableStateOf(ReportFormat.PDF) }
    var chart by rememberSaveable { mutableStateOf(true) }
    var categories by rememberSaveable { mutableStateOf(true) }
    var operations by rememberSaveable { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }

    val request = ReportRequest(
        kind = kind,
        format = format,
        range = range,
        isWholeMonth = isWholeMonth,
        excludedCategoryIds = excludedIds,
        includeChart = chart,
        includeCategories = categories,
        includeOperations = operations,
    )
    val fileName = service.fileName(request)
    val savedMsg = stringResource(R.string.export_saved)
    val errorMsg = stringResource(R.string.export_error_generic)
    val shareTitle = stringResource(R.string.export_share_title)

    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(format.mimeType)) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            runCatching {
                val file = service.generate(request)
                service.copyTo(file.file, uri)
            }.onSuccess {
                snackbar.show(savedMsg, Kopi.HAPPY)
                onDismiss()
            }.onFailure { snackbar.show(errorMsg, Kopi.THINKING) }
            busy = false
        }
    }

    FBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Text(
                stringResource(if (kind == ReportKind.REPORT) R.string.export_sheet_report_title else R.string.export_sheet_data_title),
                style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            VSpace(4.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (kind == ReportKind.REPORT) stringResource(R.string.export_period, periodLabel)
                    else stringResource(R.string.export_all_ops, periodLabel),
                    style = FinanceType.bodySmall,
                    color = c.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                if (onChangePeriod != null) {
                    TextAction(stringResource(R.string.export_change), onChangePeriod)
                }
            }
            VSpace(12.dp)
            FormatCard(ReportFormat.PDF, "PDF", Color(0xFFFF3B30), stringResource(R.string.export_pdf_desc), format) { format = it }
            VSpace(8.dp)
            FormatCard(ReportFormat.XLSX, "Excel", Color(0xFF1D8B4A), stringResource(R.string.export_xlsx_desc), format) { format = it }
            if (kind == ReportKind.EXPORT) {
                VSpace(8.dp)
                FormatCard(ReportFormat.CSV, "CSV", Color(0xFF6E6E76), stringResource(R.string.export_csv_desc), format) { format = it }
            }
            if (kind == ReportKind.REPORT) {
                VSpace(12.dp)
                ToggleRow(stringResource(R.string.report_section_chart), chart) { chart = it }
                ToggleRow(stringResource(R.string.export_toggle_categories), categories) { categories = it }
                ToggleRow(stringResource(R.string.export_toggle_operations), operations) { operations = it }
                if (excludedNames.isNotEmpty()) {
                    VSpace(6.dp)
                    Text(
                        stringResource(R.string.report_excluded, excludedNames.joinToString(", ")),
                        style = FinanceType.bodySmall,
                        color = c.warningText,
                    )
                }
            }
            VSpace(12.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(c.bg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_file), null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                HSpace(10.dp)
                Text(fileName, style = FinanceType.bodySmall, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            VSpace(16.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ButtonOutlined(
                    stringResource(R.string.save),
                    onClick = { saver.launch(fileName) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    height = 54.dp,
                )
                ButtonPrimary(
                    stringResource(R.string.export_share),
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            runCatching { service.generate(request) }
                                .onSuccess { f ->
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = f.mimeType
                                        putExtra(Intent.EXTRA_STREAM, f.uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(send, shareTitle))
                                    onDismiss()
                                }
                                .onFailure { snackbar.show(errorMsg, Kopi.THINKING) }
                            busy = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (onOpenBackup != null) {
                VSpace(4.dp)
                TextAction(stringResource(R.string.export_backup_link), onOpenBackup, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            VSpace(12.dp)
        }
    }
}

@Composable
private fun FormatCard(
    value: ReportFormat,
    label: String,
    color: Color,
    description: String,
    selected: ReportFormat,
    onSelect: (ReportFormat) -> Unit,
) {
    val c = FinanceTheme.colors
    val isSelected = value == selected
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) c.selectedCardBg else c.surface)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) c.primary else c.outline, RoundedCornerShape(16.dp))
            .clickable(role = Role.RadioButton) { onSelect(value) }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(label.take(4).uppercase(), style = FinanceType.captionBold, color = Color.White, maxLines = 1)
        }
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Text(label, style = FinanceType.title, color = c.textPrimary)
            Text(description, style = FinanceType.caption, color = c.textSecondary)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = FinanceTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = FinanceType.body, color = c.textPrimary, modifier = Modifier.weight(1f))
        FSwitch(checked, onChange)
    }
}
