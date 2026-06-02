package ru.plumsoftware.finance.presentation.importdata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DonutSmall
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.util.ImportFileHelper
import ru.plumsoftware.finance.util.downloadsInitialUri
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.theme.Dimens

private val ImportPurple = Color(0xFF5856D6)
private val ImportWarning = Color(0xFFFF9500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPickerSheet(
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val downloadsInitialUri = remember { downloadsInitialUri() }
    var isProcessing by remember { mutableStateOf(false) }
    var copyError by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(
        OpenDocumentWithInitialUri(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        isProcessing = true
        val tempPath = ImportFileHelper.copyToCacheSync(context, uri)
        isProcessing = false

        if (tempPath != null) {
            onDismiss()
            navController.navigate(AppRoute.ImportPreview.route(tempPath))
        } else {
            copyError = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = RoundedCornerShape(
            topStart = Dimens.RadiusXl,
            topEnd = Dimens.RadiusXl,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.SpacingL)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusXl))
                    .background(ImportPurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileUpload,
                    contentDescription = null,
                    tint = ImportPurple,
                    modifier = Modifier.size(Dimens.IconSizeL),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            Text(
                text = stringResource(R.string.import_sheet_title),
                style = typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                text = stringResource(R.string.import_sheet_desc),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimens.SpacingXl))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                val rows = listOf(
                    Icons.Rounded.Category to R.string.import_will_categories,
                    Icons.Rounded.Receipt to R.string.import_will_transactions,
                    Icons.Rounded.Repeat to R.string.import_will_recurring,
                    Icons.Rounded.Eco to R.string.import_will_assets,
                    Icons.Rounded.DonutSmall to R.string.import_will_limits,
                    Icons.Rounded.Flag to R.string.import_will_goals,
                    Icons.Rounded.EmojiEvents to R.string.import_will_achievements,
                )
                rows.forEachIndexed { index, (icon, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Dimens.SpacingM,
                                vertical = Dimens.SpacingS,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(Dimens.IconSizeM),
                        )
                        Text(
                            text = stringResource(labelRes),
                            style = typography.bodyMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Dimens.SpacingM),
                        )
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(Dimens.IconSizeS),
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 40.dp),
                            color = colors.surfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.RadiusM))
                    .background(ImportWarning.copy(alpha = 0.08f))
                    .padding(Dimens.SpacingM),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = ImportWarning,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
                Text(
                    text = stringResource(R.string.import_warning),
                    style = typography.bodySmall,
                    color = ImportWarning,
                    modifier = Modifier.padding(start = Dimens.SpacingS),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingXl))

            if (copyError) {
                Text(
                    text = stringResource(R.string.import_error_cannot_open),
                    style = typography.bodySmall,
                    color = colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.SpacingM),
                )
            }

            PrimaryButton(
                text = stringResource(R.string.import_pick_file),
                enabled = !isProcessing,
                onClick = {
                    copyError = false
                    fileLauncher.launch(downloadsInitialUri)
                },
            )
            Spacer(Modifier.height(Dimens.SpacingS))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Dimens.SpacingM))
        }
    }
}
