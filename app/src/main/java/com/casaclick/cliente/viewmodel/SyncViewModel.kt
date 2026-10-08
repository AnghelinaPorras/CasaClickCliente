package com.casaclick.cliente.viewmodel
import androidx.lifecycle.*
import com.casaclick.cliente.di.AppContainer
import com.casaclick.cliente.data.repository.mensajeHttp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
class SyncViewModel(private val c: AppContainer) : ViewModel() {
    val operaciones=c.sync.operaciones.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val metadata=c.sync.metadata.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val trabajando=c.sync.trabajando
    val conectado=c.conectividad.conectado.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),true)
    val mensaje=MutableStateFlow("")
    fun sincronizar()=accion { mensaje.value=if(c.sync.sincronizar()) "Sincronización terminada. Revisa pendientes y errores." else "No se pudo completar. Los cambios se conservaron." }
    fun reintentar(id: Long)=accion { c.sync.reintentar(id);c.programar();mensaje.value="Reintento programado con el mismo UUID." }
    fun descartar(id: Long)=accion { c.sync.descartar(id);mensaje.value="Cambio descartado. Se recuperó la versión remota cuando existía." }
    fun reenviar(id: Long)=accion { mensaje.value=c.sync.reenviar(id) }
    fun falla(activar: Boolean)=accion { mensaje.value=c.sync.simularFalla(activar) }
    private fun accion(block: suspend () -> Unit) { viewModelScope.launch { try { block() } catch(e: Exception) { if(e is CancellationException) throw e;mensaje.value=if(e is HttpException) mensajeHttp(e) else e.message ?: "No se pudo completar." } } }
}
