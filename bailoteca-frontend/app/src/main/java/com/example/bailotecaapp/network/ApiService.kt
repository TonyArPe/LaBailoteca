package com.example.bailotecaapp.network

import com.example.bailotecaapp.model.Clase
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Response
import com.example.bailotecaapp.model.Usuario

interface ApiService {

    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @GET("api/clases")
    suspend fun getClasesDisponibles(
        @Header("Authorization") token: String
    ): Response<List<Clase>>
}
