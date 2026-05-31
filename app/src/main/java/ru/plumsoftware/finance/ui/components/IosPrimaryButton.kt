package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun IosPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    if (loading) {
        Button(
            onClick = onClick,
            enabled = false,
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.RadiusL),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                disabledContainerColor = color.copy(alpha = 0.3f),
                contentColor = Color.White,
                disabledContentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.IconSizeM),
                strokeWidth = Dimens.borderThin + 1.5.dp,
                color = Color.White,
            )
        }
    } else {
        PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            color = color,
        )
    }
}
