package ru.plumsoftware.finance.presentation.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.ScreenPadding
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

private val RecordColor = Color(0xFFFFD60A)
private val UnlockedIconBg = Color(0xFFFFF1D6)

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(title = stringResource(R.string.achievements_title), onBack = onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        ) {
            item(span = { GridItemSpan(2) }) { StreakHero(state) }
            items(state.achievements, key = { it.key }) { AchievementCard(it) }
        }
    }
}

@Composable
private fun StreakHero(state: AchievementsUiState) {
    val c = FinanceTheme.colors
    val streak = state.streak.currentStreak
    InkCard(radius = 26.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (streak > 0) {
                        stringResource(R.string.ach_hero_streak, pluralStringResource(R.plurals.pl_days, streak, streak))
                    } else {
                        stringResource(R.string.ach_hero_missing)
                    },
                    style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                val next = listOf(AchievementId.WEEK_MARATHON, AchievementId.MONTH_DISCIPLINE, AchievementId.LEGEND)
                    .firstOrNull { it.target > streak }
                if (next != null) {
                    val left = next.target - streak
                    VSpace(4.dp)
                    Text(
                        text = stringResource(
                            R.string.ach_hero_next,
                            stringResource(AchievementUi.titleRes(next)),
                            pluralStringResource(R.plurals.pl_days, left, left),
                        ),
                        style = FinanceType.bodySmall,
                        color = c.onInkSecondary,
                    )
                }
            }
            KopiImage(if (streak > 0) Kopi.TROPHY else Kopi.SLEEPING, 86.dp)
        }
        VSpace(14.dp)
        Row(Modifier.fillMaxWidth()) {
            HeroStat(streak.toString(), stringResource(R.string.streak_current), c.onInkWarning, Modifier.weight(1f))
            HeroStat(state.streak.longestStreak.toString(), stringResource(R.string.streak_record), RecordColor, Modifier.weight(1f))
            HeroStat(
                "${state.unlockedCount}",
                stringResource(R.string.ach_received),
                c.onInkBar,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = FinanceType.headline.copy(fontWeight = FontWeight.ExtraBold), color = color)
        Text(label, style = FinanceType.caption, color = FinanceTheme.colors.onInkSecondary, maxLines = 1)
    }
}

private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** Ч/б фильтр для неполученных достижений. */
private fun Modifier.grayscale(): Modifier = drawWithContent {
    val paint = Paint().apply { colorFilter = GrayscaleFilter; alpha = 0.6f }
    drawIntoCanvas { canvas ->
        canvas.saveLayer(Rect(Offset.Zero, size), paint)
        drawContent()
        canvas.restore()
    }
}

@Composable
private fun AchievementCard(a: Achievement) {
    val c = FinanceTheme.colors
    FCard(radius = 22.dp, modifier = Modifier.heightIn(min = 170.dp)) {
        Box(
            Modifier
                .size(46.dp)
                .background(if (a.isUnlocked) UnlockedIconBg else c.bg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                a.emoji,
                fontSize = 22.sp,
                modifier = if (a.isUnlocked) Modifier else Modifier.grayscale(),
            )
        }
        VSpace(10.dp)
        Text(a.title, style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        // Без weight: в сетке высота карточки не ограничена сверху, weight сжимал описание
        // до остатка от минимальной высоты, и текст обрезался. Теперь карточка растёт по содержимому.
        Text(a.description, style = FinanceType.caption, color = c.textSecondary)
        VSpace(10.dp)
        FProgressBar(progress = a.progress, color = if (a.isUnlocked) c.warning else c.primary, height = 4.dp)
        VSpace(6.dp)
        Text(
            text = if (a.isUnlocked) {
                stringResource(R.string.ach_received_one)
            } else if (a.id.isDays) {
                val v = a.value.coerceAtMost(a.id.target)
                stringResource(R.string.ach_progress_days_of, v, pluralStringResource(R.plurals.pl_days_gen, a.id.target, a.id.target))
            } else {
                stringResource(R.string.ach_progress_count, a.value.coerceAtMost(a.id.target), a.id.target)
            },
            style = FinanceType.captionBold,
            color = if (a.isUnlocked) c.warningText else c.textSecondary,
        )
    }
}
