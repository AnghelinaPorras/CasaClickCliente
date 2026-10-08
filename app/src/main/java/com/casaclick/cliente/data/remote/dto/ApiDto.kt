package com.casaclick.cliente.data.remote.dto
data class UsuarioDto(val id: Long, val correo: String, val nombre: String, val rol: String)
data class LoginRequest(val correo: String, val clave: String, val rol: String)
data class LoginResponse(val token: String, val usuario: UsuarioDto)
data class PropiedadDto(val id: Long = 0, val uuid: String, val titulo: String, val distrito: String, val tipoOperacion: String, val precio: Double, val habitaciones: Int, val areaM2: Double, val descripcion: String, val imagenes: List<String>, val version: Int = 0, val eliminado: Boolean = false)
data class VisitaDto(val id: Long = 0, val uuid: String, val propiedadId: Long, val usuarioId: Long = 0, val fecha: String, val hora: String, val estado: String = "SOLICITADA", val version: Int = 0, val clienteNombre: String = "")
