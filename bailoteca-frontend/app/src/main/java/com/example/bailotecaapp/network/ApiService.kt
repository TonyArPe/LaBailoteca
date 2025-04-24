package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.Clase
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
}
