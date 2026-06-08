package ru.plumsoftware.finance.presentation.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.StreakData
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.MascotImage
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.MascotAssets
import ru.plumsoftware.finance.ui.theme.MascotEmotion
import ru.plumsoftware.finance.ui.theme.MascotSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.achievements_title),
                backLabel = stringResource(R.string.nav_home),
                onBack = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            item { OwlAchievementsHero(streak = state.streak) }
            if (state.unlocked.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.achievements_unlocked_section), modifier = Modifier.padding(start = 0.dp))
                }
                item { AchievementsGrid(achievements = state.unlocked, unlocked = true) }
            }
            if (state.locked.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.achievements_locked_section), modifier = Modifier.padding(start = 0.dp))
                }
                item { AchievementsGrid(achievements = state.locked, unlocked = false) }
            }
            if (state.unlocked.isEmpty() && state.locked.isEmpty()) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.fullBody,
                        title = stringResource(R.string.empty_achievements_title),
                        subtitle = stringResource(R.string.empty_achievements_subtitle),
                        mascotPhrase = stringResource(R.string.mascot_phrase_start),
                    )
                }
            }
        }
    }
}

@Composable
private fun OwlAchievementsHero(streak: StreakData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusXl))
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimens.SpacingL),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    owlPhrase(streak.currentStreak),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    owlSubphrase(streak.currentStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                )
                Spacer(Modifier.height(Dimens.SpacingM))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingL)) {
                    HeroStat("${streak.currentStreak}", stringResource(R.string.streak_current), Color(0xFFFF3B30))
                    HeroStat("${streak.longestStreak}", stringResource(R.string.streak_record), Color(0xFFFF9500))
                    HeroStat("${streak.totalUnlocked}", stringResource(R.string.achievements_title), IosBlue)
                }
            }
            MascotImage(
                emotion = when {
                    streak.currentStreak >= 30 -> MascotEmotion.PARTY
                    streak.currentStreak >= 7 -> MascotEmotion.EXCITED
                    streak.currentStreak == 0 -> MascotEmotion.SLEEPING
                    else -> MascotEmotion.HAPPY
                },
                modifier = Modifier
                    .size(MascotSize.Hero)
                    .padding(start = Dimens.SpacingS),
            )
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color) {
    Column {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@Composable
private fun owlPhrase(streak: Int): String = when {
    streak == 0 -> stringResource(R.string.owl_phrase_streak_0)
    streak < 7 -> stringResource(R.string.owl_phrase_streak_under_7)
    streak < 30 -> stringResource(R.string.owl_phrase_streak_under_30)
    streak < 100 -> stringResource(R.string.owl_phrase_streak_under_100)
    else -> stringResource(R.string.owl_phrase_streak_100_plus)
}

@Composable
private fun owlSubphrase(streak: Int): String = when {
    streak == 0 -> stringResource(R.string.owl_subphrase_streak_0)
    streak < 7 -> stringResource(R.string.owl_subphrase_to_weekly_streak, 7 - streak)
    streak < 30 -> stringResource(R.string.owl_subphrase_to_monthly_streak, 30 - streak)
    else -> stringResource(R.string.owl_subphrase_keep_going)
}

@Composable
private fun AchievementsGrid(
    achievements: List<Achievement>,
    unlocked: Boolean,
) {
    val rows = achievements.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
        rows.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            ) {
                pair.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = unlocked,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusM))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (unlocked) Modifier.border(
                    width = Dimens.borderThin,
                    color = Color(0xFFFFD700).copy(alpha = 0.25f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusM),
                ) else Modifier,
            )
            .padding(Dimens.SpacingS),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (unlocked) Color(0xFFFFD700).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unlocked) achievement.emoji else "🔒",
                    fontSize = 22.sp,
                    modifier = if (!unlocked) Modifier.alpha(0.35f) else Modifier,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                achievement.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
            )
            if (!unlocked && achievement.progress != null) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = IosBlue,
                    trackColor = Color(0xFFE5E5EA),
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    achievement.progressLabel ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = IosBlue,
                )
            }
            if (unlocked && achievement.unlockedAtMillis != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    SimpleDateFormat("d MMM yyyy", Locale("ru"))
                        .format(Date(achievement.unlockedAtMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    }
}
