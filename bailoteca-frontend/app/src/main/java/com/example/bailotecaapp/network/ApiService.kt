package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.InscripcionRequest
import com.example.bailotecaapp.model.Usuario
import com.example.bailotecaapp.model.dto.UsuarioRequest
import com.example.bailotecaapp.model.dto.UsuarioUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Usuarios ---

    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @GET("api/usuarios/actual")
    suspend fun getUsuarioActual(
        @Header("Authorization") token: String
    ): Response<Usuario>

    @GET("api/usuarios/{id}")
    suspend fun getUsuarioPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Usuario>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") authHeader: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body usuario: UsuarioUpdateRequest
    ): Response<Usuario>

    @POST("api/usuarios/firebase")
    suspend fun registrarDesdeFirebase(
        @Header("Authorization") token: String,
        @Body request: UsuarioRequest
    ): Response<Usuario>

    @GET("api/usuarios/mis-alumnos")
    suspend fun getMisAlumnos(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    // --- Clases ---

    @GET("api/clases")
    suspend fun getClasesDisponibles(
        @Header("Authorization") token: String
    ): Response<List<Clase>>

    @GET("api/clases/{id}")
    suspend fun getClasePorId(
        @Header("Authorization") token: String,
        @Path("id") claseId: Long
    ): Response<Clase>

    // --- Inscripciones ---

    @POST("api/inscripciones")
    suspend fun inscribirseClase(
        @Header("Authorization") token: String,
        @Body inscripcionRequest: InscripcionRequest
    ): Response<Inscripcion>

    @GET("api/inscripciones/mias")
    suspend fun getMisInscripciones(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

    @GET("api/inscripciones/usuario")
    suspend fun getInscripcionesDelUsuario(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun getInscripcionesPorUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Inscripcion>>

    @DELETE("api/inscripciones/{id}")
    suspend fun eliminarInscripcion(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>
}