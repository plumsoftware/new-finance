package ru.plumsoftware.finance.ui.ds

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Экраны со свайпом для удаления: ключ показа, текст пояснения и эмодзи для демо-строки. */
enum class SwipeHint(val key: String, val textRes: Int, val demoEmoji: String) {
    ACCOUNTS("swipe_accounts", R.string.hint_swipe_accounts, "💳"),
    CATEGORIES("swipe_categories", R.string.hint_swipe_categories, "🛒"),
    RECURRING("swipe_recurring", R.string.hint_swipe_recurring, "🔁"),
    HISTORY("swipe_history", R.string.hint_swipe_history, "☕"),
    NOTIFICATIONS("swipe_notifications", R.string.hint_swipe_notifications, "🔔"),
}

/**
 * Подсказка «свайп влево — удалить». При первом заходе на экран показывается сама и запоминается
 * в настройках; возвращает действие для повторного показа (кнопка «i» в шапке).
 */
@Composable
fun rememberSwipeHint(
    hint: SwipeHint,
    /** Автопоказ только когда на экране есть что свайпать. */
    hasItems: Boolean = true,
    settingsRepository: SettingsRepository = koinInject(),
): () -> Unit {
    val scope = rememberCoroutineScope()
    var visible by rememberSaveable(hint) { mutableStateOf(false) }
    LaunchedEffect(hint, hasItems) {
        if (hasItems && hint.key !in settingsRepository.settings.first().seenHints) visible = true
    }
    if (visible) {
        SwipeHintDialog(hint) {
            visible = false
            scope.launch { settingsRepository.update { it.copy(seenHints = it.seenHints + hint.key) } }
        }
    }
    return remember(hint) { { visible = true } }
}

/** Кнопка «i» для шапки экрана. */
@Composable
fun SwipeHintButton(onClick: () -> Unit) {
    IconButton44(
        iconRes = R.drawable.ic_about,
        contentDescription = stringResource(R.string.hint_swipe_button),
        onClick = onClick,
        tint = FinanceTheme.colors.textSecondary,
    )
}

@Composable
private fun SwipeHintDialog(hint: SwipeHint, onDismiss: () -> Unit) {
    val c = FinanceTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(c.surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SwipeDemo(hint.demoEmoji)
            VSpace(18.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                KopiImage(Kopi.HAPPY, 44.dp)
                HSpace(10.dp)
                Text(
                    stringResource(R.string.hint_swipe_title),
                    style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                )
            }
            VSpace(10.dp)
            Text(
                stringResource(hint.textRes),
                style = FinanceType.bodySmall,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            VSpace(20.dp)
            ButtonPrimary(
                text = stringResource(R.string.hint_swipe_ok),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp,
            )
        }
    }
}

/** Мини-анимация: строка уезжает влево, открывая «Удалить», и возвращается. */
@Composable
private fun SwipeDemo(emoji: String) {
    val c = FinanceTheme.colors
    val transition = rememberInfiniteTransition(label = "swipeDemo")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2600
                0f at 0
                0f at 500
                1f at 1100 using FastOutSlowInEasing
                1f at 1900
                0f at 2400 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "shift",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(c.danger)
            .clearAndSetSemantics { },
    ) {
        Text(
            stringResource(R.string.delete),
            style = FinanceType.title,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp),
        )
        Row(
            Modifier
                .fillMaxSize()
                .offset(x = (-104).dp * shift)
                .background(c.bg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(emoji, c.primary, size = 40.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Box(Modifier.width(110.dp).height(10.dp).background(c.outline, RoundedCornerShape(5.dp)))
                VSpace(8.dp)
                Box(Modifier.width(70.dp).height(8.dp).background(c.trackMuted, RoundedCornerShape(4.dp)))
            }
            Box(Modifier.width(48.dp).height(10.dp).background(c.outline, RoundedCornerShape(5.dp)))
        }
        // Палец, показывающий направление свайпа.
        Text(
            "👆",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 40.dp)
                .offset(x = (-104).dp * shift, y = 14.dp),
            style = FinanceType.titleLarge,
        )
    }
}
