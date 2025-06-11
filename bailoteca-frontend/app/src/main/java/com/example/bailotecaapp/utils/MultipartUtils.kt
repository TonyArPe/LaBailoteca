package com.example.bailotecaapp.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Convierte un URI de Android a un objeto MultipartBody.Part para Retrofit.
 *
 * @param context contexto necesario para resolver el URI
 * @param uri URI del archivo seleccionado
 * @param campo nombre del campo form-data (por defecto "file")
 * @return MultipartBody.Part listo para ser enviado por Retrofit
 */
fun crearMultipartDesdeUri(context: Context, uri: Uri, campo: String = "file"): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val archivoBytes = inputStream.readBytes()
    val nombreArchivo = "imagen_${System.currentTimeMillis()}.jpg"

    val requestBody = archivoBytes.toRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(campo, nombreArchivo, requestBody)
}