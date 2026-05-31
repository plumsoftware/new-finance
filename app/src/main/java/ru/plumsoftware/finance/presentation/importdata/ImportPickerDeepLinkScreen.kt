package ru.plumsoftware.finance.presentation.importdata

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/** Host for `finance://app/settings/import` — shows the import picker sheet. */
@Composable
fun ImportPickerDeepLinkScreen(
    navController: NavController,
) {
    ImportPickerSheet(
        navController = navController,
        onDismiss = { navController.popBackStack() },
    )
}
