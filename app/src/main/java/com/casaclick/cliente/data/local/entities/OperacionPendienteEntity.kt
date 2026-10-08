package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "operaciones_pendientes", indices = [Index(value = ["uuidOperacion"], unique = true)])
data class OperacionPendienteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0,
        val uuidOperacion: String = java.util.UUID.randomUUID().toString(), val entidad: String, val idEntidadLocal: Long,
        val tipoOperacion: String, val payload: String, val fechaRegistro: Long = System.currentTimeMillis(),
        val estado: String = "PENDIENTE", val intentos: Int = 0, val mensajeError: String? = null, val codigoHttp: Int? = null)
