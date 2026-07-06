package com.example.ubicafii.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageCache {
    private fun getCacheDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCachedFile(context: Context, imageId: String): File? {
        val file = File(getCacheDir(context), "$imageId.jpg")
        return if (file.exists() && file.length() > 0) file else null
    }

    suspend fun cacheImage(context: Context, imageUrl: String, imageId: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(getCacheDir(context), "$imageId.jpg")
                // Si ya existe y no está vacío, no lo vuelve a descargar
                if (file.exists() && file.length() > 0) return@withContext file

                // Usamos el motor de Coil para descargar de forma segura (soporta http y https sin caerse)
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false) // Permite manejar los bytes de la imagen en hilos secundarios
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    // Convertimos el resultado a un array de bytes y lo guardamos físicamente
                    val drawable = result.drawable
                    val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        FileOutputStream(file).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        return@withContext file
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}