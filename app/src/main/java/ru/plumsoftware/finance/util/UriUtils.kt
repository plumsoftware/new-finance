package ru.plumsoftware.finance.util

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract

fun Uri.isDownloadsProviderUri(): Boolean =
    authority == "com.android.providers.downloads.documents"

fun Uri.isMediaStoreUri(): Boolean =
    authority?.startsWith("com.android.providers.media") == true

fun Uri.requiresOpenDocument(): Boolean =
    isDownloadsProviderUri() || isMediaStoreUri()

fun downloadsInitialUri(): Uri? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DocumentsContract.buildDocumentUri(
            "com.android.providers.downloads.documents",
            "downloads",
        )
    } else {
        null
    }
