package com.casaclick.cliente.viewmodel
import androidx.lifecycle.*
import com.casaclick.cliente.di.AppContainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class AuthUiState(val cargando: Boolean=false, val mensaje: String="")
class AuthViewModel(private val c: AppContainer) : ViewModel() {
    val usuario=c.auth.usuario
    val url get()=c.sesion.baseUrl
    val estado=MutableStateFlow(AuthUiState())
    fun login(correo: String, clave: String, url: String) { viewModelScope.launch {
        estado.value=AuthUiState(true)
        try { val m=c.auth.login(correo,clave,url);estado.value=AuthUiState(mensaje=m);c.programar() }
        catch(e: Exception) { if(e is CancellationException) throw e;estado.value=AuthUiState(mensaje=e.message ?: "No se pudo ingresar.") }
    } }
    fun salir() { viewModelScope.launch { c.auth.salir();estado.value=AuthUiState() } }
}
