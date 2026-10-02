package ru.plumsoftware.finance.ui.ds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Позы Коппи для редизайна. */
enum class Kopi(val res: Int) {
    HAPPY(R.drawable.mascot_happy),
    THINKING(R.drawable.mascot_thinking),
    TROPHY(R.drawable.mascot_trophy),
    FULL(R.drawable.mascot_full),
    SLEEPING(R.drawable.mascot_sleeping),
}

@Composable
fun KopiImage(pose: Kopi, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(pose.res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size),
    )
}

/** Совет Коппи (§7 `MascotTip`): маскот 54dp + облако, начало «Коппи:». */
@Composable
fun MascotTip(
    text: String,
    modifier: Modifier = Modifier,
    pose: Kopi = Kopi.THINKING,
    mascotSize: Dp = 54.dp,
    bubbleColor: Color = FinanceTheme.colors.surface,
) {
    val c = FinanceTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KopiImage(pose, mascotSize)
        Box(
            Modifier
                .weight(1f)
                .background(
                    bubbleColor,
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 6.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = c.warningText, fontWeight = FontWeight.SemiBold)) {
                        append(androidx.compose.ui.res.stringResource(R.string.ds_kopi_prefix))
                    }
                    append(" ")
                    append(text)
                },
                style = FinanceType.bodySmall,
                color = c.textPrimary,
            )
        }
    }
}

/** Пустое состояние (§7 `EmptyState`). */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    pose: Kopi = Kopi.THINKING,
    mascotSize: Dp = 100.dp,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = FinanceTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KopiImage(pose, mascotSize)
        Text(title, style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary, textAlign = TextAlign.Center)
        if (text != null) {
            Text(text, style = FinanceType.bodySmall, color = c.textSecondary, textAlign = TextAlign.Center)
        }
        if (action != null && onAction != null) {
            VSpace(4.dp)
            ButtonTonal(text = action, onClick = onAction)
        }
    }
}

// region MascotSnackbar (§7): toastBg, радиус 16, маскот 34dp, текст 13sp, 2.6 с, сверху под статус-баром

data class MascotMessage(val text: String, val pose: Kopi = Kopi.HAPPY, val id: Long = System.nanoTime())

@Stable
class MascotSnackbarState {
    var current by mutableStateOf<MascotMessage?>(null)
        private set

    fun show(text: String, pose: Kopi = Kopi.HAPPY) {
        current = MascotMessage(text, pose)
    }

    fun dismiss() {
        current = null
    }
}

val LocalMascotSnackbar = staticCompositionLocalOf { MascotSnackbarState() }

@Composable
fun MascotSnackbarHost(state: MascotSnackbarState, modifier: Modifier = Modifier) {
    val msg = state.current
    var visible by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf<MascotMessage?>(null) }
    LaunchedEffect(msg?.id) {
        if (msg != null) {
            shown = msg
            visible = true
            delay(2600)
            visible = false
            delay(300)
            if (state.current?.id == msg.id) state.dismiss()
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(300)) { -it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(250)) { -it } + fadeOut(tween(250)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val m = shown ?: return@AnimatedVisibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.2f))
                .background(FinanceTheme.colors.toastBg, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KopiImage(m.pose, 34.dp)
            Text(m.text, style = FinanceType.label.copy(fontWeight = FontWeight.Normal, letterSpacing = 0.sp), color = Color.White)
        }
    }
}

// endregion
