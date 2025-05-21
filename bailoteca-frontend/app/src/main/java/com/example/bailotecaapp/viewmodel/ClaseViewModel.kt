package com.example.bailotecaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Clase
import com.example.bailotecaapp.model.Inscripcion
import com.example.bailotecaapp.model.dto.InscripcionRequest
import com.example.bailotecaapp.network.ApiService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

/**
 * ViewModel para gestionar clases disponibles e inscripciones.
 * Encapsula toda la lógica de red relacionada con clases.
 */
@HiltViewModel
class ClaseViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    // Lista de clases disponibles
    private val _clases = MutableStateFlow<List<Clase>>(emptyList())
    val clases: StateFlow<List<Clase>> = _clases

    // Clase seleccionada (detalle)
    private val _claseSeleccionada = MutableStateFlow<Clase?>(null)
    val claseSeleccionada: StateFlow<Clase?> = _claseSeleccionada

    // Lista de inscripciones del usuario
    private val _inscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val inscripciones: StateFlow<List<Inscripcion>> = _inscripciones

    // Estado de carga y errores
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Obtiene la lista de clases disponibles desde la API para usuarios logueados.
     */
    fun obtenerClases() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token ?: return@launch
                val response = api.getClasesDisponibles("Bearer $token")

                if (response.isSuccessful) {
                    _clases.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Error ${response.code()}"
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

    /**
     * Obtiene la lista de clases públicas para usuarios invitados sin autenticación.
     */
    fun obtenerClasesPublicas() {
        viewModelScope.launch {
            try {
                val response = api.obtenerClases()
                _clases.value = response
            } catch (e: Exception) {
                Log.e("ClaseViewModel", "Error al obtener clases públicas", e)
            }
        }
    }

    /**
     * Carga los detalles de una clase específica.
     *
     * @param claseId ID de la clase a consultar.
     */
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

    /**
     * Realiza la inscripción a una clase a través del API.
     *
     * @param token JWT del usuario autenticado.
     * @param request Objeto que contiene el usuario y la clase a inscribirse.
     * @return Respuesta HTTP de la inscripción.
     */
    suspend fun inscribirseAClase(token: String, request: InscripcionRequest): Response<Void> {
        return api.inscribirse("Bearer $token", request)
    }

    /**
     * Carga las inscripciones del usuario actual desde el backend.
     */
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

    /**
     * Elimina una inscripción dada su ID.
     *
     * @param token JWT del usuario autenticado.
     * @param inscripcionId ID de la inscripción a eliminar.
     * @return Respuesta HTTP del borrado.
     */
    suspend fun eliminarInscripcion(token: String, inscripcionId: Long): Response<Void> {
        return api.eliminarInscripcion("Bearer $token", inscripcionId)
    }
}