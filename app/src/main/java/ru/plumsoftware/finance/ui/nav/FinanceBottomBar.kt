package ru.plumsoftware.finance.ui.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.ds.FitText
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Нижняя навигация (§5.1): 78dp + системная область, 5 колонок с FAB по центру.
 */
@Composable
fun FinanceBottomBar(
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    onFab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = FinanceTheme.colors
    // Отступ под системную навигацию — снаружи: сама панель всегда 78dp (§5.1),
    // иначе на устройствах с кнопками высота съедается и подписи обрезаются.
    Column(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .navigationBarsPadding(),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.navBorder))
        Row(
            Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItems.take(2).forEach { item ->
                NavCell(item, item.route == selectedRoute, onSelect, Modifier.weight(1f))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(58.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = c.primary, spotColor = c.primary.copy(alpha = 0.35f))
                        .background(c.primary, RoundedCornerShape(20.dp))
                        .clickable(role = Role.Button, onClickLabel = stringResource(R.string.add_transaction), onClick = onFab),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_transaction),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            BottomNavItems.drop(2).forEach { item ->
                NavCell(item, item.route == selectedRoute, onSelect, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NavCell(item: BottomNavItem, selected: Boolean, onSelect: (String) -> Unit, modifier: Modifier) {
    val c = FinanceTheme.colors
    val content by animateColorAsState(if (selected) c.primaryTonalText else c.navInactive, tween(200), label = "nav")
    val indicator by animateColorAsState(if (selected) c.navIndicator else Color.Transparent, tween(200), label = "navi")
    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSelect(item.route) },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(width = 56.dp, height = 30.dp)
                .background(indicator, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(item.iconRes), contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        }
        FitText(
            text = stringResource(item.titleRes),
            style = FinanceType.caption.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = content,
            modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp),
        )
    }
}
