package ru.plumsoftware.finance.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

object ImportFileHelper {

    private const val TAG = "ImportHelper"

    /**
     * Copy URI content to app cache IMMEDIATELY while the grant is active.
     * Call synchronously inside the launcher callback or onNewIntent,
     * never after navigation.
     */
    fun copyToCacheSync(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            val bytes = inputStream.use { it.readBytes() }

            val text = bytes.toString(Charsets.UTF_8)
            if (!text.trimStart().startsWith("{")) return null

            val file = File(
                context.cacheDir,
                "import_${System.currentTimeMillis()}.owlbackup",
            )
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            null
        }
    }

    /** Read already-copied temp file. No permissions needed. */
    fun readTempFile(absolutePath: String): String? {
        return try {
            val file = File(absolutePath)
            if (!file.exists() || !file.canRead()) return null
            file.readText()
        } catch (_: Exception) {
            null
        }
    }

    /** Delete temp file after import completes or screen is closed. */
    fun deleteTempFile(context: Context, absolutePath: String) {
        try {
            val file = File(absolutePath)
            if (file.canonicalPath.startsWith(context.cacheDir.canonicalPath)) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }
}
