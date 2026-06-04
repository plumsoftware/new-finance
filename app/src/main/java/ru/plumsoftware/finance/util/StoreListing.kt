package ru.plumsoftware.finance.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.plumsoftware.finance.AppConfig

fun openStoreListing(context: Context) {
    val url = AppConfig.storeListingUrl
    if (url.isBlank()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}
