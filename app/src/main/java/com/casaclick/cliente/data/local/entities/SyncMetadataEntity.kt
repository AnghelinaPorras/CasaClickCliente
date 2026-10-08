package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(@PrimaryKey val recurso: String, val ultimaSincronizacionExitosa: Long = 0, val cursorRemoto: String = "", val mensaje: String = "")
