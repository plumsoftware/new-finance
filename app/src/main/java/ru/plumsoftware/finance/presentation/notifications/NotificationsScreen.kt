package ru.plumsoftware.finance.presentation.notifications

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationType
import ru.plumsoftware.finance.navigation.navigateNotificationDeepLink
import ru.plumsoftware.finance.navigation.parseNotificationDeepLink
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.SwipeHint
import ru.plumsoftware.finance.ui.ds.SwipeHintButton
import ru.plumsoftware.finance.ui.ds.rememberSwipeHint
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate
import java.util.concurrent.TimeUnit

private enum class NotificationDateGroup(@StringRes val labelRes: Int) {
    TODAY(R.string.notif_group_today),
    YESTERDAY(R.string.notif_group_yesterday),
    EARLIER(R.string.notif_group_earlier),
}

private fun AppNotification.dateGroup(today: LocalDate): NotificationDateGroup = when (DateFmt.toLocalDate(receivedAtMillis)) {
    today -> NotificationDateGroup.TODAY
    today.minusDays(1) -> NotificationDateGroup.YESTERDAY
    else -> NotificationDateGroup.EARLIER
}

/** Уведомления: «Все / Непрочитанные», группы по дням, «Прочитать все», «Очистить», удаление свайпом. */
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirmClear by remember { mutableStateOf(false) }
    val showSwipeHint = rememberSwipeHint(SwipeHint.NOTIFICATIONS, hasItems = notifications.isNotEmpty())
    val unread = notifications.count { !it.isRead }
    val today = LocalDate.now()
    val grouped = remember(notifications, tab, today) {
        notifications
            .filter { tab == 0 || !it.isRead }
            .sortedByDescending { it.receivedAtMillis }
            .groupBy { it.dateGroup(today) }
            .toSortedMap(compareBy { it.ordinal })
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.notif_clear_title), style = FinanceType.titleLarge, color = c.textPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    confirmClear = false
                }) { Text(stringResource(R.string.notif_clear), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = stringResource(R.string.notifications),
            onBack = { navController.popBackStackOrHome() },
            actions = {
                if (notifications.isNotEmpty()) SwipeHintButton(showSwipeHint)
                if (unread > 0) TextAction(stringResource(R.string.notif_read_all), viewModel::markAllRead)
            },
        )
        if (notifications.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_notifications),
                text = stringResource(R.string.no_notifications_desc),
                pose = Kopi.SLEEPING,
                modifier = Modifier.padding(top = 48.dp),
            )
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FChip(stringResource(R.string.notif_tab_all), selected = tab == 0, onClick = { tab = 0 })
                    FChip(
                        if (unread > 0) "${stringResource(R.string.notif_tab_unread)} · $unread" else stringResource(R.string.notif_tab_unread),
                        selected = tab == 1,
                        onClick = { tab = 1 },
                    )
                }
            }
            if (grouped.isEmpty()) {
                item { EmptyState(title = stringResource(R.string.notif_all_read), pose = Kopi.HAPPY, mascotSize = 72.dp) }
            }
            grouped.forEach { (group, items) ->
                item(key = "h_${group.name}") {
                    Text(
                        stringResource(group.labelRes),
                        style = FinanceType.titleSection,
                        color = c.textPrimary,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
                item(key = "g_${group.name}") {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(c.surface),
                    ) {
                        items.forEachIndexed { i, n ->
                            if (i > 0) CardDivider(Modifier.padding(start = 70.dp))
                            NotificationRow(
                                notification = n,
                                onTap = {
                                    viewModel.markRead(n.id)
                                    val deepLink = parseNotificationDeepLink(n.data)
                                    when {
                                        deepLink != null -> navController.navigateNotificationDeepLink(deepLink)
                                        n.type == NotificationType.LIMIT_WARNING || n.type == NotificationType.LIMIT_EXCEEDED ->
                                            navController.navigate(AppRoute.Limits.route)
                                        n.type == NotificationType.SAVINGS_MILESTONE -> navController.navigate(AppRoute.SmartSavings.route)
                                        n.type == NotificationType.STREAK -> navController.navigate(AppRoute.Achievements.route)
                                        n.type == NotificationType.MONTHLY_SUMMARY -> navController.navigate(AppRoute.Analytics.route)
                                    }
                                },
                                onDelete = { viewModel.delete(n.id) },
                            )
                        }
                    }
                }
            }
            item {
                VSpace(8.dp)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextAction(stringResource(R.string.notif_clear), { confirmClear = true }, color = c.dangerText)
                }
            }
        }
    }
}

private fun NotificationType.emoji(): String = when (this) {
    NotificationType.LIMIT_WARNING -> "⚠️"
    NotificationType.LIMIT_EXCEEDED -> "🚫"
    NotificationType.SAVINGS_MILESTONE -> "🏆"
    NotificationType.MONTHLY_SUMMARY -> "📊"
    NotificationType.STREAK -> "🔥"
}

@Composable
private fun NotificationType.tint(): Color {
    val c = FinanceTheme.colors
    return when (this) {
        NotificationType.LIMIT_WARNING -> c.warning
        NotificationType.LIMIT_EXCEEDED -> c.danger
        NotificationType.SAVINGS_MILESTONE -> c.success
        NotificationType.MONTHLY_SUMMARY -> c.primary
        NotificationType.STREAK -> c.warning
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationRow(notification: AppNotification, onTap: () -> Unit, onDelete: () -> Unit) {
    val c = FinanceTheme.colors
    val swipe = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    val title = notification.titleRes?.let { stringResource(it) } ?: notification.title
    val body = notification.bodyRes?.let { stringResource(it, *notification.bodyArgs.toTypedArray()) } ?: notification.body
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(c.danger).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Text(stringResource(R.string.delete), style = FinanceType.title, color = Color.White)
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .heightIn(min = 72.dp)
                .clickable(role = Role.Button, onClick = onTap)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            EmojiBadge(notification.type.emoji(), notification.type.tint(), size = 42.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = FinanceType.bodyMedium.copy(fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold),
                    color = c.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (body.isNotBlank()) {
                    Text(body, style = FinanceType.bodySmall, color = c.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Text(timeLabel(notification.receivedAtMillis), style = FinanceType.caption, color = c.textSecondary)
            }
            if (!notification.isRead) {
                HSpace(8.dp)
                Box(Modifier.padding(top = 6.dp).size(8.dp).background(c.primary, CircleShape))
            }
        }
    }
}

@Composable
private fun timeLabel(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val date = DateFmt.toLocalDate(millis)
    val today = LocalDate.now()
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> stringResource(R.string.notif_time_just_now)
        diff < TimeUnit.HOURS.toMillis(1) -> stringResource(R.string.notif_time_minutes_ago, TimeUnit.MILLISECONDS.toMinutes(diff).toInt())
        date == today || date == today.minusDays(1) -> DateFmt.time(millis)
        else -> "${DateFmt.dayMonthShort(date)}, ${DateFmt.time(millis)}"
    }
}
