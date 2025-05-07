package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.InscripcionRequest
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Response

/**
 * ViewModel para gestionar clases disponibles e inscripciones.
 * Usa Hilt para inyectar ApiService y encapsula la lógica de red.
 */
@HiltViewModel
class ClaseViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _clases = MutableStateFlow<List<Clase>>(emptyList())
    val clases: StateFlow<List<Clase>> = _clases

    private val _claseSeleccionada = MutableStateFlow<Clase?>(null)
    val claseSeleccionada: StateFlow<Clase?> = _claseSeleccionada

    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun obtenerClases() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val response = api.getClasesDisponibles("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _clases.value = response.body()!!
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                    Log.e("ClaseViewModel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
                Log.e("ClaseViewModel", "Excepción: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarClase(claseId: Long) {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = api.getClasePorId("Bearer $token", claseId)
                if (response.isSuccessful) {
                    _claseSeleccionada.value = response.body()
                } else {
                    Log.e("ClaseViewModel", "Error al obtener clase: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ClaseViewModel", "Excepción: ${e.localizedMessage}")
            }
        }
    }

    suspend fun inscribirseAClase(token: String, request: InscripcionRequest): Response<Inscripcion> {
        return api.inscribirseClase("Bearer $token", request)
    }

    fun cargarMisInscripciones() {
        viewModelScope.launch {
            try {
                val token = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val response = api.getMisInscripciones("Bearer $token")
                if (response.isSuccessful) {
                    _inscripciones.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ClaseViewModel", "Error al cargar inscripciones: ${e.message}")
            }
        }
    }

    suspend fun eliminarInscripcion(token: String, inscripcionId: Long): Response<Void> {
        return api.eliminarInscripcion("Bearer $token", inscripcionId)
    }

}