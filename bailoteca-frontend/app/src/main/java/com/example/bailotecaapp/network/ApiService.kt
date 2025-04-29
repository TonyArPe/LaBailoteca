package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.InscripcionRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Response
import com.example.bailotecaapp.model.Usuario
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Header("Authorization") authHeader: String,
        @Body usuario: Usuario
    ): Response<Usuario>

    @GET("api/clases")
    suspend fun getClasesDisponibles(
        @Header("Authorization") token: String
    ): Response<List<Clase>>

    @GET("api/usuarios/me")
    suspend fun getUsuarioActual(
        @Header("Authorization") token: String
    ): Response<Usuario>

    @POST("api/inscripciones")
    suspend fun inscribirseClase(
        @Header("Authorization") token: String,
        @Body inscripcionRequest: InscripcionRequest
    ): Response<Void>

    @GET("api/clases/{id}")
    suspend fun getClasePorId(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") claseId: Long
    ): Response<Clase>

    @GET("api/inscripciones/mias")
    suspend fun getMisInscripciones(
        @Header("Authorization") token: String
    ): Response<List<Inscripcion>>

}
