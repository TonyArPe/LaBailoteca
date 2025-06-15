package com.example.bailotecaapp.utils

/**
 * Construye la URL completa para acceder a un archivo estático del backend (como imágenes),
 * añadiendo un parámetro de timestamp al final para evitar problemas de caché.
 *
 * Si el nombre ya es una URL absoluta (empieza por http), se devuelve tal cual.
 * Si es nulo, devuelve null.
 *
 * @param fileName nombre del archivo (ej: "foto123.jpg") o URL absoluta
 * @param baseUrl URL base del backend, terminada o no en "/"
 * @return URL completa para mostrar la imagen (ej: https://host.ngrok.io/media/foto123.jpg?t=12345)
 */
fun construirUrlMedia(fileName: String?, baseUrl: String): String? {
    return fileName?.let {
        if (it.startsWith("http")) it
        else {
            val cleanBase = baseUrl.removeSuffix("/")
            val cleanFile = it.removePrefix("/") // 🔧 evita que // cause bloqueo en backend
            "$cleanBase/media/$cleanFile?t=${System.currentTimeMillis()}"
        }
    }
}