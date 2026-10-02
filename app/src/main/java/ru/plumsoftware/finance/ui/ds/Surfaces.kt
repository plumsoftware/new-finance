package ru.plumsoftware.finance.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Горизонтальный отступ экрана (§3.3). */
val ScreenPadding = 16.dp

/** Промежуток между блоками (§3.3). */
val BlockGap = 12.dp

/** Нижний отступ корневых вкладок под навигацию (§6). */
val RootBottomInset = 124.dp

/** Белая карточка без тени (§7 `Card`). */
@Composable
fun FCard(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    color: Color = FinanceTheme.colors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(color, shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Тёмный акцентный блок (§7 `InkCard`). */
@Composable
fun InkCard(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) = FCard(
    modifier = modifier,
    radius = radius,
    padding = padding,
    color = FinanceTheme.colors.ink,
    onClick = onClick,
    content = content,
)

/** Пунктирная карточка-приглашение («+ Новая цель», «Добавить первый актив»). */
@Composable
fun DashedCard(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val color = FinanceTheme.colors.dashedBorder
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .clickable(role = Role.Button, onClick = onClick)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(radius.toPx()),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    ),
                )
            }
            .padding(padding),
        content = content,
    )
}

/** CAPS-заголовок группы (§7 `SectionHeader`). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = FinanceType.label,
        color = FinanceTheme.colors.textSecondary,
        modifier = modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp),
    )
}

/** Заголовок секции + ссылка справа (§7 `SectionTitle`). */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = FinanceType.titleSection,
            color = FinanceTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing?.invoke()
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = FinanceType.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = FinanceTheme.colors.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }
    }
}

/** App bar подэкрана: «назад» 44×44 + заголовок + необязательное действие (§6). */
@Composable
fun SubScreenAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backIcon: Int = R.drawable.ic_back,
    backDescription: String = stringResource(R.string.ds_back),
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton44(iconRes = backIcon, contentDescription = backDescription, onClick = onBack)
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            style = FinanceType.titleLarge,
            color = FinanceTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = { actions() })
        Spacer(Modifier.width(6.dp))
    }
}

/** Иконка-кнопка с зоной касания 44dp (§12). */
@Composable
fun IconButton44(
    iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    background: Color = Color.Transparent,
    tint: Color = FinanceTheme.colors.textPrimary,
    badge: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = size * 0.2f, end = size * 0.2f)
                    .size(7.dp)
                    .background(FinanceTheme.colors.danger, CircleShape),
            )
        }
    }
}

/** Цветная плитка 36dp радиус 11 с белым глифом (§3.6). */
@Composable
fun SettingsTile(iconRes: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(color, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Эмодзи на подложке цвета категории 13% (§3.1). */
@Composable
fun EmojiBadge(
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    circle: Boolean = true,
    radius: Dp = 14.dp,
    alpha: Float = 0x22 / 255f,
) {
    Box(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { }
            .background(color.copy(alpha = alpha), if (circle) CircleShape else RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.48f).sp,
        )
    }
}

/** Разделитель внутри карточки 1dp. */
@Composable
fun CardDivider(modifier: Modifier = Modifier, color: Color = FinanceTheme.colors.divider) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

/** Метка «Реклама» для нативных блоков. */
@Composable
fun AdLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.ds_ad_label),
        style = FinanceType.caption,
        color = FinanceTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun HSpace(w: Dp) = Spacer(Modifier.width(w))

/** Вертикальная колонка карточек с промежутком 12dp. */
val BlockArrangement = Arrangement.spacedBy(BlockGap)
