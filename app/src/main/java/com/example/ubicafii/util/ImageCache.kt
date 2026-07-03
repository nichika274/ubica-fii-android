package com.example.ubicafii.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageCache {
    private fun getCacheDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCachedFile(context: Context, imageId: String): File? {
        val file = File(getCacheDir(context), "$imageId.jpg")
        return if (file.exists()) file else null
    }

    suspend fun cacheImage(context: Context, imageUrl: String, imageId: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(getCacheDir(context), "$imageId.jpg")
                if (file.exists()) return@withContext file

                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                file
            } catch (e: Exception) {
                null
            }
        }
    }
}