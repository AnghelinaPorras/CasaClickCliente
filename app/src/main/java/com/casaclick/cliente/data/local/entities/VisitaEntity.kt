package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "visitas", indices = [Index(value = ["uuid"], unique = true), Index(value = ["idRemoto"], unique = true)])
data class VisitaEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val idRemoto: Long? = null,
        val uuid: String = java.util.UUID.randomUUID().toString(), val version: Int = 0, val estadoSync: String = "PENDIENTE",
        val propiedadId: Long, val propiedadIdRemoto: Long, val usuarioId: Long, val fecha: String, val hora: String,
        val estado: String = "PROVISIONAL", val clienteNombre: String = "")
