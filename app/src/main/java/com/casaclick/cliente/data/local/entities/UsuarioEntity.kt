package com.casaclick.cliente.data.local.entities
import androidx.room.*
@Entity(tableName = "usuarios")
data class UsuarioEntity(@PrimaryKey val id: Long, val correo: String, val nombre: String, val rol: String)
