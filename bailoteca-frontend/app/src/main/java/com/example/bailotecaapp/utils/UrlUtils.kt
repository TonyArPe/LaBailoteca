package com.example.bailotecaapp.utils

/**
 * Construye la URL completa para acceder a un archivo estático del backend (como imágenes).
 *
 * Si el nombre ya es una URL absoluta, se devuelve tal cual.
 * Si es nulo, devuelve null.
 * Si es un nombre de archivo, se añade el prefijo /media/ a la base URL.
 *
 * @param fileName nombre del archivo devuelto por el backend (ej: "foto123.jpg")
 * @param baseUrl URL base del backend, terminada o no en "/"
 * @return URL completa para mostrar la imagen (ej: https://host.ngrok.io/media/foto123.jpg)
 */
fun construirUrlMedia(fileName: String?, baseUrl: String): String? {
    return fileName?.let {
        if (it.startsWith("http")) it
        else {
            val cleanBase = baseUrl.removeSuffix("/")
            "$cleanBase/media/$it"
        }
    }
}