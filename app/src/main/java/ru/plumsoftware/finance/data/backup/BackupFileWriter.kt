package ru.plumsoftware.finance.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import ru.plumsoftware.finance.domain.model.ExportFormat
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object BackupFileWriter {

    fun buildFileName(format: ExportFormat): String {
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH-mm"))
        return "owl_backup_${date}_$time.${format.extension}"
    }

    fun write(context: Context, json: String, format: ExportFormat): Uri {
        val fileName = buildFileName(format)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToDownloads(context, fileName, json)
        } else {
            writeToCache(context, fileName, json)
        }
    }

    private fun writeToDownloads(context: Context, fileName: String, json: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create backup file")
        resolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    private fun writeToCache(context: Context, fileName: String, json: String): Uri {
        val dir = context.getExternalFilesDir(null) ?: context.cacheDir
        val file = File(dir, fileName)
        file.writeText(json)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
