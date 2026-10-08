package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.FavoritoEntity
        @Dao interface FavoritoDao {
            @Upsert suspend fun insertar(registro: FavoritoEntity): Long
            @Update suspend fun actualizar(registro: FavoritoEntity)
            @Delete suspend fun eliminar(registro: FavoritoEntity)
            @Query("SELECT * FROM favoritos") fun observar(): Flow<List<FavoritoEntity>>
        @Query("SELECT * FROM favoritos WHERE propiedadId=:id AND usuarioId=:usuario") suspend fun buscar(id: Long, usuario: Long): FavoritoEntity?
        }
