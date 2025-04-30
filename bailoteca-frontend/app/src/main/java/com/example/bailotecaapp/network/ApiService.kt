package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.InscripcionRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Response
import com.example.bailotecaapp.model.Usuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @GET("api/usuarios/me")
    suspend fun getUsuarioActual(
        @Header("Authorization") token: String
    ): Response<Usuario>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") authHeader: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    @GET("api/clases")
    suspend fun getClasesDisponibles(
        @Header("Authorization") token: String
    ): Response<List<Clase>>

    @GET("api/clases/{id}")
    suspend fun getClasePorId(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") claseId: Long
    ): Response<Clase>

    @POST("api/inscripciones")
    suspend fun inscribirseClase(
        @Header("Authorization") token: String,
        @Body inscripcionRequest: InscripcionRequest
    ): Response<Void>

    @GET("api/inscripciones/mias")
    suspend fun getMisInscripciones(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

    @GET("api/inscripciones/usuario/{usuarioId}")
    suspend fun getInscripcionesPorUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long
    ): Response<List<Inscripcion>>

    @DELETE("/api/inscripciones/{id}")
    suspend fun eliminarInscripcion(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

}
