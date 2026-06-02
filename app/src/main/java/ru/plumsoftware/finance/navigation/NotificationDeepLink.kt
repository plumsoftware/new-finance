package ru.plumsoftware.finance.navigation

import android.net.Uri
import ru.plumsoftware.finance.domain.model.AppNotification

fun AppNotification.deepLinkUri(): Uri? = parseNotificationDeepLink(data)
