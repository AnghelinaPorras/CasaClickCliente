package com.casaclick.cliente.data.repository
import com.casaclick.cliente.BuildConfig
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.local.entities.UsuarioEntity
import com.casaclick.cliente.data.remote.RetrofitClient
import com.casaclick.cliente.data.remote.dto.*
import com.casaclick.cliente.util.SessionStore
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
class AuthRepository(private val db: AppDatabase, val sesion: SessionStore, val mutex: Mutex) {
    val usuario = MutableStateFlow(if (sesion.activa) sesion.perfil else null)
    suspend fun login(correo: String, clave: String, urlEntrada: String): String = withContext(Dispatchers.IO) { mutex.withLock {
        require(correo.isNotBlank() && clave.isNotBlank()) { "Completa correo y clave." }
        val url = urlEntrada.trim().let { if (it.endsWith('/')) it else "$it/" }
        require(url.startsWith("http://") || url.startsWith("https://")) { "La URL debe empezar por http:// o https://." }
        try {
            val respuesta = RetrofitClient.crear(url) { null }.login(LoginRequest(correo.trim().lowercase(), clave, BuildConfig.ROL))
            require(respuesta.usuario.rol == BuildConfig.ROL) { "Rol incorrecto." }
            if (sesion.perfil?.id != respuesta.usuario.id || sesion.baseUrl != url) {
                require(db.operaciones().cantidadActivas() == 0) { "Sincroniza o descarta tus pendientes antes de cambiar de cuenta o servidor." }
                db.clearAllTables()
            }
            val u = respuesta.usuario
            db.usuarios().insertar(UsuarioEntity(u.id,u.correo,u.nombre,u.rol))
            sesion.guardar(u,respuesta.token,clave,url); usuario.value = u
            "Sesión online iniciada."
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sinServidor = e is IOException || (e is HttpException && e.code() >= 500)
            if (sinServidor && sesion.verificarOffline(correo.trim().lowercase(),clave,url) && sesion.perfil?.rol == BuildConfig.ROL) {
                sesion.activar(); usuario.value = sesion.perfil; "Acceso offline. Consulta tus datos guardados y conserva tus operaciones pendientes."
            } else if (e is HttpException) throw IllegalStateException(mensajeHttp(e))
            else if (sinServidor) throw IllegalStateException("No se pudo conectar. El primer ingreso debe ser online; después podrás entrar offline con la misma cuenta.")
            else throw e
        }
    } }
    suspend fun salir() = mutex.withLock { sesion.salir(); usuario.value = null }
}
fun mensajeHttp(e: HttpException): String = try {
    com.google.gson.JsonParser.parseString(e.response()?.errorBody()?.string()).asJsonObject.get("mensaje").asString
} catch (_: Exception) { "Error HTTP ${e.code()}. Reintenta cuando el servidor esté disponible." }
