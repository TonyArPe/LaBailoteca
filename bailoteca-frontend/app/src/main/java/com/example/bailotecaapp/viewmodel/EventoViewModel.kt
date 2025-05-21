package com.example.bailotecaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bailotecaapp.model.Evento
import com.example.bailotecaapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para cargar eventos públicos que los usuarios invitados pueden visualizar.
 */
@HiltViewModel
class EventoViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    /**
     * Obtiene la lista de eventos públicos desde la API.
     */
    fun obtenerEventosPublicos() {
        viewModelScope.launch {
            try {
                val resultado = apiService.obtenerEventos()
                _eventos.value = resultado
            } catch (e: Exception) {
                // Ignoramos errores y dejamos la lista vacía
            }
        }
    }
}