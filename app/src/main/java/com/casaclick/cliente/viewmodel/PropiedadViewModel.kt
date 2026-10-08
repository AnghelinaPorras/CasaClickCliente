package com.casaclick.cliente.viewmodel
import androidx.lifecycle.*
import com.casaclick.cliente.di.AppContainer
import com.casaclick.cliente.data.local.entities.PropiedadEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class Filtros(val operacion: String="TODAS", val minimo: String="", val maximo: String="", val habitaciones: String="", val distrito: String="", val favoritas: Boolean=false)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PropiedadViewModel(private val c: AppContainer) : ViewModel() {
    val filtros=MutableStateFlow(Filtros())
    val mensaje=MutableStateFlow("")
    val errorFormulario=MutableStateFlow("")
    val formulario=MutableStateFlow<PropiedadEntity?>(null)
    val favoritos=c.propiedades.favoritos.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptySet())
    val todas=c.propiedades.propiedades.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val propiedades=combine(todas,filtros,favoritos) { lista,f,fav -> lista.filter {
        (f.operacion=="TODAS" || it.tipoOperacion==f.operacion) && it.precio >= (f.minimo.toDoubleOrNull() ?: 0.0) && it.precio <= (f.maximo.toDoubleOrNull() ?: Double.MAX_VALUE) &&
        (f.habitaciones.toIntOrNull()==null || it.habitaciones==f.habitaciones.toIntOrNull()) && it.distrito.contains(f.distrito,true) && (!f.favoritas || it.id in fav)
    } }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val cantidadPrecio=filtros.flatMapLatest { c.propiedades.contarPrecio(it.minimo.toDoubleOrNull() ?: 0.0,it.maximo.toDoubleOrNull() ?: Double.MAX_VALUE) }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),0)
    fun favorito(id: Long)=accion { c.propiedades.favorito(id) }
    fun cargarFormulario(id: Long) { formulario.value=null;errorFormulario.value="";viewModelScope.launch { formulario.value=if(id==0L) PropiedadEntity(titulo="",distrito="",tipoOperacion="VENTA",precio=0.0,habitaciones=1,areaM2=0.0) else c.propiedades.entidad(id) } }
    fun guardar(p: PropiedadEntity, listo: () -> Unit) { viewModelScope.launch {
        errorFormulario.value=""
        try { c.propiedades.guardar(p);mensaje.value="Propiedad guardada. Sincronización pendiente.";listo() }
        catch(e: Exception) { if(e is CancellationException) throw e;errorFormulario.value=e.message ?: "No se pudo guardar la propiedad." }
    } }
    fun eliminar(id: Long, listo: () -> Unit)=accion { c.propiedades.eliminar(id);mensaje.value="Eliminación pendiente.";listo() }
    private fun accion(block: suspend () -> Unit) { viewModelScope.launch { try { block() } catch(e: Exception) { if(e is CancellationException) throw e;mensaje.value=e.message ?: "No se pudo guardar." } } }
}
