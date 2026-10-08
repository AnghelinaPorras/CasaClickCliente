package com.casaclick.cliente.data.repository
import com.casaclick.cliente.BuildConfig
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.local.entities.*
import com.casaclick.cliente.data.mapper.*
import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import java.time.*
class VisitaRepository(private val db: AppDatabase, private val auth: AuthRepository, private val programar: () -> Unit) {
    val visitas = db.visitas().observar().map { lista -> lista.map { it.modelo() } }
    suspend fun guardar(propiedadId: Long, fecha: String, hora: String, visitaId: Long = 0) = auth.mutex.withLock {
        require(BuildConfig.ROL == "cliente") { "Solo los clientes agendan visitas." }; val usuario = requireNotNull(auth.usuario.value)
        val instante = try { LocalDateTime.of(LocalDate.parse(fecha),LocalTime.parse(hora)) } catch (_: Exception) { error("Usa fecha AAAA-MM-DD y hora HH:MM válidas.") }
        require(instante.isAfter(LocalDateTime.now(ZoneId.of("America/Lima")))) { "Elige una fecha y hora futuras." }
        db.withTransaction {
            val propiedad = requireNotNull(db.propiedades().porId(propiedadId));require(!propiedad.eliminado)
            val anterior = if (visitaId==0L) null else requireNotNull(db.visitas().porId(visitaId))
            val ops = db.operaciones().activas("visitas",visitaId)
            require(ops.none { it.estado != "ERROR" }) { "Sincroniza antes de volver a editar." }
            ops.forEach { db.operaciones().actualizar(it.copy(estado="DESCARTADA")) }
            val nueva = anterior?.copy(fecha=fecha,hora=hora,estado="PROVISIONAL",estadoSync="PENDIENTE") ?: VisitaEntity(propiedadId=propiedadId,propiedadIdRemoto=requireNotNull(propiedad.idRemoto),usuarioId=usuario.id,fecha=fecha,hora=hora,clienteNombre=usuario.nombre)
            val id = if (anterior==null) db.visitas().insertar(nueva) else { db.visitas().actualizar(nueva); nueva.id }
            db.operaciones().insertar(OperacionPendienteEntity(entidad="visitas",idEntidadLocal=id,tipoOperacion=if(nueva.idRemoto==null) "POST" else "PUT",payload=Gson().toJson(nueva.dto())))
        }; programar()
    }
    suspend fun cambiarEstado(id: Long, estado: String) = auth.mutex.withLock {
        val usuario = requireNotNull(auth.usuario.value)
        db.withTransaction {
            val v = requireNotNull(db.visitas().porId(id)); require(v.idRemoto != null) { "La visita aún es provisional. Descártala en Sincronización." }
            require(BuildConfig.ROL == "administrador" || (v.usuarioId==usuario.id && estado=="CANCELADA")) { "Acción no permitida." }
            require(db.operaciones().activas("visitas",id).isEmpty()) { "Sincroniza o descarta el cambio anterior." }
            val nueva=v.copy(estado=estado,estadoSync="PENDIENTE");db.visitas().actualizar(nueva)
            db.operaciones().insertar(OperacionPendienteEntity(entidad="visitas",idEntidadLocal=id,tipoOperacion=if(BuildConfig.ROL=="cliente") "DELETE" else "PUT",payload=Gson().toJson(nueva.dto())))
        }; programar()
    }
}
