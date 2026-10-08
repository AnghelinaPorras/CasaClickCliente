package com.casaclick.cliente.data.local.dao
        import androidx.room.*
        import kotlinx.coroutines.flow.Flow
        import com.casaclick.cliente.data.local.entities.UsuarioEntity
        @Dao interface UsuarioDao {
            @Upsert suspend fun insertar(registro: UsuarioEntity): Long
            @Update suspend fun actualizar(registro: UsuarioEntity)
            @Delete suspend fun eliminar(registro: UsuarioEntity)
            @Query("SELECT * FROM usuarios ORDER BY id") fun observar(): Flow<List<UsuarioEntity>>
        @Query("SELECT * FROM usuarios WHERE id=:id") suspend fun porId(id: Long): UsuarioEntity?
        }
