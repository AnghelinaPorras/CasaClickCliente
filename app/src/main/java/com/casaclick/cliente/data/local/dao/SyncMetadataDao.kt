package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.SyncMetadataEntity
        @Dao interface SyncMetadataDao {
            @Upsert suspend fun insertar(registro: SyncMetadataEntity): Long
            @Update suspend fun actualizar(registro: SyncMetadataEntity)
            @Delete suspend fun eliminar(registro: SyncMetadataEntity)
            @Query("SELECT * FROM sync_metadata") fun observar(): Flow<List<SyncMetadataEntity>>
        }
