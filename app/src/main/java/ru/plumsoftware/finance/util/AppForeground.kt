package ru.plumsoftware.finance.util

import android.app.ActivityManager
import android.content.Context

fun isAppInForeground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        ?: return false
    return activityManager.runningAppProcesses?.any { process ->
        process.processName == context.packageName &&
            process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    } == true
}
