package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.PropiedadEntity
        @Dao interface PropiedadDao {
            @Upsert suspend fun insertar(registro: PropiedadEntity): Long
            @Update suspend fun actualizar(registro: PropiedadEntity)
            @Delete suspend fun eliminar(registro: PropiedadEntity)
            @Query("SELECT * FROM propiedades WHERE eliminado=0 ORDER BY distrito,titulo") fun observar(): Flow<List<PropiedadEntity>>
        @Query("SELECT * FROM propiedades WHERE id=:id") suspend fun porId(id: Long): PropiedadEntity?
        @Query("SELECT * FROM propiedades WHERE uuid=:uuid") suspend fun porUuid(uuid: String): PropiedadEntity?
        @Query("SELECT * FROM propiedades") suspend fun todas(): List<PropiedadEntity>
        @Query("SELECT COUNT(*) FROM propiedades WHERE eliminado=0 AND precio BETWEEN :minimo AND :maximo") fun contarPrecio(minimo: Double, maximo: Double): Flow<Int>
        }
