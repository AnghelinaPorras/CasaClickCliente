package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.VisitaEntity
        @Dao interface VisitaDao {
            @Upsert suspend fun insertar(registro: VisitaEntity): Long
            @Update suspend fun actualizar(registro: VisitaEntity)
            @Delete suspend fun eliminar(registro: VisitaEntity)
            @Query("SELECT * FROM visitas ORDER BY fecha,hora") fun observar(): Flow<List<VisitaEntity>>
        @Query("SELECT * FROM visitas WHERE id=:id") suspend fun porId(id: Long): VisitaEntity?
        @Query("SELECT * FROM visitas WHERE uuid=:uuid") suspend fun porUuid(uuid: String): VisitaEntity?
        }
