package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.*
import com.example.bailotecaapp.model.dto.InscripcionRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import retrofit2.http.*
import retrofit2.Response

/**
 * Interfaz que define los endpoints disponibles en la API REST de Bailoteca.
 * Se usa con Retrofit para realizar las llamadas HTTP desde la app Android.
 */
interface ApiService {

    // USUARIOS

    /**
     * Obtiene todos los usuarios (ADMIN).
     */
    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    /**
     * Obtiene el usuario autenticado.
     */
    @GET("api/usuarios/me")
    suspend fun getUsuarioActual(
        @Header("Authorization") token: String
    ): Response<Usuario>

    /**
     * Obtiene un usuario por ID.
     */
    @GET("api/usuarios/{id}")
    suspend fun getUsuarioPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Usuario>

    /**
     * Crea un nuevo usuario.
     */
    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") authHeader: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    /**
     * Actualiza un usuario existente.
     */
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body usuario: UsuarioUpdateRequest
    ): Response<Usuario>

    @DELETE("/api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @PUT("/api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body usuario: Usuario
    ): Response<Usuario>

    // CLASES

    /**
     * Obtiene todas las clases disponibles.
     */
    @GET("api/clases")
    suspend fun getClasesDisponibles(
        @Header("Authorization") token: String
    ): Response<List<Clase>>

    /**
     * Obtiene una clase por ID.
     */
    @GET("api/clases/{id}")
    suspend fun getClasePorId(
        @Header("Authorization") token: String,
        @Path("id") claseId: Long
    ): Response<Clase>

    @GET("api/clases/publicas")
    suspend fun obtenerClases(): List<Clase>

    /**
     * Obtiene todos los eventos públicos.
     */
    @GET("/api/eventos/publicos")
    suspend fun obtenerEventos(): List<Evento>

    // INSCRIPCIONES

    /**
     * Realiza una inscripción del usuario a una clase.
     *
     * @param authorization Header con el token JWT.
     * @param request Objeto que contiene el usuario y la clase a inscribirse.
     * @return Respuesta HTTP 200 si éxito o 400/500 si hay error.
     */
    @POST("/api/inscripciones")
    suspend fun inscribirse(
        @Header("Authorization") authorization: String,
        @Body request: InscripcionRequest
    ): Response<Void>


    /**
     * Obtiene las inscripciones del usuario autenticado.
     */
    @GET("api/inscripciones/mias")
    suspend fun getMisInscripciones(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

    /**
     * Obtiene las inscripciones de un usuario por su ID.
     */
    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun getInscripcionesPorUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Inscripcion>>

    /**
     * Elimina una inscripción por ID.
     */
    @DELETE("/api/inscripciones/{id}")
    suspend fun eliminarInscripcion(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

}