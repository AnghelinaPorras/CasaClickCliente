package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "favoritos", indices = [Index(value = ["propiedadId", "usuarioId"], unique = true)])
data class FavoritoEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val propiedadId: Long, val usuarioId: Long)
