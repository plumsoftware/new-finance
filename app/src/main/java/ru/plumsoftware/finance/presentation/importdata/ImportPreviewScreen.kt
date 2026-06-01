package ru.plumsoftware.finance.presentation.importdata

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DonutSmall
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.util.ImportFileHelper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ImportState
import ru.plumsoftware.finance.domain.model.ImportStrategy
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    localFilePath: String,
    navController: NavController,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(localFilePath) {
        viewModel.parseFile(localFilePath)
    }

    DisposableEffect(localFilePath) {
        onDispose {
            if (viewModel.state.value !is ImportState.Success) {
                ImportFileHelper.deleteTempFile(context, localFilePath)
            }
        }
    }

    ImportPreviewContent(
        state = state,
        localFilePath = localFilePath,
        navController = navController,
        viewModel = viewModel,
        padding = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportPreviewContent(
    state: ImportState,
    localFilePath: String,
    navController: NavController,
    viewModel: ImportViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues?,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.import_preview_title),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = navController::popBackStack,
            )
        },
    ) { innerPadding ->
        val contentPadding = padding ?: innerPadding
        when (val current = state) {
            ImportState.Parsing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.primary)
                        Spacer(Modifier.height(Dimens.SpacingM))
                        Text(
                            text = stringResource(R.string.import_parsing),
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            is ImportState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(Dimens.SpacingL),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.error.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Spacer(Modifier.height(Dimens.SpacingL))
                    Text(
                        text = stringResource(R.string.import_error_title),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = current.message,
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Dimens.SpacingXl))
                    PrimaryButton(
                        text = stringResource(R.string.import_pick_another),
                        onClick = {
                            navController.navigate(AppRoute.Settings.route) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }

            is ImportState.Preview -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentPadding = PaddingValues(
                        top = Dimens.SpacingM,
                        bottom = Dimens.SpacingXxl,
                        start = Dimens.SpacingL,
                        end = Dimens.SpacingL,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                ) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(Dimens.SpacingM),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.InsertDriveFile,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(Dimens.IconSizeL),
                                )
                                Column(modifier = Modifier.padding(start = Dimens.SpacingM)) {
                                    Text(
                                        text = current.meta.fileName.ifBlank {
                                            stringResource(R.string.import_preview_title)
                                        },
                                        style = typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.import_meta_info,
                                            formatBackupDateTime(current.meta.exportedAt),
                                            current.meta.appVersion,
                                        ),
                                        style = typography.bodySmall,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionLabel(text = stringResource(R.string.import_contents))
                    }

                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            val counts = current.meta.recordCounts
                            val rows = listOf(
                                Triple(Icons.Rounded.Category, R.string.import_will_categories, counts.categories),
                                Triple(Icons.Rounded.Receipt, R.string.import_will_transactions, counts.transactions),
                                Triple(Icons.Rounded.Repeat, R.string.import_will_recurring, counts.recurring),
                                Triple(Icons.Rounded.Eco, R.string.import_will_assets, counts.assets),
                                Triple(Icons.Rounded.DonutSmall, R.string.import_will_limits, counts.limits),
                                Triple(Icons.Rounded.Flag, R.string.import_will_goals, counts.goals),
                            )
                            rows.forEachIndexed { index, (icon, labelRes, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.SpacingM),
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
                                    Text(
                                        text = count.toString(),
                                        style = typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary,
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
                    }

                    item {
                        SectionLabel(
                            text = stringResource(R.string.import_strategy_label),
                            modifier = Modifier.padding(top = Dimens.SpacingM),
                        )
                    }

                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            ImportStrategy.entries.forEachIndexed { index, strategy ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setStrategy(strategy) }
                                        .padding(Dimens.SpacingM),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    RadioButton(
                                        selected = current.strategy == strategy,
                                        onClick = { viewModel.setStrategy(strategy) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = colors.primary,
                                        ),
                                    )
                                    Column(modifier = Modifier.padding(start = Dimens.SpacingXs)) {
                                        Text(
                                            text = stringResource(strategy.titleRes),
                                            style = typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            text = stringResource(strategy.descRes),
                                            style = typography.bodySmall,
                                            color = colors.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (index < ImportStrategy.entries.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = Dimens.SpacingM),
                                        color = colors.surfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(Dimens.SpacingM))
                    }

                    item {
                        PrimaryButton(
                            text = stringResource(R.string.import_action),
                            onClick = { viewModel.import(current, localFilePath) },
                        )
                    }
                }
            }

            is ImportState.Importing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Dimens.SpacingXxl),
                    ) {
                        CircularProgressIndicator(
                            progress = { current.progress },
                            color = colors.primary,
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 5.dp,
                        )
                        Spacer(Modifier.height(Dimens.SpacingM))
                        Text(
                            text = "${(current.progress * 100).toInt()}%",
                            style = typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.import_in_progress),
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            is ImportState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(Dimens.SpacingL),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.secondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Spacer(Modifier.height(Dimens.SpacingL))
                    Text(
                        text = stringResource(R.string.import_success_title),
                        style = typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingM),
                    ) {
                        listOf(
                            R.string.import_added to current.added,
                            R.string.import_updated to current.updated,
                            R.string.import_skipped to current.skipped,
                        ).forEachIndexed { index, (labelRes, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.SpacingM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    style = typography.bodyMedium,
                                )
                                Text(
                                    text = count.toString(),
                                    style = typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary,
                                )
                            }
                            if (index < 2) {
                                HorizontalDivider(color = colors.surfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(Dimens.SpacingXl))
                    PrimaryButton(
                        text = stringResource(R.string.import_go_home),
                        onClick = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun formatBackupDateTime(iso: String): String {
    if (iso.isBlank()) return "—"
    return runCatching {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("ru"))
        formatter.format(instant.atZone(ZoneId.systemDefault()))
    }.getOrDefault(iso)
}
