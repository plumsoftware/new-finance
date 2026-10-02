package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.config.SavedCalculationsRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** «Мои расчёты» — история сохранённых расчётов (§6.8 п.5). */
@Composable
fun SavedCalculationsScreen(onBack: () -> Unit, repo: SavedCalculationsRepository = koinInject()) {
    val c = FinanceTheme.colors
    val items by repo.items.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(stringResource(R.string.tools_saved), onBack)
        if (items.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.tools_saved_empty),
                text = stringResource(R.string.tools_saved_empty_sub),
                pose = Kopi.THINKING,
                modifier = Modifier.padding(top = 48.dp),
            )
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FCard {
                    Text(item.title, style = FinanceType.titleBold, color = c.textPrimary)
                    Text(item.result, style = FinanceType.body, color = c.textPrimary)
                    if (item.details.isNotBlank()) Text(item.details, style = FinanceType.caption, color = c.textSecondary)
                    val date = DateFmt.toLocalDate(item.savedAtMillis)
                    Text("${DateFmt.dayMonth(date)} ${date.year} · ${DateFmt.time(item.savedAtMillis)}", style = FinanceType.caption, color = c.textSecondary)
                    TextAction(stringResource(R.string.delete), { repo.delete(item.id) }, color = c.dangerText)
                }
            }
        }
    }
}
