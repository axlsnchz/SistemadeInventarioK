package mx.edu.utez.sistemadeinventario.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    /** Crea un archivo vacío donde la cámara va a guardar la foto (carpeta cache/images). */
    fun createTempImageFile(context: Context): File {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val carpeta = File(context.cacheDir, "images").apply { mkdirs() }
        return File.createTempFile("IMG_${fecha}_", ".jpg", carpeta)
    }

    /** Convierte el archivo en una Uri segura para pasársela a la cámara (usa el FileProvider del manifest). */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Convierte la foto en la parte "multipart" que pide ApiService.subirFoto().
     * Lee el archivo, así que llamarlo fuera del hilo principal (el repositorio lo hace en IO).
     * [partName] tiene que llamarse igual que el @RequestParam del backend ("image").
     */
    fun uriToMultipart(context: Context, uri: Uri, partName: String = "image"): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("No se pudo leer la foto")
        val cuerpo = bytes.toRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(partName, "foto.jpg", cuerpo)
    }
}
