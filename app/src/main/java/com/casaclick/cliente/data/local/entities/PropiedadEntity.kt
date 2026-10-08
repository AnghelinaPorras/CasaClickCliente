package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "propiedades", indices = [Index(value = ["uuid"], unique = true), Index(value = ["idRemoto"], unique = true)])
data class PropiedadEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val idRemoto: Long? = null,
        val uuid: String = java.util.UUID.randomUUID().toString(), val version: Int = 0, val estadoSync: String = "PENDIENTE",
        val titulo: String, val distrito: String, val tipoOperacion: String, val precio: Double,
        val habitaciones: Int, val areaM2: Double, val descripcion: String = "", val imagenesJson: String = "[]", val eliminado: Boolean = false)
