package com.casaclick.cliente.data.repository
import com.casaclick.cliente.BuildConfig
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.local.entities.*
import com.casaclick.cliente.data.mapper.*
import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
class PropiedadRepository(private val db: AppDatabase, private val auth: AuthRepository, private val programar: () -> Unit) {
    val propiedades = db.propiedades().observar().map { lista -> lista.map { it.modelo() } }
    val favoritos = db.favoritos().observar().map { lista -> lista.map { it.propiedadId }.toSet() }
    fun contarPrecio(min: Double, max: Double) = db.propiedades().contarPrecio(min,max)
    suspend fun entidad(id: Long) = db.propiedades().porId(id)
    suspend fun favorito(id: Long) = auth.mutex.withLock {
        require(BuildConfig.ROL == "cliente"); val usuario = requireNotNull(auth.usuario.value)
        db.withTransaction { val anterior = db.favoritos().buscar(id,usuario.id)
            if (anterior == null) db.favoritos().insertar(FavoritoEntity(propiedadId=id,usuarioId=usuario.id)) else db.favoritos().eliminar(anterior)
        }
    }
    suspend fun guardar(p: PropiedadEntity) = auth.mutex.withLock {
        require(BuildConfig.ROL == "administrador" && auth.usuario.value != null) { "Solo el asesor puede guardar." }
        require(p.precio > 0 && p.areaM2 > 0 && p.habitaciones >= 0 && p.titulo.trim().length >= 3 && p.distrito.isNotBlank()) { "Completa título, distrito, precio y área mayores a 0." }
        db.withTransaction {
            val activas = db.operaciones().activas("propiedades",p.id)
            require(activas.none { it.estado != "ERROR" }) { "Primero sincroniza este registro." }
            activas.forEach { db.operaciones().actualizar(it.copy(estado="DESCARTADA")) }
            val nueva = p.copy(estadoSync="PENDIENTE")
            val local = if (p.id==0L) db.propiedades().insertar(nueva) else { db.propiedades().actualizar(nueva); p.id }
            db.operaciones().insertar(OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=local,tipoOperacion=if(p.idRemoto==null) "POST" else "PUT",payload=Gson().toJson(nueva.dto())))
        }; programar()
    }
    suspend fun eliminar(id: Long) = auth.mutex.withLock {
        require(BuildConfig.ROL == "administrador" && auth.usuario.value != null)
        db.withTransaction {
            val p = requireNotNull(db.propiedades().porId(id))
            require(db.operaciones().activas("propiedades",id).isEmpty()) { "Primero sincroniza o descarta los cambios de esta propiedad." }
            require(p.idRemoto != null) { "Descarta la creación en Sincronización." }
            db.propiedades().actualizar(p.copy(eliminado=true,estadoSync="PENDIENTE"))
            db.operaciones().insertar(OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=id,tipoOperacion="DELETE",payload=Gson().toJson(p.dto())))
        }; programar()
    }
}
