package ru.plumsoftware.finance.presentation.notifications

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationType
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val StreakOrange = Color(0xFFFF6B35)
private val WarningOrange = Color(0xFFFF9500)

private enum class NotificationDateGroup(@StringRes val labelRes: Int) {
    TODAY(R.string.notif_group_today),
    YESTERDAY(R.string.notif_group_yesterday),
    EARLIER(R.string.notif_group_earlier),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val filteredNotifications = remember(notifications, selectedTab) {
        when (selectedTab) {
            1 -> notifications.filter { !it.isRead }
            else -> notifications
        }
    }

    val grouped = remember(filteredNotifications) {
        filteredNotifications
            .groupBy { it.dateGroup() }
            .toList()
            .sortedBy { it.first.ordinal }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notifications),
                        style = typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                        ),
                        color = colors.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navController::popBackStack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = colors.primary,
                            modifier = Modifier.size(Dimens.iconSizeNav),
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(visible = notifications.isNotEmpty()) {
                        TextButton(onClick = viewModel::clearAll) {
                            Text(
                                text = stringResource(R.string.clear_all),
                                style = typography.bodyMedium,
                                color = colors.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { padding ->
        if (notifications.isEmpty()) {
            NotificationsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                NotificationFilterTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.SpacingL,
                        vertical = Dimens.SpacingXs,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                ) {
                    grouped.forEach { (group, items) ->
                        stickyHeader {
                            Text(
                                text = stringResource(group.labelRes),
                                style = typography.labelMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.background)
                                    .padding(vertical = Dimens.SpacingS),
                            )
                        }
                        items(items, key = { it.id }) { notification ->
                            NotificationCard(
                                notification = notification,
                                onTap = {
                                    viewModel.markRead(notification.id)
                                    when (notification.type) {
                                        NotificationType.LIMIT_WARNING,
                                        NotificationType.LIMIT_EXCEEDED,
                                        -> navController.navigate(AppRoute.Limits.route)
                                        NotificationType.SAVINGS_MILESTONE ->
                                            navController.navigate(AppRoute.SmartSavings.route)
                                        else -> Unit
                                    }
                                },
                                onDismiss = { viewModel.delete(notification.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val tabs = listOf(R.string.notif_tab_all, R.string.notif_tab_unread)

    Row(
        modifier = Modifier.padding(
            horizontal = Dimens.SpacingL,
            vertical = Dimens.SpacingS,
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        tabs.forEachIndexed { index, labelRes ->
            val selected = selectedTab == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                    .background(
                        if (selected) colors.primary else Color.Transparent,
                    )
                    .clickable { onTabSelected(index) }
                    .padding(
                        horizontal = Dimens.SpacingM,
                        vertical = Dimens.SpacingXs,
                    ),
            ) {
                Text(
                    text = stringResource(labelRes),
                    style = typography.bodyMedium,
                    color = if (selected) Color.White else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: AppNotification,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val typeColor = notification.type.color()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        },
        positionalThreshold = { fullWidth -> fullWidth * 0.35f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimens.RadiusL))
                    .background(colors.error.copy(alpha = 0.1f))
                    .padding(end = Dimens.SpacingL),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = colors.error,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
            }
        },
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTap,
        ) {
            Row(
                modifier = Modifier.padding(Dimens.SpacingM),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.emojiPickerSize)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = notification.type.icon(),
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(Dimens.IconSizeM),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.SpacingM),
                ) {
                    Text(
                        text = notificationTitle(notification),
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXxs))
                    Text(
                        text = notificationBody(notification),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXxs))
                    Text(
                        text = formatNotificationTime(notification.receivedAtMillis),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.notificationBadge)
                            .clip(CircleShape)
                            .background(colors.primary),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsEmptyState(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconSizeXl + Dimens.SpacingS),
            tint = colors.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingM))
        Text(
            text = stringResource(R.string.no_notifications),
            style = typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
        Text(
            text = stringResource(R.string.no_notifications_desc),
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimens.SpacingXxl),
        )
    }
}

@Composable
private fun notificationTitle(notification: AppNotification): String {
    val titleRes = notification.titleRes
    return if (titleRes != null) {
        stringResource(titleRes)
    } else {
        notification.title
    }
}

@Composable
private fun notificationBody(notification: AppNotification): String {
    val bodyRes = notification.bodyRes
    return if (bodyRes != null) {
        stringResource(bodyRes, *notification.bodyArgs.toTypedArray())
    } else {
        notification.body
    }
}

@Composable
private fun NotificationType.color(): Color {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        NotificationType.LIMIT_WARNING -> WarningOrange
        NotificationType.LIMIT_EXCEEDED -> colors.error
        NotificationType.SAVINGS_MILESTONE -> colors.secondary
        NotificationType.MONTHLY_SUMMARY -> colors.primary
        NotificationType.STREAK -> StreakOrange
    }
}

private fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.LIMIT_WARNING -> Icons.Rounded.Warning
    NotificationType.LIMIT_EXCEEDED -> Icons.Rounded.Error
    NotificationType.SAVINGS_MILESTONE -> Icons.Rounded.EmojiEvents
    NotificationType.MONTHLY_SUMMARY -> Icons.Rounded.BarChart
    NotificationType.STREAK -> Icons.Rounded.LocalFireDepartment
}

private fun AppNotification.dateGroup(): NotificationDateGroup {
    val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US)
    val today = dayKey.format(Date())
    val yesterday = Calendar.getInstance()
        .apply { add(Calendar.DAY_OF_YEAR, -1) }
        .time
        .let(dayKey::format)
    val value = dayKey.format(Date(receivedAtMillis))
    return when (value) {
        today -> NotificationDateGroup.TODAY
        yesterday -> NotificationDateGroup.YESTERDAY
        else -> NotificationDateGroup.EARLIER
    }
}

@Composable
private fun formatNotificationTime(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestampMillis
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> stringResource(R.string.notif_time_just_now)
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
            stringResource(R.string.notif_time_minutes_ago, minutes)
        }
        else -> {
            val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US)
            val today = dayKey.format(Date())
            val yesterday = Calendar.getInstance()
                .apply { add(Calendar.DAY_OF_YEAR, -1) }
                .time
                .let(dayKey::format)
            val value = dayKey.format(Date(timestampMillis))
            val time = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(timestampMillis))
            when (value) {
                today -> time
                yesterday -> "${stringResource(R.string.yesterday)}, $time"
                else -> SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
                    .format(Date(timestampMillis))
                    .lowercase(Locale.getDefault())
            }
        }
    }
}
