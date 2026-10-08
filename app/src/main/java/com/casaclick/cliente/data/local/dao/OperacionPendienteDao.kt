package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.OperacionPendienteEntity
        @Dao interface OperacionPendienteDao {
            @Upsert suspend fun insertar(registro: OperacionPendienteEntity): Long
            @Update suspend fun actualizar(registro: OperacionPendienteEntity)
            @Delete suspend fun eliminar(registro: OperacionPendienteEntity)
            @Query("SELECT * FROM operaciones_pendientes ORDER BY fechaRegistro DESC,id DESC") fun observar(): Flow<List<OperacionPendienteEntity>>
        @Query("SELECT * FROM operaciones_pendientes WHERE estado='PENDIENTE' ORDER BY id") suspend fun pendientes(): List<OperacionPendienteEntity>
        @Query("SELECT * FROM operaciones_pendientes WHERE id=:id") suspend fun porId(id: Long): OperacionPendienteEntity?
        @Query("SELECT * FROM operaciones_pendientes WHERE entidad=:entidad AND idEntidadLocal=:id AND estado IN ('PENDIENTE','ENVIANDO','ERROR')") suspend fun activas(entidad: String, id: Long): List<OperacionPendienteEntity>
        @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado IN ('PENDIENTE','ENVIANDO','ERROR')") suspend fun cantidadActivas(): Int
        @Query("UPDATE operaciones_pendientes SET estado='PENDIENTE' WHERE estado='ENVIANDO'") suspend fun recuperarInterrumpidas()
        }
