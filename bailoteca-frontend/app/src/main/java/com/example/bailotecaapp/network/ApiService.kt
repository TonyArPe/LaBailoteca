package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.*
import com.example.bailotecaapp.model.dto.ClaseRequest
import com.example.bailotecaapp.model.dto.InscripcionRequest
import com.example.bailotecaapp.model.dto.UsuarioEstadoUpdateRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*
import retrofit2.Response

/**
 * Interfaz de comunicación con la API REST de Bailoteca.
 * Define todos los endpoints accesibles desde la app Android.
 */
interface ApiService {

    // --------------------------- USUARIO Y SESIÓN ---------------------------

    /**
     * Obtiene el usuario actualmente autenticado.
     */
    @GET("/api/usuarios/me")
    suspend fun obtenerUsuarioActual(@Header("Authorization") token: String): Usuario

    /**
     * Devuelve todos los usuarios (solo para ADMIN).
     */
    @GET("api/usuarios")
    suspend fun getUsuarios(@Header("Authorization") token: String): Response<List<Usuario>>

    /**
     * Obtiene un usuario por su ID.
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

    @POST("api/usuarios")
    suspend fun crearUsuarioInvitado(
        @Body usuario: Usuario
    ): Response<Usuario>

    /**
     * Actualiza los datos de un usuario existente.
     */
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body usuario: UsuarioUpdateRequest
    ): Response<Usuario>

    /**
     * Elimina un usuario del sistema.
     */
    @DELETE("/api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    /**
     * Cambia el estado de pago de un usuario.
     */
    @PUT("/api/usuarios/{id}/estado")
    suspend fun actualizarEstadoUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: UsuarioEstadoUpdateRequest
    ): Response<Usuario>

    // --------------------------- CLASES ---------------------------

    /**
     * Lista todas las clases accesibles para el usuario autenticado.
     */
    @GET("api/clases")
    suspend fun getClasesDisponibles(@Header("Authorization") token: String): Response<List<Clase>>

    /**
     * Detalle de una clase por su ID.
     */
    @GET("api/clases/{id}")
    suspend fun getClasePorId(
        @Header("Authorization") token: String,
        @Path("id") claseId: Long
    ): Response<Clase>

    /**
     * Lista las clases públicas (accesibles sin login).
     */
    @GET("api/clases/publicas")
    suspend fun obtenerClases(): List<Clase>

    /**
     * Devuelve todos los alumnos de una clase según el profesor.
     */
    @GET("usuarios/profesor/{claseId}/alumnos")
    suspend fun obtenerAlumnosPorProfesor(
        @Path("claseId") claseId: Long,
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    /**
     * Crea una nueva clase.
     */
    @POST("/api/clases")
    suspend fun crearClase(
        @Header("Authorization") token: String,
        @Body request: ClaseRequest
    ): Response<Void>

    /**
     * Actualiza una clase existente.
     */
    @PUT("/api/clases/{id}")
    suspend fun actualizarClase(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: ClaseRequest
    ): Response<Void>

    /**
     * Elimina una clase del sistema.
     */
    @DELETE("/api/clases/{id}")
    suspend fun eliminarClase(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @GET("/api/clases/{id}/alumnos")
    suspend fun obtenerAlumnos(
        @Header("Authorization") token: String,
        @Path("id") claseId: Long
    ): Response<List<Usuario>>

    // --------------------------- INSCRIPCIONES ---------------------------

    /**
     * Inscribe al usuario en una clase específica.
     */
    @POST("/api/inscripciones")
    suspend fun inscribirse(
        @Header("Authorization") authorization: String,
        @Body request: InscripcionRequest
    ): Response<Void>

    /**
     * Devuelve inscripciones de un profesor.
     */
    @GET("/api/inscripciones/profesor/{profesorId}")
    suspend fun getInscripcionesProfesor(
        @Header("Authorization") token: String,
        @Path("profesorId") profesorId: Long
    ): Response<List<Inscripcion>>

    /**
     * Devuelve las inscripciones del usuario autenticado.
     */
    @GET("api/inscripciones/mias")
    suspend fun getMisInscripciones(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

    /**
     * Lista inscripciones por ID de usuario.
     */
    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun getInscripcionesPorUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Inscripcion>>

    /**
     * Elimina una inscripción existente.
     */
    @DELETE("/api/inscripciones/{id}")
    suspend fun eliminarInscripcion(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    // --------------------------- EVENTOS ---------------------------

    /**
     * Eventos públicos disponibles para invitados.
     */
    @GET("/api/eventos/publicos")
    suspend fun obtenerEventos(): List<Evento>

    /**
     * Eventos accesibles para usuario autenticado (ADMIN o PROFESOR).
     */
    @GET("/api/eventos")
    suspend fun getEventosPrivados(
        @Header("Authorization") token: String
    ): Response<List<Evento>>

    /**
     * Crea un nuevo evento.
     */
    @POST("/api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Body evento: Evento
    ): Response<Evento>

    /**
     * Actualiza un evento existente por ID.
     */
    @PUT("/api/eventos/{id}")
    suspend fun actualizarEvento(
        @Header("Authorization") token: String,
        @Path("id") eventoId: Long,
        @Body evento: Evento
    ): Response<Evento>

    /**
     * Elimina un evento por ID.
     */
    @DELETE("/api/eventos/{id}")
    suspend fun eliminarEvento(
        @Header("Authorization") token: String,
        @Path("id") eventoId: Long
    ): Response<Void>

    /**
     * Obtiene los detalles de un evento por ID.
     */
    @GET("/api/eventos/{id}")
    suspend fun getEventoPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Evento

    /**
     * Devuelve los eventos relacionados con un usuario específico.
     */
    @GET("/api/eventos/usuario/{usuarioId}")
    suspend fun getEventosUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Evento>>

    /**
     * Devuelve los eventos organizados por un profesor específico.
     */
    @GET("/api/eventos/profesor/{profesorId}")
    suspend fun getEventosProfesor(
        @Header("Authorization") token: String,
        @Path("profesorId") profesorId: Long
    ): Response<List<Evento>>


    //------------------------ SUBIDA FICHEROS -------------------------
    @Multipart
    @POST("/api/uploads/upload")
    suspend fun subirArchivo(
        @Part archivo: MultipartBody.Part
    ): Response<ResponseBody>

}