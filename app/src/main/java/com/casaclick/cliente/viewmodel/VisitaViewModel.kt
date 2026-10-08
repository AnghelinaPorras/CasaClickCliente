package com.casaclick.cliente.viewmodel
import androidx.lifecycle.*
import com.casaclick.cliente.di.AppContainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
class VisitaViewModel(private val c: AppContainer) : ViewModel() {
    val mensaje=MutableStateFlow("")
    val fechaFiltro=MutableStateFlow("")
    val todas=c.visitas.visitas.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val visitas=combine(todas,fechaFiltro) { lista,fecha -> lista.filter { fecha.isBlank() || it.fecha==fecha } }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    fun agendar(propiedad: Long, fecha: String, hora: String, id: Long=0, listo: () -> Unit)=accion { c.visitas.guardar(propiedad,fecha,hora,id);mensaje.value="Visita provisional guardada. Será confirmada al sincronizar.";listo() }
    fun estado(id: Long, estado: String)=accion { c.visitas.cambiarEstado(id,estado);mensaje.value="Cambio guardado. Sincronización pendiente." }
    private fun accion(block: suspend () -> Unit) { viewModelScope.launch { try { block() } catch(e: Exception) { if(e is CancellationException) throw e;mensaje.value=e.message ?: "No se pudo guardar." } } }
}
