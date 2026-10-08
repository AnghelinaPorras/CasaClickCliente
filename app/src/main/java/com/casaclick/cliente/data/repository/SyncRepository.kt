package com.casaclick.cliente.data.repository
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.local.entities.*
import com.casaclick.cliente.data.remote.RetrofitClient
import com.casaclick.cliente.data.remote.dto.*
import com.casaclick.cliente.data.mapper.*
import androidx.room.withTransaction
import com.google.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
class SyncRepository(private val db: AppDatabase, private val auth: AuthRepository) {
    val operaciones = db.operaciones().observar()
    val metadata = db.metadata().observar()
    val trabajando = MutableStateFlow(false)
    private val gson=Gson()
    private fun api() = RetrofitClient.crear(auth.sesion.baseUrl) { auth.sesion.token }
    suspend fun sincronizar(): Boolean = auth.mutex.withLock {
        if (auth.usuario.value == null) return@withLock true
        trabajando.value=true
        try {
            db.operaciones().recuperarInterrumpidas()
            // Los errores de transporte sí se reintentan; 409 requiere intervención.
            operaciones.first().filter { it.estado=="ERROR" && (it.codigoHttp==null || it.codigoHttp>=500) }.forEach { db.operaciones().actualizar(it.copy(estado="PENDIENTE")) }
            var transporte=true
            for (op in db.operaciones().pendientes()) {
                db.withTransaction { db.operaciones().actualizar(op.copy(estado="ENVIANDO",intentos=op.intentos+1)); marcar(op,"ENVIANDO") }
                try {
                    val respuesta = enviar(op)
                    db.withTransaction { aplicar(op,respuesta.cuerpo); db.operaciones().actualizar(op.copy(estado="SINCRONIZADO",intentos=op.intentos+1,mensajeError=null,codigoHttp=respuesta.codigo)) }
                } catch(e: Exception) {
                    if(e is CancellationException) throw e
                    val codigo = (e as? HttpException)?.code()
                    val mensaje = if(e is HttpException) mensajeHttp(e) else "No se pudo conectar o se agotó el tiempo de espera. Tu operación se conserva."
                    val estado = if(codigo==null || codigo>=500) "PENDIENTE" else "ERROR"
                    db.withTransaction { db.operaciones().actualizar(op.copy(estado=estado,intentos=op.intentos+1,mensajeError=mensaje,codigoHttp=codigo)); marcar(op,estado) }
                    if(codigo==null || codigo>=500 || codigo==401) { transporte=false; break }
                }
            }
            if(transporte) descargar()
            else db.metadata().insertar(SyncMetadataEntity("estado",mensaje="Servidor no disponible o sesión vencida. Revisa los errores; los datos locales se conservan."))
            transporte
        } catch(e: Exception) {
            if(e is CancellationException) throw e
            val mensaje=if(e is HttpException) mensajeHttp(e) else "No se pudo descargar. Revisa la URL y el servidor."
            db.metadata().insertar(SyncMetadataEntity("estado",mensaje=mensaje));false
        } finally { trabajando.value=false }
    }
    private data class Envio(val cuerpo: JsonObject, val codigo: Int, val duplicada: Boolean)
    private suspend fun enviar(op: OperacionPendienteEntity): Envio {
        val body=JsonParser.parseString(op.payload).asJsonObject
        val service=api()
        val respuesta = when(op.tipoOperacion) {
            "POST" -> service.crear(op.entidad,op.uuidOperacion,body)
            "PUT" -> service.editar(op.entidad,body.get("id").asLong,op.uuidOperacion,body)
            "DELETE" -> service.eliminar(op.entidad,body.get("id").asLong,op.uuidOperacion,body)
            else -> error("Operación desconocida.")
        }
        if(!respuesta.isSuccessful) throw HttpException(respuesta)
        return Envio(requireNotNull(respuesta.body()),respuesta.code(),respuesta.headers()["X-Operacion-Repetida"]=="true")
    }
    private suspend fun aplicar(op: OperacionPendienteEntity, respuesta: JsonObject) {
        if(op.entidad=="propiedades") db.propiedades().insertar(gson.fromJson(respuesta,PropiedadDto::class.java).entidad(op.idEntidadLocal))
        else {
            val anterior=requireNotNull(db.visitas().porId(op.idEntidadLocal))
            val dto=gson.fromJson(respuesta,VisitaDto::class.java)
            db.visitas().insertar(dto.copy(clienteNombre=dto.clienteNombre.ifBlank { anterior.clienteNombre }).entidad(op.idEntidadLocal,anterior.propiedadId))
        }
    }
    private suspend fun marcar(op: OperacionPendienteEntity, estado: String) {
        if(op.entidad=="propiedades") db.propiedades().porId(op.idEntidadLocal)?.let { db.propiedades().actualizar(it.copy(estadoSync=estado)) }
        else db.visitas().porId(op.idEntidadLocal)?.let { db.visitas().actualizar(it.copy(estadoSync=estado)) }
    }
    private suspend fun descargar() {
        val service=api();val propiedades=service.propiedades();val visitas=service.visitas()
        db.withTransaction {
            propiedades.forEach { dto ->
                val local=db.propiedades().porUuid(dto.uuid)
                if(local==null || local.estadoSync=="SINCRONIZADO") db.propiedades().insertar(dto.entidad(local?.id ?: 0))
            }
            val mapa=db.propiedades().todas().associateBy { it.idRemoto }
            visitas.forEach { dto ->
                val local=db.visitas().porUuid(dto.uuid);val p=mapa[dto.propiedadId]
                if(p!=null && (local==null || local.estadoSync=="SINCRONIZADO")) db.visitas().insertar(dto.entidad(local?.id ?: 0,p.id))
            }
            val ahora=System.currentTimeMillis()
            db.metadata().insertar(SyncMetadataEntity("propiedades",ahora))
            db.metadata().insertar(SyncMetadataEntity("visitas",ahora))
            db.metadata().insertar(SyncMetadataEntity("estado",ahora,mensaje="Datos actualizados. Los registros con pendientes se conservaron."))
        }
    }
    suspend fun reintentar(id: Long) = auth.mutex.withLock {
        val op=requireNotNull(db.operaciones().porId(id));require(op.estado in listOf("ERROR","PENDIENTE"))
        db.withTransaction { db.operaciones().actualizar(op.copy(estado="PENDIENTE",mensajeError=null));marcar(op,"PENDIENTE") }
    }
    suspend fun descartar(id: Long) = auth.mutex.withLock {
        val op=requireNotNull(db.operaciones().porId(id));require(op.estado in listOf("ERROR","PENDIENTE"))
        // Antes de descartar un envío incierto, consultar por UUID por si fue aceptado.
        val body=JsonParser.parseString(op.payload).asJsonObject;val uuid=body.get("uuid").asString
        val nuncaEnviado=op.tipoOperacion=="POST" && op.intentos==0
        if(op.entidad=="propiedades") {
            val remoto=if(nuncaEnviado) null else api().propiedades().find { it.uuid==uuid }
            db.withTransaction {
                if(remoto!=null) db.propiedades().insertar(remoto.entidad(op.idEntidadLocal)) else db.propiedades().porId(op.idEntidadLocal)?.let { db.propiedades().eliminar(it) }
                db.operaciones().actualizar(op.copy(estado="DESCARTADA",mensajeError="Cambio descartado por el usuario."))
            }
        } else {
            val remoto=if(nuncaEnviado) null else api().visitas().find { it.uuid==uuid }
            db.withTransaction {
                val local=db.visitas().porId(op.idEntidadLocal)
                if(remoto!=null && local!=null) db.visitas().insertar(remoto.entidad(local.id,local.propiedadId)) else if(local!=null) db.visitas().eliminar(local)
                db.operaciones().actualizar(op.copy(estado="DESCARTADA",mensajeError="Cambio descartado por el usuario."))
            }
        }
    }
    suspend fun reenviar(id: Long): String = auth.mutex.withLock {
        val op=requireNotNull(db.operaciones().porId(id));require(op.estado=="SINCRONIZADO")
        val respuesta=enviar(op)
        check(respuesta.duplicada) { "El servidor no confirmó la cabecera de operación duplicada." }
        val mensaje="Duplicada: sí · HTTP ${respuesta.codigo} · ID remoto ${respuesta.cuerpo.get("id")}: mismo UUID, sin duplicar."
        db.operaciones().actualizar(op.copy(mensajeError=mensaje,codigoHttp=respuesta.codigo))
        mensaje
    }
    suspend fun simularFalla(activar: Boolean): String = auth.mutex.withLock { api().simularFalla(if(activar) 1 else 0); if(activar) "Falla 503 activada." else "Servidor recuperado." }
}
