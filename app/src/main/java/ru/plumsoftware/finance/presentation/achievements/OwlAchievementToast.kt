package ru.plumsoftware.finance.presentation.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.components.MascotImage
import ru.plumsoftware.finance.ui.theme.MascotEmotion
import ru.plumsoftware.finance.ui.theme.MascotSize

@Composable
fun OwlAchievementToast(
    achievement: Achievement,
    onDismiss: () -> Unit,
) {
    var visible by remember(achievement.key) { mutableStateOf(false) }
    var dismissed by remember(achievement.key) { mutableStateOf(false) }
    var dragOffset by remember(achievement.key) { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 48.dp.toPx() }

    fun dismissAnimated() {
        if (dismissed) return
        dismissed = true
        dragOffset = 0f
        visible = false
        scope.launch {
            delay(300)
            onDismiss()
        }
    }

    LaunchedEffect(achievement.key) {
        visible = true
        delay(4500)
        if (!dismissed) {
            dismissAnimated()
        }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(achievement.key) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300),
        ) + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = Color(0xFFFFD700).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp),
                )
                .pointerInput(achievement.key) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (-dragOffset > dismissThresholdPx) {
                                dismissAnimated()
                            } else {
                                dragOffset = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffset = (dragOffset + dragAmount).coerceAtMost(0f)
                        },
                    )
                }
                .padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MascotImage(
                    emotion = MascotEmotion.EXCITED,
                    modifier = Modifier.size(MascotSize.Medium),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.achievement_new),
                        fontSize = 11.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                    )
                    Text(
                        achievement.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        achievement.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(achievement.emoji, fontSize = 30.sp)
            }
        }
    }
}
